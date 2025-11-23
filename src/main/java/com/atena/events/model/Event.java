package com.atena.events.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Event implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String descricao;
    private LocalDateTime date;

    // Dono do evento
    @ManyToOne
    @JoinColumn(name = "owner_id")
    @JsonBackReference
    private User owner;

    // Participantes do evento (muitos para muitos)
    @ManyToMany
    @JoinTable(
            name = "evento_participantes",
            joinColumns = @JoinColumn(name = "evento_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private List<User> participantes;

    // Comentários do evento
    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Comment> comentarios;
}
