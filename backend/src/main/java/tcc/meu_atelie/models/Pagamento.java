package tcc.meu_atelie.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "pagamento")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pagamento")
    private Long id;

    @OneToOne
    @JoinColumn(name = "encomenda_id_encomenda", nullable = false)
    @JsonIgnore
    private Encomenda encomenda;

    @Column(name = "desconto_valor")
    private BigDecimal descontoValor;

    @Column(name = "desconto_percentual")
    private BigDecimal descontoPercentual;

    private BigDecimal frete;

    private LocalDate validade;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Column(name = "condicoes_pagamento")
    private String condicoesPagamento;

    @Column(columnDefinition = "TEXT")
    private String observacoes;
}