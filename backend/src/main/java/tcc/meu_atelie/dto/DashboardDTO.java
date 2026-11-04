package tcc.meu_atelie.dto;

import lombok.Getter;
import lombok.Setter;
import tcc.meu_atelie.enums.StatusEncomenda;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class DashboardDTO {
    private String nomeUsuario;
    private ResumoDTO resumo;
    private List<AlertaDTO> alertas;
    private List<PedidoRecenteDTO> pedidosRecentes;
}
