package tcc.meu_atelie.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tcc.meu_atelie.dto.FaturamentoMensalBrutoDTO;
import tcc.meu_atelie.dto.IndicadoresBrutoDTO;
import tcc.meu_atelie.dto.RankingClienteRelatorioDTO;
import tcc.meu_atelie.enums.StatusEncomenda;
import tcc.meu_atelie.models.Encomenda;

import java.time.LocalDate;
import java.util.List;

public interface EncomendaRepository extends JpaRepository<Encomenda, Long> {

    List<Encomenda> findByUsuarioEmailOrderByIdAsc(String email);

    List<Encomenda> findByStatusAndDataPedidoBefore(StatusEncomenda status, LocalDate data);

    boolean existsByClienteId(Long clienteId);

    @Query("SELECT new tcc.meu_atelie.dto.IndicadoresBrutoDTO(" +
            "COALESCE(SUM(e.valorTotal), 0), COUNT(e)) " +
            "FROM Encomenda e " +
            "WHERE e.usuario.idUsuario = :usuarioId AND e.status IN :statusValidos " +
            "AND e.dataPedido BETWEEN :inicio AND :fim")
    IndicadoresBrutoDTO buscarIndicadores(@Param("usuarioId") Long usuarioId,
                                           @Param("statusValidos") List<StatusEncomenda> statusValidos,
                                           @Param("inicio") LocalDate inicio,
                                           @Param("fim") LocalDate fim);

    @Query("SELECT COUNT(DISTINCT e.cliente.id) FROM Encomenda e " +
            "WHERE e.usuario.idUsuario = :usuarioId AND e.status IN :statusValidos " +
            "AND e.dataPedido BETWEEN :inicio AND :fim " +
            "AND e.dataPedido = (" +
            "    SELECT MIN(e2.dataPedido) FROM Encomenda e2 " +
            "    WHERE e2.cliente = e.cliente AND e2.usuario.idUsuario = :usuarioId " +
            "    AND e2.status IN :statusValidos" +
            ")")
    Long contarNovosClientes(@Param("usuarioId") Long usuarioId,
                              @Param("statusValidos") List<StatusEncomenda> statusValidos,
                              @Param("inicio") LocalDate inicio,
                              @Param("fim") LocalDate fim);

    @Query("SELECT new tcc.meu_atelie.dto.FaturamentoMensalBrutoDTO(" +
            "e.dataPedido, e.status, e.valorTotal) " +
            "FROM Encomenda e " +
            "WHERE e.usuario.idUsuario = :usuarioId AND e.status IN :statusValidos " +
            "AND e.dataPedido BETWEEN :inicio AND :fim")
    List<FaturamentoMensalBrutoDTO> buscarParaFaturamentoMensal(@Param("usuarioId") Long usuarioId,
                                                                  @Param("statusValidos") List<StatusEncomenda> statusValidos,
                                                                  @Param("inicio") LocalDate inicio,
                                                                  @Param("fim") LocalDate fim);

    @Query("SELECT new tcc.meu_atelie.dto.RankingClienteRelatorioDTO(" +
            "c.nome, COUNT(e), SUM(e.valorTotal), MAX(e.dataPedido)) " +
            "FROM Encomenda e JOIN e.cliente c " +
            "WHERE e.usuario.idUsuario = :usuarioId AND e.status IN :statusValidos " +
            "AND e.dataPedido BETWEEN :inicio AND :fim " +
            "GROUP BY c.id, c.nome " +
            "ORDER BY SUM(e.valorTotal) DESC")
    List<RankingClienteRelatorioDTO> rankingClientes(@Param("usuarioId") Long usuarioId,
                                                       @Param("statusValidos") List<StatusEncomenda> statusValidos,
                                                       @Param("inicio") LocalDate inicio,
                                                       @Param("fim") LocalDate fim,
                                                       Pageable pageable);
}
