package tcc.meu_atelie.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tcc.meu_atelie.dto.DashboardDTO;
import tcc.meu_atelie.dto.EncomendaDTO;
import tcc.meu_atelie.enums.StatusEncomenda;
import tcc.meu_atelie.models.Encomenda;
import tcc.meu_atelie.services.EncomendaService;
import tcc.meu_atelie.services.ReciboService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/encomendas")
@RequiredArgsConstructor
public class EncomendaController {

    private final EncomendaService encomendaService;
    private final ReciboService reciboService;

    @PostMapping
    public ResponseEntity<?> cadastrarEncomenda(@RequestBody EncomendaDTO dto) {
        try {
            EncomendaDTO encomendaSalva = encomendaService.cadastrar(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(encomendaSalva.getId());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<EncomendaDTO>> listarTodas() {
        return ResponseEntity.ok(encomendaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(encomendaService.buscarPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/status")
    public List<StatusEncomenda> listarStatus() {
        return Arrays.asList(StatusEncomenda.values());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarEncomenda(@PathVariable Long id, @RequestBody EncomendaDTO dto) {
        try {
            EncomendaDTO encomendaAtualizada = encomendaService.atualizar(id, dto);
            return ResponseEntity.ok(encomendaAtualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        try {
            encomendaService.deletar(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDTO> obterDashboard() {
        return ResponseEntity.ok(encomendaService.obterDadosDashboard());
    }

    @GetMapping("/{id}/recibo")
    public ResponseEntity<?> gerarRecibo(@PathVariable Long id) {
        try {
            byte[] pdf = reciboService.gerarReciboPdf(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=recibo-" + id + ".pdf")
                    .body(pdf);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }
}