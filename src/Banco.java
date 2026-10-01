import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** O banco abre as contas e consulta a carteira de clientes. */
public class Banco {

    private final String nome;
    private final List<Conta> contas = new ArrayList<>();

    public Banco(String nome) {
        this.nome = nome;
    }

    public ContaCorrente abrirContaCorrente(Cliente cliente) {
        return registrar(new ContaCorrente(cliente));
    }

    public ContaPoupanca abrirContaPoupanca(Cliente cliente) {
        return registrar(new ContaPoupanca(cliente));
    }

    private <T extends Conta> T registrar(T conta) {
        contas.add(conta);
        return conta;
    }

    public Optional<Conta> buscar(int numero) {
        return contas.stream().filter(c -> c.getNumero() == numero).findFirst();
    }

    public List<Conta> contasDe(Cliente cliente) {
        return contas.stream().filter(c -> c.getCliente().getCpf().equals(cliente.getCpf())).toList();
    }

    /** Clientes em ordem alfabética, sem repetir quem tem mais de uma conta. */
    public List<Cliente> listarClientes() {
        List<Cliente> clientes = new ArrayList<>();
        for (Conta c : contas) {
            if (clientes.stream().noneMatch(x -> x.getCpf().equals(c.getCliente().getCpf()))) {
                clientes.add(c.getCliente());
            }
        }
        clientes.sort(Comparator.comparing(Cliente::getNome));
        return clientes;
    }

    /** Soma dos saldos de todas as contas. */
    public BigDecimal totalEmDepositos() {
        return contas.stream().map(Conta::getSaldo).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    public String getNome() {
        return nome;
    }

    public List<Conta> getContas() {
        return List.copyOf(contas);
    }
}
