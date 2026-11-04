package tcc.meu_atelie.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResumoDTO {
    private long emAndamento;
    private long finalizados;
    private long atrasados;
}
