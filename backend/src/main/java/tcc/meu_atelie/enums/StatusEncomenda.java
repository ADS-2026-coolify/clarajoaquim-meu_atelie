package tcc.meu_atelie.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum StatusEncomenda {
    ORCAMENTO("Orçamento"),
    EM_ANDAMENTO("Em andamento"),
    FINALIZADO("Finalizado"),
    CANCELADO("Cancelado");

    private final String descricao;

    StatusEncomenda(String descricao) {
        this.descricao = descricao;
    }

    public String getNome() {
        return name();
    }

    @JsonCreator
    public static StatusEncomenda fromString(String value) {
        if (value == null) return null;
        for (StatusEncomenda status : StatusEncomenda.values()) {
            if (status.name().equalsIgnoreCase(value) || status.getDescricao().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Status desconhecido: " + value);
    }
}
