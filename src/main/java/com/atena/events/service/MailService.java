package com.atena.events.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envio de emails transacionais (reset de senha, confirmação de troca de email).
 *
 * Usa o {@link JavaMailSender} do Spring — a configuração de SMTP vem das
 * propriedades {@code spring.mail.*}. Em dev/docker aponta para o MailHog;
 * em produção basta sobrescrever as variáveis MAIL_* para um SMTP real,
 * sem qualquer mudança neste código.
 */
@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final String from;
    private final String frontendUrl;

    public MailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String from,
            @Value("${app.frontend-url}") String frontendUrl
    ) {
        this.mailSender = mailSender;
        this.from = from;
        this.frontendUrl = frontendUrl;
    }

    public void sendPasswordReset(String toEmail, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject("AtenaEvents — Redefinição de senha");
        message.setText(
            "Recebemos um pedido para redefinir a sua senha.\n\n" +
            "Para criar uma nova senha, acesse o link abaixo:\n" +
            link + "\n\n" +
            "Se você não solicitou isso, ignore este email — sua senha permanece inalterada.\n" +
            "Este link expira em breve."
        );
        mailSender.send(message);
    }

    /**
     * Notifica um participante sobre o evento. {@code preEvent=true} envia um
     * lembrete (antes do evento); {@code false} envia um agradecimento (após o
     * evento). {@code customMessage} é opcional — quando preenchido, é anexado
     * ao corpo como mensagem do organizador.
     */
    public void sendEventNotification(String toEmail, String eventTitle,
                                      boolean preEvent, String customMessage) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);

        StringBuilder body = new StringBuilder();
        if (preEvent) {
            message.setSubject("AtenaEvents — Lembrete: " + eventTitle);
            body.append("Olá!\n\n")
                .append("Este é um lembrete de que o evento \"").append(eventTitle)
                .append("\" do qual você é participante está chegando.\n")
                .append("Esperamos você lá!\n");
        } else {
            message.setSubject("AtenaEvents — Obrigado por participar de " + eventTitle);
            body.append("Olá!\n\n")
                .append("Obrigado por participar do evento \"").append(eventTitle)
                .append("\". Esperamos que tenha sido uma ótima experiência!\n");
        }

        if (customMessage != null && !customMessage.isBlank()) {
            body.append("\nMensagem do organizador:\n").append(customMessage.trim()).append("\n");
        }

        message.setText(body.toString());
        mailSender.send(message);
    }

    /**
     * Avisa um participante de que o quadro interativo do evento foi aberto,
     * com link direto para a página do evento.
     */
    public void sendWhiteboardOpened(String toEmail, String eventTitle, Long eventId) {
        String link = frontendUrl + "/events/" + eventId;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject("AtenaEvents — O quadro de \"" + eventTitle + "\" está aberto!");
        message.setText(
            "Olá!\n\n" +
            "O quadro interativo do evento \"" + eventTitle + "\" acaba de ser aberto e está " +
            "disponível por tempo limitado.\n" +
            "Entre agora para deixar seus post-its (mensagens ou fotos polaroid):\n" +
            link + "\n\n" +
            "Aproveite — quando o tempo acabar, o quadro fica apenas como lembrança do evento.\n"
        );
        mailSender.send(message);
    }

    public void sendEmailChangeConfirmation(String toEmail, String token) {
        String link = frontendUrl + "/confirm-email?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject("AtenaEvents — Confirme seu novo email");
        message.setText(
            "Você pediu para alterar o email da sua conta para este endereço.\n\n" +
            "Para confirmar a troca, acesse o link abaixo:\n" +
            link + "\n\n" +
            "Se você não solicitou isso, ignore este email — nada será alterado.\n" +
            "Este link expira em breve."
        );
        mailSender.send(message);
    }
}
