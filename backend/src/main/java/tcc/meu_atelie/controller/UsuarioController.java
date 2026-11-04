package tcc.meu_atelie.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tcc.meu_atelie.auth.TokenService;

import tcc.meu_atelie.dto.EnderecoDTO;
import tcc.meu_atelie.dto.PerfilUsuarioDTO;
import tcc.meu_atelie.dto.UsuarioDTO;
import tcc.meu_atelie.models.Endereco;
import tcc.meu_atelie.models.Loja;
import tcc.meu_atelie.models.Usuario;
import tcc.meu_atelie.services.UsuarioService;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TokenService tokenService;


    @PostMapping
    public ResponseEntity<?> criarUsuario(@RequestBody UsuarioDTO dto) {
        try {
            Usuario usuario = new Usuario();
            usuario.setNome(dto.getNome());
            usuario.setEmail(dto.getEmail());
            usuario.setTelefone(dto.getTelefone());
            usuario.setCpf(dto.getCpf());
            usuario.setSenha(dto.getSenha());

            Loja loja = new Loja();
            loja.setNome(dto.getNomeLoja());
            loja.setDescricao(dto.getDescricaoLoja());
            loja.setCnpj(dto.getCnpj());
            loja.setWhatsapp(dto.getWhatsapp());
            loja.setFacebook(dto.getFacebook());
            loja.setInstagram(dto.getInstagram());
            loja.setUsuario(usuario);

            EnderecoDTO enderecoDTO = dto.getEndereco();
            if (enderecoDTO != null) {
                Endereco endereco = new Endereco();
                endereco.setCep(enderecoDTO.getCep());
                endereco.setRua(enderecoDTO.getRua());
                endereco.setNumero(enderecoDTO.getNumero());
                endereco.setComplemento(enderecoDTO.getComplemento());
                endereco.setBairro(enderecoDTO.getBairro());
                endereco.setCidade(enderecoDTO.getCidade());
                endereco.setEstado(enderecoDTO.getEstado());
                endereco.setLoja(loja);
                loja.setEndereco(endereco);
            }

            usuario.setLoja(loja);

            Usuario salvo = usuarioService.salvarUsuario(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(new UsuarioDTO(salvo));

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "E-mail ou CNPJ já cadastrado."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erro ao cadastrar usuário e loja."));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> fazerLogin(@RequestBody UsuarioDTO usuarioDTO) {

        Usuario usuarioAutenticado = usuarioService.autenticar(
                usuarioDTO.getEmail(),
                usuarioDTO.getSenha()
        );

        if (usuarioAutenticado != null) {

            String jwtToken = tokenService.gerarToken(usuarioAutenticado);

            UsuarioDTO usuarioAutenticadoDTO = new UsuarioDTO(
                    usuarioAutenticado.getIdUsuario(),
                    usuarioAutenticado.getNome(),
                    usuarioAutenticado.getEmail(),
                    usuarioAutenticado.getTelefone(),
                    usuarioAutenticado.getCpf(),
                    null,
                    jwtToken
            );


            return ResponseEntity.ok(usuarioAutenticadoDTO);

        } else {
            return ResponseEntity.status(401).body("Credenciais inválidas");
        }
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> listarUsuarios(@PathVariable String email, java.security.Principal principal) {
        if (principal == null || !principal.getName().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Acesso negado."));
        }
        return ResponseEntity.ok(this.usuarioService.buscarUsuario(email));
    }
}

