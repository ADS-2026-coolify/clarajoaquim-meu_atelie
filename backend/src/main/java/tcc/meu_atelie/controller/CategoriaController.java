package tcc.meu_atelie.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tcc.meu_atelie.dto.CategoriaDTO;
import tcc.meu_atelie.models.Categoria;
import tcc.meu_atelie.models.Usuario;
import tcc.meu_atelie.repositories.UsuarioRepository;
import tcc.meu_atelie.services.CategoriaService;

import java.util.List;

@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping
    public ResponseEntity<CategoriaDTO> cadastrar(@RequestBody CategoriaDTO dto, Authentication authentication) {
        String emailUsuario = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoriaService.cadastrar(dto, usuario));
    }

    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> listar(Authentication authentication) {
        String emailUsuario = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return ResponseEntity.ok(categoriaService.listarPorUsuario(usuario.getIdUsuario()));
    }
}
