package tcc.meu_atelie.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tcc.meu_atelie.models.Cliente;
import tcc.meu_atelie.models.Material;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findByUsuarioEmail(String email);
    List<Material> findByIdInAndUsuarioEmail(List<Long> ids, String email);
}
