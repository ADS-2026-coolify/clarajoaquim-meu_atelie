package tcc.meu_atelie.dto;

import lombok.Getter;
import lombok.Setter;
import tcc.meu_atelie.enums.StatusEncomenda;

import java.time.LocalDate;

@Getter
@Setter
public class PedidoRecenteDTO {
    private Long id;
    private String nomeCliente;
    private String tipoPrincipal;
    private LocalDate dataEntrega;
    private StatusEncomenda status;
}