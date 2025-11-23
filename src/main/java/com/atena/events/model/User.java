package com.atena.events.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String email;
    private String senha;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Event> eventosCriados;

    @ManyToMany(mappedBy = "participantes")
    private List<Event> eventosParticipando;

    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Comment> comentarios;
}
