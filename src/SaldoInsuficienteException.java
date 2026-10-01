import java.math.BigDecimal;

/** Regra de negócio: a operação passaria do saldo disponível da conta. */
public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(BigDecimal disponivel, BigDecimal pedido) {
        super("Saldo insuficiente: disponível " + Dinheiro.formatar(disponivel) + ", pedido " + Dinheiro.formatar(pedido) + ".");
    }
}
