package tcc.meu_atelie.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tcc.meu_atelie.dto.*;
import tcc.meu_atelie.enums.StatusEncomenda;
import tcc.meu_atelie.models.Usuario;
import tcc.meu_atelie.repositories.EncomendaRepository;
import tcc.meu_atelie.repositories.ItemEncomendaRepository;
import tcc.meu_atelie.repositories.UsuarioRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RelatorioService {

    private static final List<StatusEncomenda> STATUS_VALIDOS = List.of(StatusEncomenda.EM_ANDAMENTO, StatusEncomenda.FINALIZADO);
    private static final StatusEncomenda STATUS_FINALIZADO = StatusEncomenda.FINALIZADO;
    private static final int LIMITE_PADRAO_RANKING_CLIENTES = 10;

    @Autowired
    private EncomendaRepository encomendaRepository;

    @Autowired
    private ItemEncomendaRepository itemEncomendaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public IndicadoresPeriodoDTO buscarIndicadores(PeriodoTipo periodo, LocalDate dataReferencia, LocalDate dataInicio, LocalDate dataFim) {
        Long usuarioId = usuarioLogado().getIdUsuario();

        LocalDate[][] limites = resolverLimites(periodo, dataReferencia, dataInicio, dataFim);
        LocalDate[] atual = limites[0];
        LocalDate[] anterior = limites[1];

        IndicadoresBrutoDTO brutoAtual = encomendaRepository.buscarIndicadores(usuarioId, STATUS_VALIDOS, atual[0], atual[1]);
        IndicadoresBrutoDTO brutoAnterior = encomendaRepository.buscarIndicadores(usuarioId, STATUS_VALIDOS, anterior[0], anterior[1]);

        Long novosClientesAtual = encomendaRepository.contarNovosClientes(usuarioId, STATUS_VALIDOS, atual[0], atual[1]);
        Long novosClientesAnterior = encomendaRepository.contarNovosClientes(usuarioId, STATUS_VALIDOS, anterior[0], anterior[1]);

        BigDecimal ticketMedioAtual = calcularTicketMedio(brutoAtual.getFaturamento(), brutoAtual.getTotalPedidos());
        BigDecimal ticketMedioAnterior = calcularTicketMedio(brutoAnterior.getFaturamento(), brutoAnterior.getTotalPedidos());

        IndicadoresPeriodoDTO dto = new IndicadoresPeriodoDTO();
        dto.setFaturamento(brutoAtual.getFaturamento());
        dto.setFaturamentoVariacaoPercentual(variacaoPercentual(brutoAtual.getFaturamento(), brutoAnterior.getFaturamento()));
        dto.setTotalPedidos(brutoAtual.getTotalPedidos());
        dto.setTotalPedidosVariacaoPercentual(variacaoPercentual(
                BigDecimal.valueOf(brutoAtual.getTotalPedidos()), BigDecimal.valueOf(brutoAnterior.getTotalPedidos())));
        dto.setNovosClientes(novosClientesAtual);
        dto.setNovosClientesVariacaoPercentual(variacaoPercentual(
                BigDecimal.valueOf(novosClientesAtual), BigDecimal.valueOf(novosClientesAnterior)));
        dto.setTicketMedio(ticketMedioAtual);
        dto.setTicketMedioVariacaoPercentual(variacaoPercentual(ticketMedioAtual, ticketMedioAnterior));

        return dto;
    }

    public List<FaturamentoMensalDTO> buscarFaturamentoMensal(Integer ano) {
        Long usuarioId = usuarioLogado().getIdUsuario();
        int anoAlvo = ano != null ? ano : LocalDate.now().getYear();
        LocalDate inicioAno = LocalDate.of(anoAlvo, 1, 1);
        LocalDate fimAno = LocalDate.of(anoAlvo, 12, 31);

        List<FaturamentoMensalBrutoDTO> bruto = encomendaRepository.buscarParaFaturamentoMensal(
                usuarioId, STATUS_VALIDOS, inicioAno, fimAno);

        Map<Integer, List<FaturamentoMensalBrutoDTO>> porMes = bruto.stream()
                .collect(Collectors.groupingBy(item -> item.getDataPedido().getMonthValue()));

        LocalDate inicioMesCorrente = LocalDate.now().withDayOfMonth(1);

        List<FaturamentoMensalDTO> resultado = new ArrayList<>();
        for (int mes = 1; mes <= 12; mes++) {
            List<FaturamentoMensalBrutoDTO> itensDoMes = porMes.getOrDefault(mes, List.of());

            BigDecimal valorFinalizado = somarValores(itensDoMes, true);
            BigDecimal valorEmAndamento = somarValores(itensDoMes, false);
            BigDecimal valorTotal = valorFinalizado.add(valorEmAndamento);

            TipoValor tipo;
            if (!itensDoMes.isEmpty()) {
                tipo = valorEmAndamento.compareTo(BigDecimal.ZERO) == 0 ? TipoValor.REALIZADO : TipoValor.ESTIMADO;
            } else {
                boolean mesJaEncerrado = LocalDate.of(anoAlvo, mes, 1).isBefore(inicioMesCorrente);
                tipo = mesJaEncerrado ? TipoValor.REALIZADO : TipoValor.ESTIMADO;
            }

            resultado.add(new FaturamentoMensalDTO(mes, anoAlvo, valorTotal, tipo));
        }

        return resultado;
    }

    public List<RankingProdutoDTO> buscarRankingProdutos(PeriodoTipo periodo, LocalDate dataReferencia, LocalDate dataInicio, LocalDate dataFim) {
        Long usuarioId = usuarioLogado().getIdUsuario();
        LocalDate[] atual = resolverLimites(periodo, dataReferencia, dataInicio, dataFim)[0];

        List<RankingProdutoDTO> ranking = itemEncomendaRepository.rankingProdutos(usuarioId, STATUS_VALIDOS, atual[0], atual[1]);

        BigDecimal receitaTotal = ranking.stream()
                .map(RankingProdutoDTO::getReceita)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int posicao = 1;
        for (RankingProdutoDTO item : ranking) {
            item.setPosicao(posicao++);
            item.setPercentualReceita(percentual(item.getReceita(), receitaTotal));
        }

        return ranking;
    }

    public List<RankingClienteRelatorioDTO> buscarRankingClientes(PeriodoTipo periodo, LocalDate dataReferencia, Integer limite, LocalDate dataInicio, LocalDate dataFim) {
        Long usuarioId = usuarioLogado().getIdUsuario();
        LocalDate[] atual = resolverLimites(periodo, dataReferencia, dataInicio, dataFim)[0];
        int limiteResultados = limite != null && limite > 0 ? limite : LIMITE_PADRAO_RANKING_CLIENTES;

        return encomendaRepository.rankingClientes(
                usuarioId, STATUS_VALIDOS, atual[0], atual[1], PageRequest.of(0, limiteResultados));
    }

    private Usuario usuarioLogado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email).orElseThrow();
    }

    private BigDecimal somarValores(List<FaturamentoMensalBrutoDTO> itens, boolean finalizados) {
        return itens.stream()
                .filter(item -> STATUS_FINALIZADO.equals(item.getStatus()) == finalizados)
                .map(FaturamentoMensalBrutoDTO::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private LocalDate[][] resolverLimites(PeriodoTipo periodo, LocalDate dataReferencia, LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio != null && dataFim != null) {
            if (dataInicio.isAfter(dataFim)) {
                throw new IllegalArgumentException("A data inicial não pode ser posterior à data final.");
            }
            LocalDate[] atual = {dataInicio, dataFim};
            long dias = ChronoUnit.DAYS.between(dataInicio, dataFim) + 1;
            LocalDate fimAnterior = dataInicio.minusDays(1);
            LocalDate inicioAnterior = fimAnterior.minusDays(dias - 1);
            return new LocalDate[][]{atual, {inicioAnterior, fimAnterior}};
        }

        if (periodo == null) {
            throw new IllegalArgumentException("Informe um período ou um intervalo de datas personalizado.");
        }

        LocalDate referencia = dataReferencia != null ? dataReferencia : LocalDate.now();
        return new LocalDate[][]{limitesPeriodo(periodo, referencia), limitesPeriodoAnterior(periodo, referencia)};
    }

    private LocalDate[] limitesPeriodo(PeriodoTipo periodo, LocalDate referencia) {
        return switch (periodo) {
            case SEMANA -> {
                LocalDate inicio = referencia.with(ChronoField.DAY_OF_WEEK, 1);
                yield new LocalDate[]{inicio, inicio.plusDays(6)};
            }
            case MES -> {
                LocalDate inicio = referencia.withDayOfMonth(1);
                yield new LocalDate[]{inicio, inicio.plusMonths(1).minusDays(1)};
            }
            case TRIMESTRE -> {
                int mesInicioTrimestre = ((referencia.getMonthValue() - 1) / 3) * 3 + 1;
                LocalDate inicio = LocalDate.of(referencia.getYear(), mesInicioTrimestre, 1);
                yield new LocalDate[]{inicio, inicio.plusMonths(3).minusDays(1)};
            }
            case SEMESTRE -> {
                int mesInicioSemestre = ((referencia.getMonthValue() - 1) / 6) * 6 + 1;
                LocalDate inicio = LocalDate.of(referencia.getYear(), mesInicioSemestre, 1);
                yield new LocalDate[]{inicio, inicio.plusMonths(6).minusDays(1)};
            }
            case ANO -> {
                LocalDate inicio = LocalDate.of(referencia.getYear(), 1, 1);
                yield new LocalDate[]{inicio, LocalDate.of(referencia.getYear(), 12, 31)};
            }
        };
    }

    private LocalDate[] limitesPeriodoAnterior(PeriodoTipo periodo, LocalDate referencia) {
        LocalDate inicioAtual = limitesPeriodo(periodo, referencia)[0];
        LocalDate referenciaAnterior = switch (periodo) {
            case SEMANA -> inicioAtual.minusWeeks(1);
            case MES -> inicioAtual.minusMonths(1);
            case TRIMESTRE -> inicioAtual.minusMonths(3);
            case SEMESTRE -> inicioAtual.minusMonths(6);
            case ANO -> inicioAtual.minusYears(1);
        };
        return limitesPeriodo(periodo, referenciaAnterior);
    }

    private BigDecimal calcularTicketMedio(BigDecimal faturamento, Long totalPedidos) {
        if (totalPedidos == null || totalPedidos == 0) {
            return BigDecimal.ZERO;
        }
        return faturamento.divide(BigDecimal.valueOf(totalPedidos), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal variacaoPercentual(BigDecimal atual, BigDecimal anterior) {
        if (anterior == null || anterior.compareTo(BigDecimal.ZERO) == 0) {
            return atual != null && atual.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : null;
        }
        return atual.subtract(anterior)
                .divide(anterior, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal percentual(BigDecimal parte, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return parte.divide(total, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
