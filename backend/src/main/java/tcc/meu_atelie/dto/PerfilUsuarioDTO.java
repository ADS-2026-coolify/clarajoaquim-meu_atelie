package tcc.meu_atelie.dto;

import lombok.Getter;
import lombok.Setter;
import tcc.meu_atelie.models.Usuario;

@Getter
@Setter
public class PerfilUsuarioDTO {
    private String nome;
    private String email;
    private String telefone;

    public PerfilUsuarioDTO(Usuario usuario) {
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.telefone = usuario.getTelefone();
    }
}
