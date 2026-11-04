package tcc.meu_atelie.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class RankingClienteRelatorioDTO {
    private String nomeCliente;
    private Long totalPedidos;
    private BigDecimal valorTotal;
    private LocalDate dataUltimoPedido;

    public RankingClienteRelatorioDTO(String nomeCliente, Long totalPedidos, BigDecimal valorTotal, LocalDate dataUltimoPedido) {
        this.nomeCliente = nomeCliente;
        this.totalPedidos = totalPedidos;
        this.valorTotal = valorTotal;
        this.dataUltimoPedido = dataUltimoPedido;
    }
}
