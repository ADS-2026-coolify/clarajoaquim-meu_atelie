package tcc.meu_atelie.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ClienteDetalheDTO {
    private Long id;
    private String nome;
    private String cpf;
    private String cnpj;
    private LocalDate dataNascimento;
    private String telefone;
    private String email;
    private String observacoes;
    private CanalDTO canalAquisicao;
    private EnderecoDTO endereco;

    @Getter @Setter @NoArgsConstructor
    public static class CanalDTO {
        private Long id;
        private String descricao;
    }
}