package tcc.meu_atelie.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class PagamentoDTO {
    private BigDecimal frete;
    private BigDecimal descontoValor;
    private String condicoesPagamento;
    private LocalDate dataPagamento;
}
