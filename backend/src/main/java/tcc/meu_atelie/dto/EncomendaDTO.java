package tcc.meu_atelie.dto;

import lombok.Getter;
import lombok.Setter;
import tcc.meu_atelie.enums.StatusEncomenda;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class EncomendaDTO {
    private Long id;
    private String nomeCliente;
    private BigDecimal valorTotal;

    private LocalDate dataPedido;
    private LocalDate dataEntrega;
    private StatusEncomenda status;
    private Long clienteId;
    private List<ItemDTO> itens;
    private PagamentoDTO pagamento;
}
