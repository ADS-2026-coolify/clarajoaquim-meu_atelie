package tcc.meu_atelie.forms;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FornecedorForm {
    private String nome;
    private String cnpjCpf;
    private String telefone;
    private String email;
}
