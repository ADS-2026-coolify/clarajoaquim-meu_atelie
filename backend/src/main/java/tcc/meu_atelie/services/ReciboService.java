package tcc.meu_atelie.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;
import tcc.meu_atelie.enums.StatusEncomenda;
import tcc.meu_atelie.models.Encomenda;
import tcc.meu_atelie.models.Endereco;
import tcc.meu_atelie.models.Loja;
import tcc.meu_atelie.repositories.EncomendaRepository;
import tcc.meu_atelie.repositories.LojaRepository;
import tcc.meu_atelie.util.ValorPorExtensoConverter;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReciboService {

    private static final String[] MESES = {
            "janeiro", "fevereiro", "março", "abril", "maio", "junho",
            "julho", "agosto", "setembro", "outubro", "novembro", "dezembro"
    };

    private final EncomendaRepository encomendaRepository;
    private final LojaRepository lojaRepository;
    private final TemplateEngine templateEngine;

    public byte[] gerarReciboPdf(Long encomendaId) {
        String emailLogado = SecurityContextHolder.getContext().getAuthentication().getName();

        Encomenda encomenda = encomendaRepository.findById(encomendaId)
                .orElseThrow(() -> new RuntimeException("Encomenda não encontrada"));

        if (!encomenda.getUsuario().getEmail().equals(emailLogado)) {
            throw new RuntimeException("Acesso negado");
        }

        if (encomenda.getStatus() != StatusEncomenda.FINALIZADO) {
            throw new IllegalStateException("Recibo só pode ser emitido para pedidos finalizados.");
        }

        if (encomenda.getDataEntrega() == null) {
            throw new IllegalStateException("Pedido finalizado sem data de entrega cadastrada. Preencha a data de entrega antes de emitir o recibo.");
        }

        Loja loja = lojaRepository.findByUsuarioIdUsuario(encomenda.getUsuario().getIdUsuario())
                .orElseThrow(() -> new RuntimeException("Perfil da loja não encontrado."));

        if (loja.getEndereco() == null) {
            throw new IllegalStateException(
                    "Endereço da loja não cadastrado. Complete o endereço em 'Meu Perfil' antes de emitir o recibo.");
        }

        BigDecimal valor = encomenda.getValorTotal() != null ? encomenda.getValorTotal() : BigDecimal.ZERO;

        Context context = new Context();
        context.setVariable("numero", String.format("%03d", encomenda.getId()));
        context.setVariable("valorFormatado", formatarMoeda(valor));
        context.setVariable("valorExtenso", capitalizar(ValorPorExtensoConverter.converter(valor)));
        context.setVariable("nomeCliente", encomenda.getCliente().getNome());
        context.setVariable("enderecoCliente", formatarEndereco(encomenda.getCliente().getEndereco()));
        context.setVariable("referente", montarReferente(encomenda));

        LocalDate dataFinalizacao = encomenda.getDataEntrega();
        context.setVariable("cidadeEmissao", loja.getEndereco().getCidade() != null ? loja.getEndereco().getCidade() : "");
        context.setVariable("diaEmissao", String.valueOf(dataFinalizacao.getDayOfMonth()));
        context.setVariable("mesEmissao", MESES[dataFinalizacao.getMonthValue() - 1]);
        context.setVariable("anoEmissao", String.valueOf(dataFinalizacao.getYear()));

        context.setVariable("emitenteNome", encomenda.getUsuario().getNome());
        context.setVariable("emitenteCpf", encomenda.getUsuario().getCpf());
        context.setVariable("enderecoEmitente", formatarEndereco(loja.getEndereco()));

        String html = templateEngine.process("recibo", context);

        return converterParaPdf(html);
    }

    private byte[] converterParaPdf(String html) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF do recibo", e);
        }
    }

    private String montarReferente(Encomenda encomenda) {
        if (encomenda.getItens() == null || encomenda.getItens().isEmpty()) {
            return "Pedido nº " + encomenda.getId();
        }
        return encomenda.getItens().stream()
                .map(item -> item.getQuantidade() + "x " + item.getProduto().getNome())
                .collect(Collectors.joining(", "));
    }

    private String formatarEndereco(Endereco endereco) {
        if (endereco == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (endereco.getRua() != null) {
            sb.append(endereco.getRua());
        }
        if (endereco.getNumero() != null) {
            sb.append(", ").append(endereco.getNumero());
        }
        if (endereco.getBairro() != null) {
            sb.append(" - ").append(endereco.getBairro());
        }
        if (endereco.getCidade() != null) {
            sb.append(", ").append(endereco.getCidade());
        }
        if (endereco.getEstado() != null) {
            sb.append("/").append(endereco.getEstado());
        }
        return sb.toString();
    }

    private String formatarMoeda(BigDecimal valor) {
        NumberFormat formato = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));
        return formato.format(valor);
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
