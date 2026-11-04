package tcc.meu_atelie.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tcc.meu_atelie.dto.ClienteDetalheDTO;
import tcc.meu_atelie.dto.ClienteResumoDTO;
import tcc.meu_atelie.forms.ClienteForm;
import tcc.meu_atelie.models.CanalAquisicao;
import tcc.meu_atelie.services.ClienteService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping("/resumo")
    public ResponseEntity<List<ClienteResumoDTO>> listarResumo(Authentication authentication) {
        String emailLogado = authentication.getName();
        List<ClienteResumoDTO> resumo = clienteService.listarResumoClientesPorUsuario(emailLogado);
        return ResponseEntity.ok(resumo);
    }

    @GetMapping("/canal-aquisicao")
    public ResponseEntity<List<CanalAquisicao>> listarCanais() {
        return ResponseEntity.ok(clienteService.listarCanais());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteDetalheDTO> buscarPorId(@PathVariable Long id, Authentication authentication) {
        String emailLogado = authentication.getName();
        return ResponseEntity.ok(clienteService.buscarClientePorId(id, emailLogado));
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<ClienteResumoDTO> salvarCliente(@RequestBody ClienteForm dto, Authentication authentication) {
        String emailLogado = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clienteService.salvarCliente(dto, emailLogado));
    }

    @PutMapping("/editar/{id}")
    public ResponseEntity<ClienteResumoDTO> atualizarCliente(@PathVariable Long id, @RequestBody ClienteForm dto, Authentication authentication) {
        String emailLogado = authentication.getName();
        return ResponseEntity.ok(clienteService.atualizarCliente(id, dto, emailLogado));
    }

    @GetMapping("/inativos")
    public ResponseEntity<List<ClienteResumoDTO>> listarInativos(Authentication authentication) {
        String emailLogado = authentication.getName();
        return ResponseEntity.ok(clienteService.listarClientesInativos(emailLogado));
    }

    @DeleteMapping("desativar/{id}")
    public ResponseEntity<Void> desativarCliente(@PathVariable Long id, Authentication authentication) {
        String emailLogado = authentication.getName();
        clienteService.desativarCliente(id, emailLogado);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reativar")
    public ResponseEntity<Void> reativarCliente(@PathVariable Long id, Authentication authentication) {
        String emailLogado = authentication.getName();
        clienteService.reativarCliente(id, emailLogado);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarCliente(@PathVariable Long id, Authentication authentication) {
        String emailLogado = authentication.getName();
        try {
            clienteService.deletarPermanentemente(id, emailLogado);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
    }
}