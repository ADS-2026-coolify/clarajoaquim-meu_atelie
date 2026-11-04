package tcc.meu_atelie.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tcc.meu_atelie.dto.CategoriaDTO;
import tcc.meu_atelie.models.Categoria;
import tcc.meu_atelie.models.Usuario;
import tcc.meu_atelie.repositories.CategoriaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public CategoriaDTO cadastrar(CategoriaDTO dto, Usuario usuario) {
        Categoria categoria = new Categoria();
        categoria.setNome(dto.getNome());
        categoria.setDescricao(dto.getDescricao());
        categoria.setUsuario(usuario);

        Categoria categoriaSalva = categoriaRepository.save(categoria);

        CategoriaDTO responseDTO = new CategoriaDTO();
        responseDTO.setId(categoriaSalva.getId());
        responseDTO.setNome(categoriaSalva.getNome());

        return responseDTO;
    }

    public List<CategoriaDTO> listarTodas() {
        return categoriaRepository.findAll().stream().map(categoria -> {
            CategoriaDTO dto = new CategoriaDTO();
            dto.setId(categoria.getId());
            dto.setNome(categoria.getNome());
            dto.setDescricao(categoria.getDescricao());
            return dto;
        }).toList();
    }

    public List<CategoriaDTO> listarPorUsuario(Long idUsuario) {
        List<Categoria> categorias = categoriaRepository.findByUsuarioIdUsuario(idUsuario);

        return categorias.stream().map(cat -> {
            CategoriaDTO dto = new CategoriaDTO();
            dto.setId(cat.getId());
            dto.setNome(cat.getNome());
            return dto;
        }).collect(Collectors.toList());
    }
}
