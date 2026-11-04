package tcc.meu_atelie.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LojaDTO {
    private Long idLoja;
    private String nome;
    private String email;
    private String descricao;
    private String cnpj;
    private String whatsapp;
    private String facebook;
    private String instagram;
    private Long idUsuario;
    private String nomeUsuario;
    private String fotoUrl;
    private EnderecoDTO endereco;
}
