package tcc.meu_atelie.services;

import org.hibernate.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tcc.meu_atelie.dto.PerfilUsuarioDTO;
import tcc.meu_atelie.dto.UsuarioDTO;
import tcc.meu_atelie.models.Usuario;
import tcc.meu_atelie.repositories.UsuarioRepository;

import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario salvarUsuario(Usuario usuario) {
        String senhaCriptografada = passwordEncoder.encode(usuario.getSenha());
        usuario.setSenha(senhaCriptografada);

        return usuarioRepository.save(usuario);
    }

    public Usuario autenticar(String email, String senha) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            if (passwordEncoder.matches(senha, usuario.getSenha())) {
                return usuario;
            }
        }

        return null;
    }


    public PerfilUsuarioDTO buscarUsuario(String email) {
        Usuario usuario = this.usuarioRepository.findByEmail(email)
                .orElseThrow(()-> new ObjectNotFoundException(HttpStatus.BAD_REQUEST, "Não foi possível encontrar o usuário."));
        return new PerfilUsuarioDTO(usuario);

    }
}

