package tcc.meu_atelie.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tcc.meu_atelie.models.Produto;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByUsuarioIdUsuario(Long idUsuario);
    Optional<Produto> findByIdAndUsuarioEmail(Long id, String email);
}
