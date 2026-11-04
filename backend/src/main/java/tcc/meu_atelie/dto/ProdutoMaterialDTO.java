package tcc.meu_atelie.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProdutoMaterialDTO {
    private Long materialId;
    private String nomeMaterial;
    private BigDecimal quantidade;
    private String unidade;
    private BigDecimal custoCalculado;
}
