package com.atena.events.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.atena.events.model.Event;
import com.atena.events.model.PostIt;
import com.atena.events.model.PostItType;
import com.atena.events.model.User;
import com.atena.events.model.dto.PostItCreateDTO;
import com.atena.events.model.dto.PostItMoveDTO;
import com.atena.events.model.dto.PostItResponseDTO;
import com.atena.events.model.dto.WhiteboardDTO;
import com.atena.events.repository.EventRepository;
import com.atena.events.repository.PostItRepository;
import com.atena.events.repository.UserRepository;

@Service
@Transactional
public class WhiteboardService {

    // ───────────────────────────────────────────────────────────────────────
    // DURAÇÃO DO QUADRO — altere AQUI para mudar quanto tempo o quadro fica ativo
    // após ser ativado (ex.: Duration.ofMinutes(30), Duration.ofHours(1)).
    // O frontend recebe expiresAt + serverNow e calcula o restante a partir disto.
    // ───────────────────────────────────────────────────────────────────────
    private static final Duration WINDOW = Duration.ofHours(4);
    // Limite de post-its por usuário.
    private static final int MAX_POST_ITS_PER_USER = 2;
    // Paleta pastel para post-its de texto (atribuída por rotação).
    private static final List<String> COLORS =
            List.of("#FFF8B8", "#FFD6E0", "#C8F0D2", "#CDE7FF", "#E5D4FF", "#FFE0C2");

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final PostItRepository postItRepository;
    private final ParticipationService participationService;
    private final MailService mailService;

