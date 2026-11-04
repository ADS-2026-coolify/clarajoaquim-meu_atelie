package tcc.meu_atelie.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tcc.meu_atelie.dto.FaturamentoMensalDTO;
import tcc.meu_atelie.dto.IndicadoresPeriodoDTO;
import tcc.meu_atelie.dto.PeriodoTipo;
import tcc.meu_atelie.dto.RankingClienteRelatorioDTO;
import tcc.meu_atelie.dto.RankingProdutoDTO;
import tcc.meu_atelie.services.RelatorioPdfService;
import tcc.meu_atelie.services.RelatorioService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/relatorios")
public class RelatorioController {

    @Autowired
    private RelatorioService relatorioService;

    @Autowired
    private RelatorioPdfService relatorioPdfService;

    @GetMapping("/indicadores")
    public ResponseEntity<?> buscarIndicadores(
            @RequestParam(required = false) PeriodoTipo periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        try {
            return ResponseEntity.ok(relatorioService.buscarIndicadores(periodo, data, dataInicio, dataFim));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/faturamento-mensal")
    public ResponseEntity<List<FaturamentoMensalDTO>> buscarFaturamentoMensal(
            @RequestParam(required = false) Integer ano) {
        return ResponseEntity.ok(relatorioService.buscarFaturamentoMensal(ano));
    }

    @GetMapping("/ranking-produtos")
    public ResponseEntity<?> buscarRankingProdutos(
            @RequestParam(required = false) PeriodoTipo periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        try {
            return ResponseEntity.ok(relatorioService.buscarRankingProdutos(periodo, data, dataInicio, dataFim));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/ranking-clientes")
    public ResponseEntity<?> buscarRankingClientes(
            @RequestParam(required = false) PeriodoTipo periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) Integer limite,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        try {
            return ResponseEntity.ok(relatorioService.buscarRankingClientes(periodo, data, limite, dataInicio, dataFim));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/pdf")
    public ResponseEntity<?> gerarRelatorioPdf(
            @RequestParam(required = false) PeriodoTipo periodo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {
        try {
            byte[] pdf = relatorioPdfService.gerarRelatorioPdf(periodo, data, dataInicio, dataFim);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio.pdf")
                    .body(pdf);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }
}
