package tcc.meu_atelie.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "canal_aquisicao",
        uniqueConstraints = @UniqueConstraint(name = "uk_canal_aquisicao_descricao",
                columnNames = "descricao"))
public class CanalAquisicao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_canal")
    private Long id;

    @Column(nullable = false, length = 150)
    private String descricao;
}
