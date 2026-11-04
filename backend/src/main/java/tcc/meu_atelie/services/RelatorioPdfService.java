package tcc.meu_atelie.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;
import tcc.meu_atelie.dto.IndicadoresPeriodoDTO;
import tcc.meu_atelie.dto.PeriodoTipo;
import tcc.meu_atelie.dto.RankingClienteRelatorioDTO;
import tcc.meu_atelie.dto.RankingProdutoDTO;
import tcc.meu_atelie.models.Loja;
import tcc.meu_atelie.models.Usuario;
import tcc.meu_atelie.repositories.LojaRepository;
import tcc.meu_atelie.repositories.UsuarioRepository;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RelatorioPdfService {

    private static final Map<PeriodoTipo, String> ROTULOS_PERIODO = Map.of(
            PeriodoTipo.SEMANA, "Semana",
            PeriodoTipo.MES, "Mês",
            PeriodoTipo.TRIMESTRE, "Trimestre",
            PeriodoTipo.SEMESTRE, "Semestre",
            PeriodoTipo.ANO, "Ano"
    );

    private final RelatorioService relatorioService;
    private final LojaRepository lojaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TemplateEngine templateEngine;

    public byte[] gerarRelatorioPdf(PeriodoTipo periodo, LocalDate data, LocalDate dataInicio, LocalDate dataFim) {
        String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByEmail(emailLogado).orElseThrow();
        Loja loja = lojaRepository.findByUsuarioIdUsuario(usuario.getIdUsuario()).orElse(null);

        IndicadoresPeriodoDTO indicadores = relatorioService.buscarIndicadores(periodo, data, dataInicio, dataFim);
        List<RankingProdutoDTO> rankingProdutos = relatorioService.buscarRankingProdutos(periodo, data, dataInicio, dataFim);
        List<RankingClienteRelatorioDTO> rankingClientes = relatorioService.buscarRankingClientes(periodo, data, 10, dataInicio, dataFim);

        DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String rotuloPeriodo = dataInicio != null && dataFim != null
                ? "de " + dataInicio.format(formatoData) + " a " + dataFim.format(formatoData)
                : ROTULOS_PERIODO.getOrDefault(periodo, periodo != null ? periodo.name() : "");

        Context context = new Context();
        context.setVariable("nomeLoja", loja != null && loja.getNome() != null ? loja.getNome() : usuario.getNome());
        context.setVariable("rotuloPeriodo", rotuloPeriodo);
        context.setVariable("dataEmissao", LocalDate.now().format(formatoData));

        context.setVariable("faturamento", formatarMoeda(indicadores.getFaturamento()));
        context.setVariable("totalPedidos", indicadores.getTotalPedidos());
        context.setVariable("novosClientes", indicadores.getNovosClientes());
        context.setVariable("ticketMedio", formatarMoeda(indicadores.getTicketMedio()));

        context.setVariable("rankingProdutos", rankingProdutos);
        context.setVariable("rankingClientes", rankingClientes);

        String html = templateEngine.process("relatorio", context);

        return converterParaPdf(html);
    }

    private byte[] converterParaPdf(String html) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF do relatório", e);
        }
    }

    private String formatarMoeda(BigDecimal valor) {
        NumberFormat formato = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
        return formato.format(valor != null ? valor : BigDecimal.ZERO);
    }
}
