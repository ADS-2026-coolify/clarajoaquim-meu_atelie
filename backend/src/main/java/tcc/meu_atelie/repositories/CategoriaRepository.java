package tcc.meu_atelie.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tcc.meu_atelie.models.Categoria;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findByUsuarioIdUsuario(Long idUsuario);
    Optional<Categoria> findByIdAndUsuarioIdUsuario(Long id, Long idUsuario);
}
