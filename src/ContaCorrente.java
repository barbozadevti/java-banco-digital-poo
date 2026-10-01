import java.math.BigDecimal;

/** Conta corrente: tem limite de cheque especial e tarifa mensal. */
public class ContaCorrente extends Conta {

    public static final BigDecimal LIMITE_PADRAO = Dinheiro.de("500");
    public static final BigDecimal TARIFA_MENSAL = Dinheiro.de("12.90");

    private final BigDecimal limite;

    public ContaCorrente(Cliente cliente) {
        this(cliente, LIMITE_PADRAO);
    }

    public ContaCorrente(Cliente cliente, BigDecimal limite) {
        super(cliente);
        if (limite == null || limite.signum() < 0) {
            throw new IllegalArgumentException("O limite não pode ser negativo.");
        }
        this.limite = limite.setScale(2);
    }

    @Override
    protected String tipo() {
        return "Conta Corrente";
    }

    /** O cheque especial entra no disponível: o saldo pode ficar negativo até o limite. */
    @Override
    public BigDecimal disponivelParaSaque() {
        return getSaldo().add(limite);
    }

    /** Cobra a tarifa do mês; se não houver disponível, a cobrança fica para depois. */
    public boolean cobrarTarifaMensal() {
        if (disponivelParaSaque().compareTo(TARIFA_MENSAL) < 0) {
            return false;
        }
        debitar(TARIFA_MENSAL, Transacao.Tipo.TARIFA, "Tarifa mensal");
        return true;
    }

    public boolean isUsandoChequeEspecial() {
        return getSaldo().signum() < 0;
    }

    public BigDecimal getLimite() {
        return limite;
    }

    @Override
    protected void imprimirDetalhes() {
        System.out.println("Limite do cheque especial: " + Dinheiro.formatar(limite)
                + (isUsandoChequeEspecial() ? " (em uso)" : ""));
        System.out.println("Disponível para saque: " + Dinheiro.formatar(disponivelParaSaque()));
    }
}
