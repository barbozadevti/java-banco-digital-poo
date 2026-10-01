import java.math.BigDecimal;

/** Contrato de toda conta do banco (abstração): o que uma conta faz, sem dizer como. */
public interface IConta {

    void sacar(BigDecimal valor);

    void depositar(BigDecimal valor);

    void transferir(BigDecimal valor, IConta destino);

    void imprimirExtrato();
}
