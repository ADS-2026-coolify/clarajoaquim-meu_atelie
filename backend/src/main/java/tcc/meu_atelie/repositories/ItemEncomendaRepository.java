package tcc.meu_atelie.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tcc.meu_atelie.dto.RankingProdutoDTO;
import tcc.meu_atelie.enums.StatusEncomenda;
import tcc.meu_atelie.models.ItemEncomenda;

import java.time.LocalDate;
import java.util.List;

public interface ItemEncomendaRepository extends JpaRepository<ItemEncomenda, Long> {

    @Query("SELECT new tcc.meu_atelie.dto.RankingProdutoDTO(" +
            "p.nome, SUM(i.quantidade), SUM(i.quantidade * i.valorUnitario)) " +
            "FROM ItemEncomenda i JOIN i.produto p JOIN i.encomenda e " +
            "WHERE e.usuario.idUsuario = :usuarioId AND e.status IN :statusValidos " +
            "AND e.dataPedido BETWEEN :inicio AND :fim " +
            "GROUP BY p.id, p.nome " +
            "ORDER BY SUM(i.quantidade * i.valorUnitario) DESC")
    List<RankingProdutoDTO> rankingProdutos(@Param("usuarioId") Long usuarioId,
                                             @Param("statusValidos") List<StatusEncomenda> statusValidos,
                                             @Param("inicio") LocalDate inicio,
                                             @Param("fim") LocalDate fim);

    boolean existsByProdutoId(Long produtoId);
}
