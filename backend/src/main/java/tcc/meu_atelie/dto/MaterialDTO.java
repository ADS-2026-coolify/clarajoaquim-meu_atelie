package tcc.meu_atelie.dto;

import lombok.Getter;
import lombok.Setter;
import tcc.meu_atelie.models.Material;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class MaterialDTO {
    private Long id;
    private String nome;
    private String categoria;
    private String cor;
    private String marca;
    private BigDecimal quantidadeEstoque;
    private BigDecimal precoUnitario;
    private String unidadeMedida;
    private String anotacoes;
    private String fornecedor;
    private Long fornecedorId;
    private String codigoReferencia;
    private BigDecimal estoqueMinimo;
    private LocalDate dataUltimaCompra;
    private String status;

    public MaterialDTO() {}

    public MaterialDTO(Material material) {
        this.id = material.getId();
        this.nome = material.getNome();
        this.cor = material.getCor();
        this.marca = material.getMarca();
        this.status = material.getStatus();
        this.quantidadeEstoque = material.getQuantidadeEstoque();
        this.precoUnitario = material.getPrecoUnitario();
        this.unidadeMedida = material.getUnidadeMedida();
        this.anotacoes = material.getAnotacoes();
        if (material.getCategoria() != null) {
            this.categoria = material.getCategoria();
        }
        if (material.getFornecedor() != null) {
            this.fornecedor = material.getFornecedor().getNome();
            this.fornecedorId = material.getFornecedor().getId();
        }
        this.codigoReferencia = material.getCodigoReferencia();
        this.estoqueMinimo = material.getEstoqueMinimo();
        this.dataUltimaCompra = material.getDataUltimaCompra();
    }
}
