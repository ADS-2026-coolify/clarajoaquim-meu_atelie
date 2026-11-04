package tcc.meu_atelie.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tcc.meu_atelie.dto.ProdutoDTO;
import tcc.meu_atelie.dto.ProdutoMaterialDTO;
import tcc.meu_atelie.forms.ProdutoForm;
import tcc.meu_atelie.forms.ProdutoMaterialForm;
import tcc.meu_atelie.models.Categoria;
import tcc.meu_atelie.models.Material;
import tcc.meu_atelie.models.Produto;
import tcc.meu_atelie.models.ProdutoMaterial;
import tcc.meu_atelie.models.Usuario;
import tcc.meu_atelie.repositories.CategoriaRepository;
import tcc.meu_atelie.repositories.ItemEncomendaRepository;
import tcc.meu_atelie.repositories.MaterialRepository;
import tcc.meu_atelie.repositories.ProdutoMaterialRepository;
import tcc.meu_atelie.repositories.ProdutoRepository;
import tcc.meu_atelie.repositories.UsuarioRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProdutoService {

    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private MaterialRepository materialRepository;
    @Autowired private ItemEncomendaRepository itemEncomendaRepository;
    @Autowired private ProdutoMaterialRepository produtoMaterialRepository;

    @Transactional
    public ProdutoDTO cadastrar(ProdutoForm form, MultipartFile arquivoImagem, String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Produto produto = new Produto();
        produto.setNome(form.getNome());
        produto.setDescricao(form.getDescricao());
        produto.setPreco(form.getPreco());
        produto.setPrecoCusto(form.getPrecoCusto());
        produto.setQuantidadeEstoque(form.getQuantidadeEstoque());
        produto.setStatus(form.getStatus());
        produto.setLargura(form.getLargura());
        produto.setAltura(form.getAltura());
        produto.setObservacoes(form.getObservacoes());
        produto.setUsuario(usuario);

        Categoria categoria = categoriaRepository.findByIdAndUsuarioIdUsuario(form.getCategoriaId(), usuario.getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
        produto.setCategoria(categoria);

        aplicarImagem(produto, arquivoImagem);

        Produto produtoSalvo = produtoRepository.save(produto);

        List<ProdutoMaterial> produtoMateriais = montarProdutoMateriais(produtoSalvo, form.getMateriais(), email);
        produtoMaterialRepository.saveAll(produtoMateriais);
        produtoSalvo.setProdutoMateriais(produtoMateriais);

        return converterParaDTO(produtoSalvo);
    }

    public List<ProdutoDTO> listarProdutosDoUsuarioLogado() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado no token"));

        return produtoRepository.findByUsuarioIdUsuario(usuario.getIdUsuario())
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    private ProdutoDTO converterParaDTO(Produto produto) {
        ProdutoDTO dto = new ProdutoDTO();
        dto.setId(produto.getId());
        dto.setNome(produto.getNome());
        dto.setDescricao(produto.getDescricao());
        dto.setPreco(produto.getPreco());
        dto.setPrecoCusto(produto.getPrecoCusto());
        dto.setQuantidadeEstoque(produto.getQuantidadeEstoque());
        dto.setStatus(produto.getStatus());
        dto.setLargura(produto.getLargura());
        dto.setAltura(produto.getAltura());
        dto.setObservacoes(produto.getObservacoes());
        dto.setImagem(produto.getImagem());

        if (produto.getCategoria() != null) {
            dto.setCategoriaId(produto.getCategoria().getId());
            dto.setNomeCategoria(produto.getCategoria().getNome());
        }

        if (produto.getProdutoMateriais() != null) {
            List<ProdutoMaterialDTO> materiaisUtilizados = produto.getProdutoMateriais().stream()
                    .map(this::converterMaterialParaDTO)
                    .toList();
            dto.setMateriaisUtilizados(materiaisUtilizados);
            dto.setCustoMateriaisCalculado(materiaisUtilizados.stream()
                    .map(ProdutoMaterialDTO::getCustoCalculado)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        }

        return dto;
    }

    private ProdutoMaterialDTO converterMaterialParaDTO(ProdutoMaterial produtoMaterial) {
        ProdutoMaterialDTO dto = new ProdutoMaterialDTO();
        dto.setMaterialId(produtoMaterial.getMaterial().getId());
        dto.setNomeMaterial(produtoMaterial.getMaterial().getNome());
        dto.setQuantidade(produtoMaterial.getQuantidade());
        dto.setUnidade(produtoMaterial.getUnidadeInformada());
        dto.setCustoCalculado(calcularCustoMaterial(produtoMaterial));
        return dto;
    }

    private BigDecimal calcularCustoMaterial(ProdutoMaterial produtoMaterial) {
        BigDecimal quantidade = produtoMaterial.getQuantidade();
        BigDecimal precoUnitario = produtoMaterial.getMaterial().getPrecoUnitario();

        if (quantidade == null || precoUnitario == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal quantidadeNaUnidadeDoMaterial = quantidade;
        boolean materialEmMetros = "Metros".equalsIgnoreCase(produtoMaterial.getMaterial().getUnidadeMedida());
        boolean informadoEmCm = "cm".equalsIgnoreCase(produtoMaterial.getUnidadeInformada());

        if (materialEmMetros && informadoEmCm) {
            quantidadeNaUnidadeDoMaterial = quantidade.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        }

        return quantidadeNaUnidadeDoMaterial.multiply(precoUnitario).setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public ProdutoDTO atualizar(Long id, ProdutoForm form, MultipartFile arquivoImagem, String email) {
        Produto produto = produtoRepository.findByIdAndUsuarioEmail(id, email)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        produto.setNome(form.getNome());
        produto.setDescricao(form.getDescricao());
        produto.setPreco(form.getPreco());
        produto.setPrecoCusto(form.getPrecoCusto());
        produto.setQuantidadeEstoque(form.getQuantidadeEstoque());
        produto.setStatus(form.getStatus());
        produto.setLargura(form.getLargura());
        produto.setAltura(form.getAltura());
        produto.setObservacoes(form.getObservacoes());

        if (form.getCategoriaId() != null) {
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            Categoria categoria = categoriaRepository.findByIdAndUsuarioIdUsuario(form.getCategoriaId(), usuario.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
            produto.setCategoria(categoria);
        }

        aplicarImagem(produto, arquivoImagem);

        Produto produtoSalvo = produtoRepository.save(produto);

        produtoMaterialRepository.deleteByProdutoId(produtoSalvo.getId());
        List<ProdutoMaterial> produtoMateriais = montarProdutoMateriais(produtoSalvo, form.getMateriais(), email);
        produtoMaterialRepository.saveAll(produtoMateriais);
        produtoSalvo.setProdutoMateriais(produtoMateriais);

        return converterParaDTO(produtoSalvo);
    }

    public ProdutoDTO buscarPorId(Long id, String email) {
        Produto produto = produtoRepository.findByIdAndUsuarioEmail(id, email)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        return converterParaDTO(produto);
    }

    @Transactional
    public void deletar(Long id, String email) {
        Produto produto = produtoRepository.findByIdAndUsuarioEmail(id, email)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (itemEncomendaRepository.existsByProdutoId(id)) {
            throw new IllegalStateException(
                    "Este produto está vinculado a um ou mais pedidos e não pode ser excluído. Desative o produto em vez de excluí-lo.");
        }

        try {
            produtoMaterialRepository.deleteByProdutoId(id);
            produtoRepository.delete(produto);
        } catch (OptimisticLockingFailureException e) {
            throw new IllegalStateException("Este produto já foi excluído.");
        }
    }

    private List<ProdutoMaterial> montarProdutoMateriais(Produto produto, List<ProdutoMaterialForm> materiaisForm, String email) {
        if (materiaisForm == null || materiaisForm.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> materiaisIds = materiaisForm.stream().map(ProdutoMaterialForm::getMaterialId).toList();
        List<Material> materiais = materialRepository.findByIdInAndUsuarioEmail(materiaisIds, email);
        if (materiais.size() != materiaisIds.size()) {
            throw new RuntimeException("Um ou mais materiais informados não foram encontrados");
        }
        Map<Long, Material> materiaisPorId = materiais.stream()
                .collect(Collectors.toMap(Material::getId, m -> m));

        List<ProdutoMaterial> resultado = new ArrayList<>();
        for (ProdutoMaterialForm item : materiaisForm) {
            ProdutoMaterial produtoMaterial = new ProdutoMaterial();
            produtoMaterial.setProduto(produto);
            produtoMaterial.setMaterial(materiaisPorId.get(item.getMaterialId()));
            produtoMaterial.setQuantidade(item.getQuantidade());
            produtoMaterial.setUnidadeInformada(item.getUnidade());
            resultado.add(produtoMaterial);
        }
        return resultado;
    }

    private void aplicarImagem(Produto produto, MultipartFile arquivoImagem) {
        if (arquivoImagem == null || arquivoImagem.isEmpty()) {
            return;
        }
        try {
            String nomeArquivo = UUID.randomUUID() + "_" + arquivoImagem.getOriginalFilename();
            Path caminho = Paths.get("uploads/" + nomeArquivo);
            Files.createDirectories(caminho.getParent());
            Files.write(caminho, arquivoImagem.getBytes());
            produto.setImagem(nomeArquivo);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar a imagem", e);
        }
    }
}
