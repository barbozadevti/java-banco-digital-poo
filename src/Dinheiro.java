import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

/** Valores em reais: sempre BigDecimal com 2 casas (double erra centavos, ex.: 0.1 + 0.2). */
public final class Dinheiro {

    private static final Locale BRASIL = Locale.of("pt", "BR");

    private Dinheiro() {
    }

    public static BigDecimal de(String valor) {
        return new BigDecimal(valor).setScale(2, RoundingMode.HALF_EVEN);
    }

    /** Valida e arredonda um valor de operação: precisa ser positivo. */
    public static BigDecimal positivo(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor precisa ser maior que zero.");
        }
        return valor.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static String formatar(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(BRASIL).format(valor).replace(' ', ' ');
    }
}
