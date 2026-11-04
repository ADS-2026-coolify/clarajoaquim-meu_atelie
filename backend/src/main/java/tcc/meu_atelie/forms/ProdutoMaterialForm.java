package tcc.meu_atelie.forms;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProdutoMaterialForm {
    @NotNull
    private Long materialId;

    private BigDecimal quantidade;

    private String unidade;
}
