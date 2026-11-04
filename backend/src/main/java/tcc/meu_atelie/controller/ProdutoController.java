package tcc.meu_atelie.controller;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tcc.meu_atelie.dto.ProdutoDTO;
import tcc.meu_atelie.forms.ProdutoForm;
import tcc.meu_atelie.services.ProdutoService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProdutoDTO> cadastrar(
            @Valid @RequestPart("produto") ProdutoForm form,
            @RequestPart(value = "imagem", required = false) MultipartFile imagem,
            Authentication authentication) {

        String emailLogado = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(produtoService.cadastrar(form, imagem, emailLogado));
    }

    @GetMapping
    public ResponseEntity<List<ProdutoDTO>> listarPorUsuario() {
        return ResponseEntity.ok(produtoService.listarProdutosDoUsuarioLogado());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProdutoDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestPart("produto") ProdutoForm form,
            @RequestPart(value = "imagem", required = false) MultipartFile imagem,
            Authentication authentication) {
        String emailLogado = authentication.getName();
        return ResponseEntity.ok(produtoService.atualizar(id, form, imagem, emailLogado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoDTO> buscarPorId(@PathVariable Long id, Authentication authentication) {
        String emailLogado = authentication.getName();
        return ResponseEntity.ok(produtoService.buscarPorId(id, emailLogado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id, Authentication authentication) {
        String emailLogado = authentication.getName();
        try {
            produtoService.deletar(id, emailLogado);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
    }
}
