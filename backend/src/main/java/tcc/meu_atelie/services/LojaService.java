package tcc.meu_atelie.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tcc.meu_atelie.models.Endereco;
import tcc.meu_atelie.models.Loja;
import tcc.meu_atelie.repositories.LojaRepository;

import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LojaService {

    private final LojaRepository lojaRepository;

    public Loja buscarPerfil(Long idUsuario, String emailLogado) {
        Loja loja = lojaRepository.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new RuntimeException("Perfil da loja não encontrado."));

        if (!loja.getUsuario().getEmail().equals(emailLogado)) {
            throw new RuntimeException("Acesso negado");
        }

        return loja;
    }

    public Loja atualizarPerfil(Long idUsuario, Loja dadosAtualizados, String emailLogado) {
        Loja loja = buscarPerfil(idUsuario, emailLogado);

        loja.setNome(dadosAtualizados.getNome());
        loja.setDescricao(dadosAtualizados.getDescricao());
        loja.setWhatsapp(dadosAtualizados.getWhatsapp());
        loja.setFacebook(dadosAtualizados.getFacebook());
        loja.setInstagram(dadosAtualizados.getInstagram());

        if (dadosAtualizados.getEndereco() != null) {
            Endereco endereco = loja.getEndereco();
            if (endereco == null) {
                endereco = new Endereco();
                endereco.setLoja(loja);
                loja.setEndereco(endereco);
            }
            Endereco novoEndereco = dadosAtualizados.getEndereco();
            endereco.setCep(novoEndereco.getCep());
            endereco.setRua(novoEndereco.getRua());
            endereco.setNumero(novoEndereco.getNumero());
            endereco.setComplemento(novoEndereco.getComplemento());
            endereco.setBairro(novoEndereco.getBairro());
            endereco.setCidade(novoEndereco.getCidade());
            endereco.setEstado(novoEndereco.getEstado());
        }

        return lojaRepository.save(loja);
    }

    public String salvarFoto(Long idUsuario, MultipartFile arquivo, String emailLogado) {
        try {
            String nomeArquivo = UUID.randomUUID().toString() + "_" + arquivo.getOriginalFilename();

            Path diretorio = Paths.get("uploads");
            if (!Files.exists(diretorio)) {
                Files.createDirectories(diretorio);
            }

            Path caminhoArquivo = diretorio.resolve(nomeArquivo);
            Files.copy(arquivo.getInputStream(), caminhoArquivo);

            Loja loja = buscarPerfil(idUsuario, emailLogado);
            loja.setFotoUrl(nomeArquivo);
            lojaRepository.save(loja);

            return nomeArquivo;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar a foto", e);
        }
    }
}
