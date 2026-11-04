package tcc.meu_atelie.forms;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class MaterialForm {
    @NotBlank
    private String nome;

    @NotBlank
    private String categoria;

    @NotNull
    @DecimalMin(value = "0.01", message = "A quantidade deve ser maior que zero")
    private BigDecimal quantidadeEstoque;

    @DecimalMin(value = "0.01", message = "O estoque mínimo deve ser maior que zero")
    private BigDecimal estoqueMinimo;

    @NotNull
    @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
    private BigDecimal precoUnitario;
    private String unidadeMedida;
    private String codigoReferencia;
    private String cor;
    private String marca;
    private LocalDate dataUltimaCompra;
    private String anotacoes;
    private Long fornecedorId;
    private String status;
}