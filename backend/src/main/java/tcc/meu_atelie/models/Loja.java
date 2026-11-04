package tcc.meu_atelie.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "loja")
public class Loja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_loja")
    private Long idLoja;

    @Column(nullable = false)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, unique = true)
    private String cnpj;

    private String whatsapp;
    private String facebook;
    private String instagram;
    private String fotoUrl;

    @OneToOne
    @JoinColumn(name = "usuario_id_usuario", nullable = false)
    @JsonIgnore
    private Usuario usuario;

    @OneToOne(mappedBy = "loja", cascade = CascadeType.ALL)
    private Endereco endereco;
}