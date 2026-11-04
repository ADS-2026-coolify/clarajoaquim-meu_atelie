package tcc.meu_atelie.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import tcc.meu_atelie.dto.MaterialDTO;
import tcc.meu_atelie.forms.MaterialForm;
import tcc.meu_atelie.models.Fornecedor;
import tcc.meu_atelie.models.Material;
import tcc.meu_atelie.models.Usuario;
import tcc.meu_atelie.repositories.FornecedorRepository;
import tcc.meu_atelie.repositories.MaterialRepository;
import tcc.meu_atelie.repositories.ProdutoMaterialRepository;
import tcc.meu_atelie.repositories.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MaterialService {
    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProdutoMaterialRepository produtoMaterialRepository;

    public MaterialDTO cadastrar(MaterialForm form, String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Material m = new Material();
        m.setNome(form.getNome());
        m.setCategoria(form.getCategoria());
        m.setCor(form.getCor());
        m.setQuantidadeEstoque(form.getQuantidadeEstoque());
        m.setUnidadeMedida(form.getUnidadeMedida());
        m.setCodigoReferencia(form.getCodigoReferencia());
        m.setMarca(form.getMarca());
        m.setEstoqueMinimo(form.getEstoqueMinimo());
        m.setPrecoUnitario(form.getPrecoUnitario());
        m.setDataUltimaCompra(form.getDataUltimaCompra());
        m.setAnotacoes(form.getAnotacoes());
        m.setStatus(form.getStatus() != null ? form.getStatus() : "disponivel");
        m.setUsuario(usuario);

        if (form.getFornecedorId() != null) {
            Fornecedor f = fornecedorRepository.findByIdAndUsuarioEmail(form.getFornecedorId(), email)
                    .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado"));
            m.setFornecedor(f);
        }
        Material salvo = materialRepository.save(m);
        return new MaterialDTO(salvo);
    }

    public List<MaterialDTO> listarPorUsuario(String email) {
        List<Material> materiais = materialRepository.findByUsuarioEmail(email);

        return materiais.stream()
                .map(MaterialDTO::new)
                .toList(); // Se estiver no Java 17+, ou .collect(Collectors.toList())
    }

    public MaterialDTO atualizar(Long id, MaterialForm form, String email) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material não encontrado"));

        if (!material.getUsuario().getEmail().equals(email)) {
            throw new RuntimeException("Acesso negado: Este material não pertence ao seu ateliê.");
        }

        material.setNome(form.getNome());
        material.setCategoria(form.getCategoria());
        material.setCor(form.getCor());
        material.setQuantidadeEstoque(form.getQuantidadeEstoque());
        material.setUnidadeMedida(form.getUnidadeMedida());
        material.setCodigoReferencia(form.getCodigoReferencia());
        material.setMarca(form.getMarca());
        material.setEstoqueMinimo(form.getEstoqueMinimo());
        material.setPrecoUnitario(form.getPrecoUnitario());
        material.setDataUltimaCompra(form.getDataUltimaCompra());
        material.setAnotacoes(form.getAnotacoes());
        if (form.getStatus() != null) {
            material.setStatus(form.getStatus());
        }

        if (form.getFornecedorId() != null) {
            Fornecedor f = fornecedorRepository.findByIdAndUsuarioEmail(form.getFornecedorId(), email)
                    .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado"));
            material.setFornecedor(f);
        }

        Material materialSalvo = materialRepository.save(material);
        return new MaterialDTO(materialSalvo);
    }

    public void deletar(Long id, String email) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material não encontrado"));

        if (!material.getUsuario().getEmail().equals(email)) {
            throw new RuntimeException("Acesso negado.");
        }

        if (produtoMaterialRepository.existsByMaterialId(id)) {
            throw new IllegalStateException(
                    "Este material está vinculado a um ou mais produtos e não pode ser excluído. Remova-o dos produtos antes de excluir.");
        }

        try {
            materialRepository.delete(material);
        } catch (OptimisticLockingFailureException e) {
            throw new IllegalStateException("Este material já foi excluído.");
        }
    }

    public Map<String, Object> obterResumo(String email) {
        List<Material> materiais = materialRepository.findByUsuarioEmail(email);
        List<MaterialDTO> listaBaixo = new ArrayList<>();
        int baixo = 0;

        for (Material m : materiais) {
            if (m.getEstoqueMinimo() != null && m.getQuantidadeEstoque().compareTo(m.getEstoqueMinimo()) <= 0) {
                baixo++;
                listaBaixo.add(new MaterialDTO(m));
            }
        }

        return Map.of(
                "totalItens", materiais.size(),
                "estoqueNormal", materiais.size() - baixo,
                "estoqueBaixo", baixo,
                "listaEstoqueBaixo", listaBaixo
        );
    }
}