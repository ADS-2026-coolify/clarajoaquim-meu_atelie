package tcc.meu_atelie.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tcc.meu_atelie.dto.ClienteResumoDTO;
import tcc.meu_atelie.models.Cliente;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByUsuarioEmailAndAtivoTrue(String email);

    Optional<Cliente> findByIdAndUsuarioEmailAndAtivoTrue(Long id, String email);

    List<Cliente> findByUsuarioEmailAndAtivoFalse(String email);

    Optional<Cliente> findByIdAndUsuarioEmail(Long id, String email);

    List<Cliente> findByUsuarioEmail(String email);
}