    package tcc.meu_atelie.models;

    import com.fasterxml.jackson.annotation.JsonIgnore;
    import jakarta.persistence.*;
    import lombok.Getter;
    import lombok.Setter;
    import java.math.BigDecimal;

    @Getter
    @Setter
    @Entity
    @Table(name = "item_encomenda")
    public class ItemEncomenda {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id_item")
        private Long id;

        @ManyToOne
        @JoinColumn(name = "encomenda_id_encomenda", nullable = false)
        @JsonIgnore
        private Encomenda encomenda;

        @ManyToOne
        @JoinColumn(name = "produto_id_produto", nullable = false)
        private Produto produto;

        private Integer quantidade;

        @Column(name = "valor_unitario")
        private BigDecimal valorUnitario;
    }