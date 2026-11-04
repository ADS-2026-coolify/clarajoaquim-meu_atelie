package tcc.meu_atelie.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProdutoDTO {
    private Long id;
    private String nomeCategoria;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Long categoriaId;
    private List<ProdutoMaterialDTO> materiaisUtilizados;
    private BigDecimal custoMateriaisCalculado;
    private String imagem;

    private BigDecimal precoCusto;
    private Integer quantidadeEstoque;
    private String status;
    private Double largura;
    private Double altura;
    private String observacoes;
}