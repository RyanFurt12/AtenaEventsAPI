package com.atena.events.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "post_it")
public class PostIt implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PostItType type;

    // Mensagem (TEXT) ou legenda de uma palavra na parte branca da polaroid (PHOTO).
    private String text;

    // Apenas para PHOTO — foto em base64 (estilo polaroid).
    @Column(columnDefinition = "TEXT")
    private String imageBase64;

    // Cor pastel atribuída pelo service (apenas TEXT).
    private String color;

    // Posição em porcentagem (0–100) relativa ao quadro → responsivo.
    private Double xPct;
    private Double yPct;

    // Usado para a ordem de empilhamento: quem inseriu primeiro fica em cima.
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "event_id")
    @JsonBackReference
    private Event event;

    @ManyToOne
    @JoinColumn(name = "author_id")
    @JsonBackReference
    private User author;
}
