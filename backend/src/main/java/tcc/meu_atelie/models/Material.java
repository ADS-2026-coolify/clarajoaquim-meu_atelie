package tcc.meu_atelie.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "material")
public class Material {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_material")
    private Long id;

    @Column(nullable = false) private String nome;
    @Column(nullable = false) private String categoria;
    @Column(nullable = false) private String cor;

    @Column(name = "qtd_estoque", nullable = false)
    private BigDecimal quantidadeEstoque;

    @Column(nullable = false) private String unidadeMedida;

    private String codigoReferencia;
    private String marca;
    private BigDecimal estoqueMinimo;

    @Column(nullable = false)
    private String status;

    @Column(name = "preco_unitario")
    private BigDecimal precoUnitario;

    private LocalDate dataUltimaCompra;
    private String anotacoes;

    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}