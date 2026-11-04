package tcc.meu_atelie.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tcc.meu_atelie.dto.MaterialDTO;
import tcc.meu_atelie.forms.MaterialForm;
import tcc.meu_atelie.services.MaterialService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/materiais")
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    @GetMapping("/listar")
    public ResponseEntity<List<MaterialDTO>> listar(Authentication authentication) {
        String emailLogado = authentication.getName();
        List<MaterialDTO> materiais = materialService.listarPorUsuario(emailLogado);
        return ResponseEntity.ok(materiais);
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<MaterialDTO> cadastrar(@Valid @RequestBody MaterialForm form, Authentication authentication) {
        String emailLogado = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(materialService.cadastrar(form, emailLogado));
    }

    @PutMapping("/atualizar/{id}")
    public ResponseEntity<MaterialDTO> atualizar(@PathVariable Long id, @Valid @RequestBody MaterialForm form, Authentication authentication) {
        String emailLogado = authentication.getName();
        return ResponseEntity.ok(materialService.atualizar(id, form, emailLogado));
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id, Authentication authentication) {
        String emailLogado = authentication.getName();
        try {
            materialService.deletar(id, emailLogado);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/resumo")
    public ResponseEntity<Map<String, Object>> obterResumo(Authentication authentication) {
        return ResponseEntity.ok(materialService.obterResumo(authentication.getName()));
    }
}