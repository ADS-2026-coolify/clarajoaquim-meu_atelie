package tcc.meu_atelie.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "produto_material")
public class ProdutoMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produto_material")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produto_id_produto", nullable = false)
    @JsonIgnore
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "material_id_material", nullable = false)
    private Material material;

    private BigDecimal quantidade;

    @Column(name = "unidade_informada")
    private String unidadeInformada;
}
