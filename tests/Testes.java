import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Testes sem dependências: cada caso imprime ok/FALHA, e o processo sai com erro se algum falhar. */
public class Testes {

    private static int falhas;
    private static int total;

    private static final Cliente RAFAEL = new Cliente("Rafael Barboza", "529.982.247-25");
    private static final Cliente ANA = new Cliente("Ana Souza", "111.444.777-35");

    interface Caso {
        void executar() throws Exception;
    }

    static void caso(String nome, Caso caso) {
        total++;
        try {
            caso.executar();
            System.out.println("ok    " + nome);
        } catch (Throwable e) {
            falhas++;
            System.out.println("FALHA " + nome + "\n      " + e);
        }
    }

    static void igual(Object esperado, Object obtido) {
        if (esperado instanceof BigDecimal a && obtido instanceof BigDecimal b ? a.compareTo(b) != 0 : !esperado.equals(obtido)) {
            throw new AssertionError("esperado " + esperado + ", obtido " + obtido);
        }
    }

    static <T extends Throwable> T lanca(Class<T> tipo, Caso caso) throws Exception {
        try {
            caso.executar();
        } catch (Throwable e) {
            if (tipo.isInstance(e)) {
                return tipo.cast(e);
            }
            throw new AssertionError("esperava " + tipo.getSimpleName() + ", veio " + e);
        }
        throw new AssertionError("esperava " + tipo.getSimpleName() + ", nada foi lançado");
    }

    static BigDecimal r(String valor) {
        return Dinheiro.de(valor);
    }

