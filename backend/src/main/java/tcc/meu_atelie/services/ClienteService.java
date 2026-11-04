package tcc.meu_atelie.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import tcc.meu_atelie.dto.ClienteDetalheDTO;
import tcc.meu_atelie.dto.ClienteResumoDTO;
import tcc.meu_atelie.dto.EnderecoDTO;
import tcc.meu_atelie.forms.ClienteForm;
import tcc.meu_atelie.models.CanalAquisicao;
import tcc.meu_atelie.models.Cliente;
import tcc.meu_atelie.models.Endereco;
import tcc.meu_atelie.models.Usuario;
import tcc.meu_atelie.repositories.CanalAquisicaoRepository;
import tcc.meu_atelie.repositories.ClienteRepository;
import tcc.meu_atelie.repositories.EncomendaRepository;
import tcc.meu_atelie.repositories.UsuarioRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CanalAquisicaoRepository canalAquisicaoRepository;

    @Autowired
    private EncomendaRepository encomendaRepository;

    public List<ClienteResumoDTO> listarResumoClientesPorUsuario(String email) {
        List<Cliente> clientes = clienteRepository.findByUsuarioEmail(email);

        return clientes.stream()
                .map(c -> {
                    long totalPedidos = (c.getEncomendas() != null) ? c.getEncomendas().size() : 0L;

                    BigDecimal valorTotalGasto = (c.getEncomendas() != null)
                            ? c.getEncomendas().stream()
                            .map(e -> e.getValorTotal() != null ? e.getValorTotal() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            : BigDecimal.ZERO;

                    return new ClienteResumoDTO(
                            c.getId(),
                            c.getNome(),
                            c.getTelefone(),
                            c.getEmail(),
                            totalPedidos,
                            valorTotalGasto,
                            c.getDataCadastro(),
                            c.isAtivo()
                    );
                })
                .toList();
    }

    public ClienteResumoDTO salvarCliente(ClienteForm dto, String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Cliente cliente = new Cliente();
        cliente.setNome(dto.getNome());
        cliente.setEmail(dto.getEmail());
        cliente.setDataNascimento(dto.getDataNascimento());
        cliente.setObservacoes(dto.getObservacoes());
        cliente.setUsuario(usuario);

        if (dto.getCpf() != null) cliente.setCpf(dto.getCpf().replaceAll("\\D", ""));
        if (dto.getCnpj() != null) cliente.setCnpj(dto.getCnpj().replaceAll("\\D", ""));
        if (dto.getTelefone() != null) cliente.setTelefone(dto.getTelefone().replaceAll("\\D", ""));

        if (dto.getCanalId() != null) {
            CanalAquisicao canal = canalAquisicaoRepository.findById(dto.getCanalId())
                    .orElseThrow(() -> new RuntimeException("Canal não encontrado"));
            cliente.setCanalAquisicao(canal);
        }

        if (dto.getEndereco() != null) {
            Endereco endereco = new Endereco();
            endereco.setRua(dto.getEndereco().getRua());
            endereco.setNumero(dto.getEndereco().getNumero());
            endereco.setComplemento(dto.getEndereco().getComplemento());
            endereco.setBairro(dto.getEndereco().getBairro());
            endereco.setCidade(dto.getEndereco().getCidade());
            endereco.setEstado(dto.getEndereco().getEstado());
            endereco.setCep(dto.getEndereco().getCep() != null ? dto.getEndereco().getCep().replaceAll("\\D", "") : null);

            endereco.setCliente(cliente);
            cliente.setEndereco(endereco);
        }

        Cliente salvo = clienteRepository.save(cliente);

        return new ClienteResumoDTO(
                salvo.getId(), salvo.getNome(), salvo.getTelefone(),
                salvo.getEmail(), 0L, BigDecimal.ZERO, salvo.getDataCadastro(), cliente.isAtivo()
        );
    }

    public ClienteResumoDTO atualizarCliente(Long id, ClienteForm dto, String email) {
        Cliente cliente = clienteRepository.findByIdAndUsuarioEmail(id, email)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado ou acesso negado."));

        cliente.setNome(dto.getNome());
        cliente.setDataNascimento(dto.getDataNascimento());
        cliente.setEmail(dto.getEmail());
        cliente.setObservacoes(dto.getObservacoes());

        cliente.setCpf(dto.getCpf() != null ? dto.getCpf().replaceAll("\\D", "") : null);
        cliente.setCnpj(dto.getCnpj() != null ? dto.getCnpj().replaceAll("\\D", "") : null);
        cliente.setTelefone(dto.getTelefone() != null ? dto.getTelefone().replaceAll("\\D", "") : null);

        if (dto.getCanalId() != null) {
            CanalAquisicao canal = canalAquisicaoRepository.findById(dto.getCanalId())
                    .orElseThrow(() -> new RuntimeException("Canal não encontrado"));
            cliente.setCanalAquisicao(canal);
        } else {
            cliente.setCanalAquisicao(null);
        }

        if (dto.getEndereco() != null) {
            Endereco endereco = cliente.getEndereco();

            if (endereco == null) {
                endereco = new Endereco();
                endereco.setCliente(cliente);
                cliente.setEndereco(endereco);
            }

            endereco.setRua(dto.getEndereco().getRua());
            endereco.setNumero(dto.getEndereco().getNumero());
            endereco.setComplemento(dto.getEndereco().getComplemento());
            endereco.setBairro(dto.getEndereco().getBairro());
            endereco.setCidade(dto.getEndereco().getCidade());
            endereco.setEstado(dto.getEndereco().getEstado());
            endereco.setCep(dto.getEndereco().getCep() != null ? dto.getEndereco().getCep().replaceAll("\\D", "") : null);
        }

        Cliente salvo = clienteRepository.save(cliente);

        long totalPedidos = (salvo.getEncomendas() != null) ? salvo.getEncomendas().size() : 0L;
        BigDecimal valorTotal = (salvo.getEncomendas() != null)
                ? salvo.getEncomendas().stream()
                .map(e -> e.getValorTotal() != null ? e.getValorTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                : BigDecimal.ZERO;

        return new ClienteResumoDTO(
                salvo.getId(), salvo.getNome(), salvo.getTelefone(),
                salvo.getEmail(), totalPedidos, valorTotal, salvo.getDataCadastro(), cliente.isAtivo()
        );
    }

    public ClienteDetalheDTO buscarClientePorId(Long id, String email) {
        Cliente cliente = clienteRepository.findByIdAndUsuarioEmail(id, email)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        ClienteDetalheDTO dto = new ClienteDetalheDTO();
        dto.setId(cliente.getId());
        dto.setNome(cliente.getNome());
        dto.setCpf(cliente.getCpf());
        dto.setCnpj(cliente.getCnpj());
        dto.setDataNascimento(cliente.getDataNascimento());
        dto.setTelefone(cliente.getTelefone());
        dto.setEmail(cliente.getEmail());
        dto.setObservacoes(cliente.getObservacoes());

        if (cliente.getCanalAquisicao() != null) {
            ClienteDetalheDTO.CanalDTO canal = new ClienteDetalheDTO.CanalDTO();
            canal.setId(cliente.getCanalAquisicao().getId());
            canal.setDescricao(cliente.getCanalAquisicao().getDescricao());
            dto.setCanalAquisicao(canal);
        }

        if (cliente.getEndereco() != null) {
            EnderecoDTO end = new EnderecoDTO();
            end.setCep(cliente.getEndereco().getCep());
            end.setRua(cliente.getEndereco().getRua());
            end.setNumero(cliente.getEndereco().getNumero());
            end.setComplemento(cliente.getEndereco().getComplemento());
            end.setBairro(cliente.getEndereco().getBairro());
            end.setCidade(cliente.getEndereco().getCidade());
            end.setEstado(cliente.getEndereco().getEstado());
            dto.setEndereco(end);
        }

        return dto;
    }

    public void desativarCliente(Long id, String email) {
        Cliente cliente = clienteRepository.findByIdAndUsuarioEmailAndAtivoTrue(id, email)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado ou já inativo."));

        cliente.setAtivo(false);
        clienteRepository.save(cliente);
    }

    public void     reativarCliente(Long id, String email) {
        Cliente cliente = clienteRepository.findByIdAndUsuarioEmail(id, email)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado."));

        cliente.setAtivo(true);
        clienteRepository.save(cliente);
    }

    public void deletarPermanentemente(Long id, String email) {
        Cliente cliente = clienteRepository.findByIdAndUsuarioEmail(id, email)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado."));

        if (encomendaRepository.existsByClienteId(id)) {
            throw new IllegalStateException(
                    "Este cliente está vinculado a um ou mais pedidos e não pode ser excluído. Desative o cliente em vez de excluí-lo.");
        }

        try {
            clienteRepository.delete(cliente);
        } catch (OptimisticLockingFailureException e) {
            throw new IllegalStateException("Este cliente já foi excluído.");
        }
    }

    public List<ClienteResumoDTO> listarClientesInativos(String email) {
        List<Cliente> clientes = clienteRepository.findByUsuarioEmailAndAtivoFalse(email);

        return clientes.stream()
                .map(c -> {
                    long totalPedidos = (c.getEncomendas() != null) ? c.getEncomendas().size() : 0L;
                    BigDecimal valorTotalGasto = (c.getEncomendas() != null)
                            ? c.getEncomendas().stream()
                            .map(e -> e.getValorTotal() != null ? e.getValorTotal() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            : BigDecimal.ZERO;

                    return new ClienteResumoDTO(
                            c.getId(), c.getNome(), c.getTelefone(),
                            c.getEmail(), totalPedidos, valorTotalGasto, c.getDataCadastro(), c.isAtivo()
                    );
                }).toList();
    }

    public List<CanalAquisicao> listarCanais() {
        return canalAquisicaoRepository.findAll();
    }
}