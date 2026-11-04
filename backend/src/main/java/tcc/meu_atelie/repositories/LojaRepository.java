package tcc.meu_atelie.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tcc.meu_atelie.models.Loja;

import java.util.Optional;

public interface LojaRepository extends JpaRepository<Loja, Long> {
    Optional<Loja> findByUsuarioIdUsuario(Long idUsuario);
}