    public WhiteboardService(EventRepository eventRepository, UserRepository userRepository,
                             PostItRepository postItRepository, ParticipationService participationService,
                             MailService mailService) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.postItRepository = postItRepository;
        this.participationService = participationService;
        this.mailService = mailService;
    }

    @Transactional(readOnly = true)
    public WhiteboardDTO getBoard(Long eventId, Long currentUserId) {
        Event event = findEventOrThrow(eventId);

        WhiteboardDTO dto = new WhiteboardDTO();
        // Relógio do servidor — o frontend usa (expiresAt - serverNow) para o
        // tempo restante, fazendo o offset de fuso horário se cancelar.
        dto.setServerNow(LocalDateTime.now());
        LocalDateTime activatedAt = event.getWhiteboardActivatedAt();
        dto.setActivated(activatedAt != null);
        dto.setActivatedAt(activatedAt);

        if (activatedAt != null) {
            LocalDateTime expiresAt = activatedAt.plus(WINDOW);
            dto.setExpiresAt(expiresAt);
            boolean expired = LocalDateTime.now().isAfter(expiresAt);
            dto.setExpired(expired);
            dto.setActive(!expired);
        }

        dto.setPostIts(postItRepository.findByEventIdOrderByCreatedAtAsc(eventId).stream()
                .map(PostItResponseDTO::new)
                .toList());

        if (currentUserId != null) {
            dto.setMyPostItCount(postItRepository.countByEventIdAndAuthorId(eventId, currentUserId));
        }

        return dto;
    }

    public WhiteboardDTO activate(Long eventId, Long currentUserId) {
        Event event = findEventOrThrow(eventId);
        verifyOwnership(event, currentUserId);

        if (event.getWhiteboardActivatedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O quadro deste evento já foi ativado.");
        }

        event.setWhiteboardActivatedAt(LocalDateTime.now());
        eventRepository.save(event);

        notifyParticipantsBoardOpened(event);

        return getBoard(eventId, currentUserId);
    }

    // Avisa por email os participantes (com email; convidados não têm) de que o
    // quadro foi aberto. Falha de envio não impede a ativação.
    private void notifyParticipantsBoardOpened(Event event) {
        if (event.getParticipations() == null) return;
        Long ownerId = event.getOwner().getId();

        List<String> recipients = event.getParticipations().stream()
                .filter(p -> p != null && "OK".equals(p.getStatus()) && p.getUser() != null)
                .filter(p -> !p.getUser().getId().equals(ownerId)) // o dono ativou, não precisa receber
                .map(p -> p.getUser().getEmail())
                .filter(email -> email != null && !email.isBlank())
                .distinct()
                .toList();

        for (String email : recipients) {
            try {
                mailService.sendWhiteboardOpened(email, event.getTitle(), event.getId());
            } catch (Exception e) {
                // Email é secundário — não deve quebrar a ativação do quadro.
            }
        }
    }

    public PostItResponseDTO addPostIt(Long eventId, Long currentUserId, PostItCreateDTO dto) {
        Event event = findEventOrThrow(eventId);
        requireActiveBoard(event);

        boolean isOwner = event.getOwner().getId().equals(currentUserId);
        if (!isOwner && !participationService.isParticipating(eventId, currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Você precisa estar participando do evento para usar o quadro.");
        }

        if (postItRepository.countByEventIdAndAuthorId(eventId, currentUserId) >= MAX_POST_ITS_PER_USER) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Você atingiu o limite de " + MAX_POST_ITS_PER_USER + " post-its.");
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (dto.getType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo do post-it é obrigatório.");
        }

        PostIt postIt = new PostIt();
        postIt.setEvent(event);
        postIt.setAuthor(user);
        postIt.setType(dto.getType());
        postIt.setXPct(clamp(dto.getXPct()));
        postIt.setYPct(clamp(dto.getYPct()));
        postIt.setCreatedAt(LocalDateTime.now());

        if (dto.getType() == PostItType.PHOTO) {
            if (dto.getImageBase64() == null || dto.getImageBase64().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A foto é obrigatória para post-it polaroid.");
            }
            postIt.setImageBase64(dto.getImageBase64());
            postIt.setText(validatePhotoCaption(dto.getText()));
        } else {
            String text = dto.getText() == null ? "" : dto.getText().trim();
            if (text.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A mensagem é obrigatória.");
            }
            if (text.length() > 200) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A mensagem é muito longa (máx. 200).");
            }
            postIt.setText(text);
            postIt.setColor(pickColor(eventId));
        }

        postItRepository.save(postIt);
        return new PostItResponseDTO(postIt);
    }

    public PostItResponseDTO movePostIt(Long postItId, Long currentUserId, PostItMoveDTO dto) {
        PostIt postIt = postItRepository.findById(postItId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post-it não encontrado"));

        requireActiveBoard(postIt.getEvent());

        if (!postIt.getAuthor().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você só pode mover seus próprios post-its.");
        }

        postIt.setXPct(clamp(dto.getXPct()));
        postIt.setYPct(clamp(dto.getYPct()));
        postItRepository.save(postIt);

        return new PostItResponseDTO(postIt);
    }

    public void deletePostIt(Long postItId, Long currentUserId) {
        PostIt postIt = postItRepository.findById(postItId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post-it não encontrado"));

        boolean isAuthor = postIt.getAuthor().getId().equals(currentUserId);
        boolean isEventOwner = postIt.getEvent().getOwner().getId().equals(currentUserId);

        if (!isAuthor && !isEventOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para remover este post-it.");
        }

        postItRepository.delete(postIt);
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private void requireActiveBoard(Event event) {
        LocalDateTime activatedAt = event.getWhiteboardActivatedAt();
        if (activatedAt == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O quadro deste evento ainda não foi ativado.");
        }
        if (LocalDateTime.now().isAfter(activatedAt.plus(WINDOW))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "O quadro deste evento já encerrou.");
        }
    }

    private String validatePhotoCaption(String caption) {
        String c = caption == null ? "" : caption.trim();
        if (c.isEmpty()) return null; // legenda é opcional na polaroid
        if (c.contains(" ")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A legenda da polaroid deve ter apenas uma palavra.");
        }
        if (c.length() > 20) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A legenda é muito longa (máx. 20 caracteres).");
        }
        return c;
    }

    private String pickColor(Long eventId) {
        long count = postItRepository.findByEventIdOrderByCreatedAtAsc(eventId).size();
        return COLORS.get((int) (count % COLORS.size()));
    }

    private Double clamp(Double value) {
        if (value == null) return 50.0; // centro por padrão
        return Math.max(0.0, Math.min(100.0, value));
    }

    private Event findEventOrThrow(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento não encontrado."));
    }

    private void verifyOwnership(Event event, Long userId) {
        if (!event.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Apenas o dono do evento pode ativar o quadro.");
        }
    }
}
