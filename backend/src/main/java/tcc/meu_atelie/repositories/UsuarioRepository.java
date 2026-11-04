package tcc.meu_atelie.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tcc.meu_atelie.models.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    String email(String email);
}
