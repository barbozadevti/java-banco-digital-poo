/** Titular de uma ou mais contas. */
public class Cliente {

    private final String nome;
    private final String cpf;

    public Cliente(String nome, String cpf) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Informe o nome do cliente.");
        }
        if (!Cpf.valido(cpf)) {
            throw new IllegalArgumentException("CPF inválido: " + cpf);
        }
        this.nome = nome.trim();
        this.cpf = Cpf.somenteDigitos(cpf);
    }

    public String getNome() {
        return nome;
    }

    public String getCpf() {
        return cpf;
    }

    /** CPF com os 6 primeiros dígitos ocultos, como nos comprovantes. */
    public String getCpfMascarado() {
        return "***.***." + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }
}
