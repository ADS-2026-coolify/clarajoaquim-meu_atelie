package tcc.meu_atelie.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tcc.meu_atelie.dto.FornecedorDTO;
import tcc.meu_atelie.forms.FornecedorForm;
import tcc.meu_atelie.models.Fornecedor;
import tcc.meu_atelie.models.Usuario;
import tcc.meu_atelie.repositories.FornecedorRepository;
import tcc.meu_atelie.repositories.UsuarioRepository;

import java.util.List;

@Service
public class FornecedorService {
    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<FornecedorDTO> listarPorUsuario(String email) {
        List<Fornecedor> fornecedores = fornecedorRepository.findByUsuarioEmail(email);
        return fornecedores.stream()
                .map(FornecedorDTO::new)
                .toList();
    }

    public FornecedorDTO salvar(FornecedorForm form, String emailUsuario) {
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Fornecedor f = new Fornecedor();
        f.setNome(form.getNome());
        f.setCnpjCpf(form.getCnpjCpf());
        f.setTelefone(form.getTelefone());
        f.setEmail(form.getEmail());
        f.setUsuario(usuario);

        return new FornecedorDTO(fornecedorRepository.save(f));
    }
}
