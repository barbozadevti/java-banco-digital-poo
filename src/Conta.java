import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Comportamento comum a toda conta (herança). O saldo só muda pelos métodos da própria conta
 * (encapsulamento), e cada tipo de conta decide quanto está disponível para saque (polimorfismo).
 */
public abstract class Conta implements IConta {

    private static final int AGENCIA_PADRAO = 1;
    private static int sequencial = 1;
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    protected final int agencia;
    protected final int numero;
    protected final Cliente cliente;
    private BigDecimal saldo = BigDecimal.ZERO.setScale(2);
    private final List<Transacao> transacoes = new ArrayList<>();

    protected Conta(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("Toda conta precisa de um titular.");
        }
        this.agencia = AGENCIA_PADRAO;
        this.numero = sequencial++;
        this.cliente = cliente;
    }

    /** Nome do tipo de conta, usado no extrato. */
    protected abstract String tipo();

    /** Quanto pode sair da conta agora. A corrente soma o limite; a poupança, não. */
    public BigDecimal disponivelParaSaque() {
        return saldo;
    }

    @Override
    public void sacar(BigDecimal valor) {
        debitar(valor, Transacao.Tipo.SAQUE, "Saque");
    }

    @Override
    public void depositar(BigDecimal valor) {
        creditar(valor, Transacao.Tipo.DEPOSITO, "Depósito");
    }

    /** A transferência só credita o destino se o débito deu certo: ou acontecem as duas partes, ou nenhuma. */
    @Override
    public void transferir(BigDecimal valor, IConta destino) {
        if (!(destino instanceof Conta contaDestino)) {
            throw new IllegalArgumentException("Conta de destino inválida.");
        }
        if (contaDestino == this) {
            throw new IllegalArgumentException("Não é possível transferir para a própria conta.");
        }
        debitar(valor, Transacao.Tipo.TRANSFERENCIA_ENVIADA, "Para " + contaDestino.cliente.getNome());
        contaDestino.creditar(valor, Transacao.Tipo.TRANSFERENCIA_RECEBIDA, "De " + cliente.getNome());
    }

    /** Modelo do extrato (template method): o cabeçalho muda pelo tipo, o resto é comum. */
    @Override
    public final void imprimirExtrato() {
        System.out.println("=== Extrato " + tipo() + " ===");
        System.out.println("Titular: " + cliente.getNome() + " (CPF " + cliente.getCpfMascarado() + ")");
        System.out.println("Agência: " + agencia + "  Conta: " + numero);
        for (Transacao t : transacoes) {
            System.out.printf("%s  %-28s %14s%n", t.dataHora().format(DATA), t.descricao(), Dinheiro.formatar(t.valorComSinal()));
        }
        System.out.println("Saldo: " + Dinheiro.formatar(saldo));
        imprimirDetalhes();
        System.out.println();
    }

    /** Linhas extras do extrato de cada tipo (limite, rendimento...). */
    protected void imprimirDetalhes() {
    }

    protected final void debitar(BigDecimal valor, Transacao.Tipo tipo, String descricao) {
        BigDecimal v = Dinheiro.positivo(valor);
        if (v.compareTo(disponivelParaSaque()) > 0) {
            throw new SaldoInsuficienteException(disponivelParaSaque(), v);
        }
        saldo = saldo.subtract(v);
        transacoes.add(new Transacao(LocalDateTime.now(), tipo, v, saldo, descricao));
    }

    protected final void creditar(BigDecimal valor, Transacao.Tipo tipo, String descricao) {
        BigDecimal v = Dinheiro.positivo(valor);
        saldo = saldo.add(v);
        transacoes.add(new Transacao(LocalDateTime.now(), tipo, v, saldo, descricao));
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public int getAgencia() {
        return agencia;
    }

    public int getNumero() {
        return numero;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public List<Transacao> getTransacoes() {
        return List.copyOf(transacoes);
    }
}
