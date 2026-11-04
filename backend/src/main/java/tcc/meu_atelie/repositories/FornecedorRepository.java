package tcc.meu_atelie.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tcc.meu_atelie.models.Cliente;
import tcc.meu_atelie.models.Fornecedor;

import java.util.List;
import java.util.Optional;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {
    List<Fornecedor> findByUsuarioEmail(String email);
    Optional<Fornecedor> findByIdAndUsuarioEmail(Long id, String email);
}
