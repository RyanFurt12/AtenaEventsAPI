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
public class Comment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String texto;

    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    // Comentário pertence a um evento
    @ManyToOne
    @JoinColumn(name = "evento_id")
    @JsonBackReference
    private Event evento;

    // Comentário tem um autor
    @ManyToOne
    @JoinColumn(name = "autor_id")
    @JsonBackReference
    private User autor;
}
