package tcc.meu_atelie.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tcc.meu_atelie.enums.StatusEncomenda;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "encomenda")
public class Encomenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_encomenda")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "cliente_id_cliente", nullable = false)
    private Cliente cliente;

    @Column(name = "data_pedido")
    private LocalDate dataPedido;

    @Column(name = "data_entrega")
    private LocalDate dataEntrega;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private StatusEncomenda status;

    @Column(name = "valor_total")
    private BigDecimal valorTotal;

    @OneToMany(mappedBy = "encomenda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemEncomenda> itens;

    @OneToOne(mappedBy = "encomenda", cascade = CascadeType.ALL)
    private Pagamento pagamento;
}
