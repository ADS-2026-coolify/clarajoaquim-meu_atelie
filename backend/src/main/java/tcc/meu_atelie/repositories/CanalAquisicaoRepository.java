package tcc.meu_atelie.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tcc.meu_atelie.models.CanalAquisicao;

import java.util.Optional;

public interface CanalAquisicaoRepository extends JpaRepository<CanalAquisicao, Long> {

    Optional<CanalAquisicao> findByDescricaoIgnoreCase(String descricao);
}