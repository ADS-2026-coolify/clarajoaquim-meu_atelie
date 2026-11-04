package tcc.meu_atelie.dto;

import lombok.Getter;
import lombok.Setter;
import tcc.meu_atelie.models.Fornecedor;

@Getter
@Setter
public class FornecedorDTO {
    private Long id;
    private String nome;
    private String cnpjCpf;
    private String telefone;
    private String email;

    public FornecedorDTO(Fornecedor f) {
        this.id = f.getId();
        this.nome = f.getNome();
        this.cnpjCpf = f.getCnpjCpf();
        this.telefone = f.getTelefone();
        this.email = f.getEmail();
    }
}