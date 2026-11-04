package tcc.meu_atelie.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IndicadoresPeriodoDTO {
    private BigDecimal faturamento;
    private BigDecimal faturamentoVariacaoPercentual;
    private Long totalPedidos;
    private BigDecimal totalPedidosVariacaoPercentual;
    private Long novosClientes;
    private BigDecimal novosClientesVariacaoPercentual;
    private BigDecimal ticketMedio;
    private BigDecimal ticketMedioVariacaoPercentual;
}
