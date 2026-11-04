package tcc.meu_atelie.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tcc.meu_atelie.dto.EnderecoDTO;
import tcc.meu_atelie.dto.LojaDTO;
import tcc.meu_atelie.models.Endereco;
import tcc.meu_atelie.models.Loja;
import tcc.meu_atelie.services.LojaService;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/perfil")
@RequiredArgsConstructor
public class LojaController {
    private final LojaService lojaService;

    @GetMapping("/{idUsuario}")
    public ResponseEntity<LojaDTO> exibirPerfil(@PathVariable Long idUsuario, Principal principal) {
        String emailDoToken = principal.getName();
        Loja loja = lojaService.buscarPerfil(idUsuario, emailDoToken);

        LojaDTO dto = new LojaDTO(
                loja.getIdLoja(),
                loja.getNome(),
                emailDoToken,
                loja.getDescricao(),
                loja.getCnpj(),
                loja.getWhatsapp(),
                loja.getFacebook(),
                loja.getInstagram(),
                loja.getUsuario().getIdUsuario(),
                loja.getUsuario().getNome(),
                loja.getFotoUrl(),
                mapearEndereco(loja.getEndereco())
        );

        return ResponseEntity.ok(dto);
    }

    private EnderecoDTO mapearEndereco(Endereco endereco) {
        if (endereco == null) {
            return null;
        }
        EnderecoDTO dto = new EnderecoDTO();
        dto.setCep(endereco.getCep());
        dto.setRua(endereco.getRua());
        dto.setNumero(endereco.getNumero());
        dto.setComplemento(endereco.getComplemento());
        dto.setBairro(endereco.getBairro());
        dto.setCidade(endereco.getCidade());
        dto.setEstado(endereco.getEstado());
        return dto;
    }

    @PostMapping("/{idUsuario}/foto")
    public ResponseEntity<?> atualizarFoto(@PathVariable Long idUsuario, @RequestParam("arquivo") MultipartFile arquivo, Principal principal) {
        String nomeArquivo = lojaService.salvarFoto(idUsuario, arquivo, principal.getName());
        return ResponseEntity.ok(Map.of("fotoUrl", nomeArquivo));
    }

    @PutMapping("/{idUsuario}")
    public ResponseEntity<LojaDTO> editarPerfil(@PathVariable Long idUsuario, @RequestBody LojaDTO dto, Principal principal) {
        Loja dadosAtualizados = new Loja();
        dadosAtualizados.setNome(dto.getNome());
        dadosAtualizados.setDescricao(dto.getDescricao());
        dadosAtualizados.setWhatsapp(dto.getWhatsapp());
        dadosAtualizados.setFacebook(dto.getFacebook());
        dadosAtualizados.setInstagram(dto.getInstagram());

        if (dto.getEndereco() != null) {
            Endereco endereco = new Endereco();
            endereco.setCep(dto.getEndereco().getCep() != null ? dto.getEndereco().getCep().replaceAll("\\D", "") : null);
            endereco.setRua(dto.getEndereco().getRua());
            endereco.setNumero(dto.getEndereco().getNumero());
            endereco.setComplemento(dto.getEndereco().getComplemento());
            endereco.setBairro(dto.getEndereco().getBairro());
            endereco.setCidade(dto.getEndereco().getCidade());
            endereco.setEstado(dto.getEndereco().getEstado());
            dadosAtualizados.setEndereco(endereco);
        }

        String emailDoToken = principal.getName();
        Loja lojaSalva = lojaService.atualizarPerfil(idUsuario, dadosAtualizados, emailDoToken);

        LojaDTO resultadoDto = new LojaDTO(
                lojaSalva.getIdLoja(),
                lojaSalva.getNome(),
                emailDoToken,
                lojaSalva.getDescricao(),
                lojaSalva.getCnpj(),
                lojaSalva.getWhatsapp(),
                lojaSalva.getFacebook(),
                lojaSalva.getInstagram(),
                lojaSalva.getUsuario().getIdUsuario(),
                lojaSalva.getUsuario().getNome(),
                lojaSalva.getFotoUrl(),
                mapearEndereco(lojaSalva.getEndereco())
        );

        return ResponseEntity.ok(resultadoDto);
    }
}
