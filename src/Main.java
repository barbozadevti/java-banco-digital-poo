import java.math.BigDecimal;

/** Demonstração: abre contas, movimenta, usa o cheque especial, rende a poupança e imprime os extratos. */
public class Main {

    public static void main(String[] args) {
        Banco banco = new Banco("Banco Digital");

        Cliente rafael = new Cliente("Rafael Barboza", "529.982.247-25");
        Cliente ana = new Cliente("Ana Souza", "111.444.777-35");

        ContaCorrente corrente = banco.abrirContaCorrente(rafael);
        ContaPoupanca poupanca = banco.abrirContaPoupanca(rafael);
        ContaCorrente daAna = banco.abrirContaCorrente(ana);

        corrente.depositar(Dinheiro.de("1000"));
        corrente.transferir(Dinheiro.de("300"), poupanca);
        corrente.sacar(Dinheiro.de("850"));          // passa do saldo: usa o cheque especial
        poupanca.renderMes();
        poupanca.transferir(Dinheiro.de("100"), daAna);

        // Polimorfismo: a mesma chamada, um extrato diferente para cada tipo de conta.
        for (IConta conta : banco.getContas()) {
            conta.imprimirExtrato();
        }

        try {
            poupanca.sacar(Dinheiro.de("5000"));
        } catch (SaldoInsuficienteException e) {
            System.out.println("Operação recusada: " + e.getMessage());
        }

        System.out.println("Clientes do " + banco.getNome() + ": "
                + banco.listarClientes().stream().map(Cliente::getNome).toList());
        System.out.println("Total em depósitos: " + Dinheiro.formatar(banco.totalEmDepositos()));
        BigDecimal disponivel = corrente.disponivelParaSaque();
        System.out.println("Disponível na corrente de " + rafael.getNome() + ": " + Dinheiro.formatar(disponivel));
    }
}
