import java.math.BigDecimal;
import java.math.RoundingMode;

/** Conta poupança: sem limite e com rendimento mensal sobre o saldo. */
public class ContaPoupanca extends Conta {

    /** 0,5% ao mês (simplificação da regra da poupança). */
    public static final BigDecimal TAXA_MENSAL = new BigDecimal("0.005");

    private BigDecimal rendimentoAcumulado = BigDecimal.ZERO.setScale(2);

    public ContaPoupanca(Cliente cliente) {
        super(cliente);
    }

    @Override
    protected String tipo() {
        return "Conta Poupança";
    }

    /** Credita o rendimento do mês e devolve o valor rendido (zero se não houver saldo). */
    public BigDecimal renderMes() {
        BigDecimal rendimento = getSaldo().multiply(TAXA_MENSAL).setScale(2, RoundingMode.HALF_EVEN);
        if (rendimento.signum() > 0) {
            creditar(rendimento, Transacao.Tipo.RENDIMENTO, "Rendimento");
            rendimentoAcumulado = rendimentoAcumulado.add(rendimento);
        }
        return rendimento;
    }

    public BigDecimal getRendimentoAcumulado() {
        return rendimentoAcumulado;
    }

    @Override
    protected void imprimirDetalhes() {
        System.out.println("Rendimento acumulado: " + Dinheiro.formatar(rendimentoAcumulado));
    }
}
