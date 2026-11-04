package tcc.meu_atelie.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class IndicadoresBrutoDTO {
    private BigDecimal faturamento;
    private Long totalPedidos;

    public IndicadoresBrutoDTO(BigDecimal faturamento, Long totalPedidos) {
        this.faturamento = faturamento;
        this.totalPedidos = totalPedidos;
    }
}
