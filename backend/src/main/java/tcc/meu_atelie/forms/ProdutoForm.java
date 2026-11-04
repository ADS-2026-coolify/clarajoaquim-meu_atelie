package tcc.meu_atelie.forms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ProdutoForm {
    @NotBlank
    private String nome;

    private String descricao;

    @NotNull
    private BigDecimal preco;

    @NotNull
    private Long categoriaId;

    private List<ProdutoMaterialForm> materiais;

    private BigDecimal precoCusto;
    private Integer quantidadeEstoque;
    private String status;
    private Double largura;
    private Double altura;
    private String observacoes;
}
