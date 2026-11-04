package tcc.meu_atelie.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "canal_aquisicao_id")
    private CanalAquisicao canalAquisicao;

    @ManyToOne
    @JoinColumn(name = "usuario_id_usuario", nullable = false)
    @JsonIgnore
    private Usuario usuario;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 14)
    private String cpf;

    @Column(length = 18)
    private String cnpj;

    private LocalDate dataNascimento;

    private String telefone;
    private String email;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @OneToOne(mappedBy = "cliente", cascade = CascadeType.ALL)
    private Endereco endereco;

    @Column(name = "data_cadastro", updatable = false)
    private LocalDate dataCadastro;

    @OneToMany(mappedBy = "cliente")
    @JsonIgnore
    private List<Encomenda> encomendas;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean ativo = true;

    @PrePersist
    protected void onCreate() {
        this.dataCadastro = LocalDate.now();
        this.ativo = true;
    }
}
