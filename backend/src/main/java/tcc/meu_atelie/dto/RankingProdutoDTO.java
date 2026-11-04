package tcc.meu_atelie.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class RankingProdutoDTO {
    private Integer posicao;
    private String nomeProduto;
    private Long quantidadeVendida;
    private BigDecimal receita;
    private BigDecimal percentualReceita;

    public RankingProdutoDTO(String nomeProduto, Long quantidadeVendida, BigDecimal receita) {
        this.nomeProduto = nomeProduto;
        this.quantidadeVendida = quantidadeVendida;
        this.receita = receita;
    }
}