    static String capturar(Runnable acao) {
        PrintStream original = System.out;
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        System.setOut(new PrintStream(saida, true, StandardCharsets.UTF_8));
        try {
            acao.run();
        } finally {
            System.setOut(original);
        }
        return saida.toString(StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        caso("BigDecimal não erra centavos (0,10 + 0,20 = 0,30)", () -> {
            ContaPoupanca c = new ContaPoupanca(RAFAEL);
            c.depositar(r("0.10"));
            c.depositar(r("0.20"));
            igual(r("0.30"), c.getSaldo());
        });

        caso("depósito e saque alteram o saldo e entram no extrato", () -> {
            ContaCorrente c = new ContaCorrente(RAFAEL);
            c.depositar(r("100"));
            c.sacar(r("40"));
            igual(r("60"), c.getSaldo());
            igual(List.of(Transacao.Tipo.DEPOSITO, Transacao.Tipo.SAQUE), c.getTransacoes().stream().map(Transacao::tipo).toList());
            igual(r("60"), c.getTransacoes().get(1).saldoApos());
        });

        caso("valor zero, negativo ou nulo é recusado", () -> {
            ContaCorrente c = new ContaCorrente(RAFAEL);
            lanca(IllegalArgumentException.class, () -> c.depositar(r("0")));
            lanca(IllegalArgumentException.class, () -> c.sacar(r("-5")));
            lanca(IllegalArgumentException.class, () -> c.depositar(null));
            igual(0, c.getTransacoes().size());
        });

        caso("poupança não deixa sacar além do saldo", () -> {
            ContaPoupanca c = new ContaPoupanca(RAFAEL);
            c.depositar(r("50"));
            SaldoInsuficienteException e = lanca(SaldoInsuficienteException.class, () -> c.sacar(r("50.01")));
            igual(true, e.getMessage().contains("R$ 50,00"));
            igual(r("50"), c.getSaldo());
        });

        caso("corrente usa o cheque especial até o limite, e não além", () -> {
            ContaCorrente c = new ContaCorrente(RAFAEL);
            c.depositar(r("100"));
            c.sacar(r("600"));
            igual(r("-500"), c.getSaldo());
            igual(true, c.isUsandoChequeEspecial());
            igual(r("0"), c.disponivelParaSaque());
            lanca(SaldoInsuficienteException.class, () -> c.sacar(r("0.01")));
        });

        caso("limite personalizado e limite negativo recusado", () -> {
            ContaCorrente c = new ContaCorrente(RAFAEL, r("1000"));
            igual(r("1000"), c.disponivelParaSaque());
            lanca(IllegalArgumentException.class, () -> new ContaCorrente(RAFAEL, r("-1")));
        });

        caso("transferência debita a origem e credita o destino", () -> {
            ContaCorrente origem = new ContaCorrente(RAFAEL);
            ContaPoupanca destino = new ContaPoupanca(ANA);
            origem.depositar(r("200"));
            origem.transferir(r("75.50"), destino);
            igual(r("124.50"), origem.getSaldo());
            igual(r("75.50"), destino.getSaldo());
            igual("De Rafael Barboza", destino.getTransacoes().get(0).descricao());
        });

        caso("transferência sem saldo não credita nada no destino", () -> {
            ContaPoupanca origem = new ContaPoupanca(RAFAEL);
            ContaCorrente destino = new ContaCorrente(ANA);
            origem.depositar(r("10"));
            lanca(SaldoInsuficienteException.class, () -> origem.transferir(r("11"), destino));
            igual(r("10"), origem.getSaldo());
            igual(r("0"), destino.getSaldo());
            igual(0, destino.getTransacoes().size());
        });

        caso("não transfere para a própria conta nem para destino nulo", () -> {
            ContaCorrente c = new ContaCorrente(RAFAEL);
            c.depositar(r("10"));
            lanca(IllegalArgumentException.class, () -> c.transferir(r("1"), c));
            lanca(IllegalArgumentException.class, () -> c.transferir(r("1"), null));
        });

        caso("poupança rende 0,5% ao mês sobre o saldo", () -> {
            ContaPoupanca c = new ContaPoupanca(RAFAEL);
            c.depositar(r("1000"));
            igual(r("5"), c.renderMes());
            // Juros sobre juros: 1005 x 0,5% = 5,025. Arredondamento bancário (HALF_EVEN): 5,02.
            igual(r("5.02"), c.renderMes());
            igual(r("1010.02"), c.getSaldo());
            igual(r("10.02"), c.getRendimentoAcumulado());
        });

        caso("poupança sem saldo não rende nem registra transação", () -> {
            ContaPoupanca c = new ContaPoupanca(RAFAEL);
            igual(r("0"), c.renderMes());
            igual(0, c.getTransacoes().size());
        });

        caso("tarifa mensal é cobrada e pode usar o cheque especial", () -> {
            ContaCorrente c = new ContaCorrente(RAFAEL);
            igual(true, c.cobrarTarifaMensal());
            igual(r("-12.90"), c.getSaldo());
            ContaCorrente semLimite = new ContaCorrente(ANA, r("0"));
            igual(false, semLimite.cobrarTarifaMensal());
        });

        caso("contas recebem números em sequência na agência 1", () -> {
            ContaCorrente a = new ContaCorrente(RAFAEL);
            ContaPoupanca b = new ContaPoupanca(RAFAEL);
            igual(a.getNumero() + 1, b.getNumero());
            igual(1, a.getAgencia());
        });

        caso("CPF validado pelos dígitos verificadores", () -> {
            igual(true, Cpf.valido("529.982.247-25"));
            igual(false, Cpf.valido("529.982.247-24"));
            igual(false, Cpf.valido("111.111.111-11"));
            igual(false, Cpf.valido("123"));
            lanca(IllegalArgumentException.class, () -> new Cliente("Fulano", "000.000.000-00"));
            lanca(IllegalArgumentException.class, () -> new Cliente(" ", "529.982.247-25"));
            igual("***.***.247-25", RAFAEL.getCpfMascarado());
        });

        caso("extrato muda com o tipo da conta (polimorfismo)", () -> {
            ContaCorrente cc = new ContaCorrente(RAFAEL);
            ContaPoupanca cp = new ContaPoupanca(RAFAEL);
            cc.depositar(r("10"));
            cp.depositar(r("10"));
            String corrente = capturar(cc::imprimirExtrato);
            String poupanca = capturar(cp::imprimirExtrato);
            igual(true, corrente.contains("=== Extrato Conta Corrente ===") && corrente.contains("Limite do cheque especial"));
            igual(true, poupanca.contains("=== Extrato Conta Poupança ===") && poupanca.contains("Rendimento acumulado"));
            igual(true, corrente.contains("CPF ***.***.247-25") && !corrente.contains("529.982"));
        });

        caso("banco lista clientes sem repetir e soma os depósitos", () -> {
            Banco banco = new Banco("Teste");
            ContaCorrente c1 = banco.abrirContaCorrente(RAFAEL);
            ContaPoupanca c2 = banco.abrirContaPoupanca(RAFAEL);
            ContaCorrente c3 = banco.abrirContaCorrente(ANA);
            c1.depositar(r("100"));
            c2.depositar(r("50"));
            c3.depositar(r("25.25"));
            igual(List.of("Ana Souza", "Rafael Barboza"), banco.listarClientes().stream().map(Cliente::getNome).toList());
            igual(r("175.25"), banco.totalEmDepositos());
            igual(2, banco.contasDe(RAFAEL).size());
            igual(c3, banco.buscar(c3.getNumero()).orElseThrow());
            igual(true, banco.buscar(9999).isEmpty());
        });

        caso("Main roda do começo ao fim", () -> {
            String saida = capturar(() -> Main.main(new String[0]));
            igual(true, saida.contains("Saldo: -R$ 150,00"));
            igual(true, saida.contains("Operação recusada: Saldo insuficiente"));
            igual(true, saida.contains("Total em depósitos: R$ 151,50"));
        });

        System.out.println();
        System.out.println((total - falhas) + " de " + total + " testes passaram.");
        if (falhas > 0) {
            System.exit(1);
        }
    }
}
