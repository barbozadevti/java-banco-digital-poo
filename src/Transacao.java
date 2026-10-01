import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Uma linha do extrato. Imutável: o histórico não se altera depois de registrado. */
public record Transacao(LocalDateTime dataHora, Tipo tipo, BigDecimal valor, BigDecimal saldoApos, String descricao) {

    public enum Tipo { DEPOSITO, SAQUE, TRANSFERENCIA_ENVIADA, TRANSFERENCIA_RECEBIDA, RENDIMENTO, TARIFA }

    /** Valor com sinal: entradas positivas, saídas negativas. */
    public BigDecimal valorComSinal() {
        return switch (tipo) {
            case DEPOSITO, TRANSFERENCIA_RECEBIDA, RENDIMENTO -> valor;
            case SAQUE, TRANSFERENCIA_ENVIADA, TARIFA -> valor.negate();
        };
    }
}
