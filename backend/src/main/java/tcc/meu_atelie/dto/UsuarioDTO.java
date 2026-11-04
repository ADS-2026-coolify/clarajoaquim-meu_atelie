package tcc.meu_atelie.dto;

import lombok.Getter;
import lombok.Setter;
import tcc.meu_atelie.models.Usuario;

@Getter
@Setter
public class UsuarioDTO {
    private Long idUsuario;
    private String nome;
    private String email;
    private String telefone;
    private String cpf;
    private String senha;
    private String token;

    private String nomeLoja;
    private String descricaoLoja;
    private String cnpj;
    private String whatsapp;
    private String facebook;
    private String instagram;
    private EnderecoDTO endereco;

    public UsuarioDTO() {}

    public UsuarioDTO(Usuario usuario) {
        this.idUsuario = usuario.getIdUsuario();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.telefone = usuario.getTelefone();
        this.cpf = usuario.getCpf();
    }

    public UsuarioDTO(Long idUsuario,
                      String nome,
                      String email,
                      String telefone,
                      String cpf,
                      String senha,
                      String token) {

        this.idUsuario = idUsuario;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.cpf = cpf;
        this.senha = senha;
        this.token = token;
    }
}
