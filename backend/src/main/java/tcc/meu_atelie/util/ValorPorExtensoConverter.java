package tcc.meu_atelie.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class ValorPorExtensoConverter {

    private static final String[] UNIDADES = {
            "", "um", "dois", "três", "quatro", "cinco", "seis", "sete", "oito", "nove"
    };
    private static final String[] DEZ_A_DEZENOVE = {
            "dez", "onze", "doze", "treze", "quatorze", "quinze", "dezesseis", "dezessete", "dezoito", "dezenove"
    };
    private static final String[] DEZENAS = {
            "", "", "vinte", "trinta", "quarenta", "cinquenta", "sessenta", "setenta", "oitenta", "noventa"
    };
    private static final String[] CENTENAS = {
            "", "cento", "duzentos", "trezentos", "quatrocentos", "quinhentos",
            "seiscentos", "setecentos", "oitocentos", "novecentos"
    };

    private ValorPorExtensoConverter() {
    }

    public static String converter(BigDecimal valor) {
        BigDecimal valorAbsoluto = valor.abs().setScale(2, RoundingMode.HALF_UP);
        long reais = valorAbsoluto.longValue();
        int centavos = valorAbsoluto.subtract(BigDecimal.valueOf(reais))
                .movePointRight(2)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        if (reais == 0 && centavos == 0) {
            return "zero reais";
        }

        StringBuilder resultado = new StringBuilder();

        if (reais > 0) {
            resultado.append(numeroPorExtenso(reais));
            resultado.append(reais == 1 ? " real" : " reais");
        }

        if (centavos > 0) {
            if (reais > 0) {
                resultado.append(" e ");
            }
            resultado.append(numeroPorExtenso(centavos));
            resultado.append(centavos == 1 ? " centavo" : " centavos");
        }

        return resultado.toString();
    }

    private static String numeroPorExtenso(long numero) {
        if (numero == 0) {
            return "zero";
        }

        long bilhoes = numero / 1_000_000_000L;
        long milhoes = (numero / 1_000_000L) % 1000;
        long milhares = (numero / 1000L) % 1000;
        long resto = numero % 1000;

        List<String> segmentos = new ArrayList<>();

        if (bilhoes > 0) {
            segmentos.add(grupoDeTres(bilhoes) + (bilhoes == 1 ? " bilhão" : " bilhões"));
        }
        if (milhoes > 0) {
            segmentos.add(grupoDeTres(milhoes) + (milhoes == 1 ? " milhão" : " milhões"));
        }
        if (milhares > 0) {
            segmentos.add(milhares == 1 ? "mil" : grupoDeTres(milhares) + " mil");
        }
        if (resto > 0) {
            segmentos.add(grupoDeTres(resto));
        }

        boolean ultimoSegmentoUsaE = resto > 0 && (resto < 100 || resto % 100 == 0);

        StringBuilder resultado = new StringBuilder();
        for (int i = 0; i < segmentos.size(); i++) {
            if (i > 0) {
                boolean ultimo = i == segmentos.size() - 1;
                resultado.append(ultimo && ultimoSegmentoUsaE ? " e " : ", ");
            }
            resultado.append(segmentos.get(i));
        }

        return resultado.toString();
    }

    private static String grupoDeTres(long numero) {
        long centena = numero / 100;
        long restoDoisDigitos = numero % 100;

        StringBuilder resultado = new StringBuilder();

        if (centena > 0) {
            if (numero == 100) {
                resultado.append("cem");
            } else {
                resultado.append(CENTENAS[(int) centena]);
            }
        }

        if (restoDoisDigitos > 0) {
            if (centena > 0) {
                resultado.append(" e ");
            }
            resultado.append(dezenaEUnidade(restoDoisDigitos));
        }

        return resultado.toString();
    }

    private static String dezenaEUnidade(long numero) {
        if (numero < 10) {
            return UNIDADES[(int) numero];
        }
        if (numero < 20) {
            return DEZ_A_DEZENOVE[(int) (numero - 10)];
        }
        long dezena = numero / 10;
        long unidade = numero % 10;
        if (unidade == 0) {
            return DEZENAS[(int) dezena];
        }
        return DEZENAS[(int) dezena] + " e " + UNIDADES[(int) unidade];
    }
}
