package tcc.meu_atelie.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tcc.meu_atelie.dto.FornecedorDTO;
import tcc.meu_atelie.forms.FornecedorForm;
import tcc.meu_atelie.services.FornecedorService;

import java.util.List;

@RestController
@RequestMapping("/fornecedores")
public class FornecedorController {

    @Autowired
    private FornecedorService fornecedorService;

    @GetMapping("/listar")
    public ResponseEntity<List<FornecedorDTO>> listar(Authentication authentication) {
        String emailLogado = authentication.getName();
        List<FornecedorDTO> fornecedores = fornecedorService.listarPorUsuario(emailLogado);
        return ResponseEntity.ok(fornecedores);
    }

    @PostMapping("/cadastrar")
    public ResponseEntity<FornecedorDTO> criar(@RequestBody FornecedorForm form, Authentication authentication) {
        String emailLogado = authentication.getName();
        FornecedorDTO novoFornecedor = fornecedorService.salvar(form, emailLogado);

        return ResponseEntity.status(HttpStatus.CREATED).body(novoFornecedor);
    }
}