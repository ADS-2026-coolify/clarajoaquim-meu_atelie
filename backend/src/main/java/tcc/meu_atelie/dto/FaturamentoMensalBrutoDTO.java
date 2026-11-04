package tcc.meu_atelie.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tcc.meu_atelie.enums.StatusEncomenda;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class FaturamentoMensalBrutoDTO {
    private LocalDate dataPedido;
    private StatusEncomenda status;
    private BigDecimal valorTotal;

    public FaturamentoMensalBrutoDTO(LocalDate dataPedido, StatusEncomenda status, BigDecimal valorTotal) {
        this.dataPedido = dataPedido;
        this.status = status;
        this.valorTotal = valorTotal;
    }
}
