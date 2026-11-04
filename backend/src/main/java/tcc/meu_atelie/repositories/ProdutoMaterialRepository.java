package tcc.meu_atelie.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tcc.meu_atelie.models.ProdutoMaterial;

public interface ProdutoMaterialRepository extends JpaRepository<ProdutoMaterial, Long> {
    boolean existsByMaterialId(Long materialId);

    @Modifying
    @Query("DELETE FROM ProdutoMaterial pm WHERE pm.produto.id = :produtoId")
    void deleteByProdutoId(@Param("produtoId") Long produtoId);
}
