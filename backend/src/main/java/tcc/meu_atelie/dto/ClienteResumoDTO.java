package tcc.meu_atelie.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ClienteResumoDTO {
    private Long id;
    private String nome;
    private String telefone;
    private String email;
    private Long totalPedidos;
    private BigDecimal totalGasto;
    private LocalDate dataCadastro;
    private boolean ativo;

    public ClienteResumoDTO(Long id, String nome, String telefone, String email, Long totalPedidos, BigDecimal totalGasto, LocalDate dataCadastro, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        this.totalPedidos = totalPedidos;
        this.totalGasto = totalGasto != null ? totalGasto : BigDecimal.ZERO;
        this.dataCadastro = dataCadastro;
        this.ativo = ativo;
    }
}