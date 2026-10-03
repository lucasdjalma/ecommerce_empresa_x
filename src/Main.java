import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Lê a linha de entrada
        String nome = scanner.next();
        String email = scanner.next();

        // TODO: Crie o objeto cliente com os dados fornecidos:
        Cliente cliente = new Cliente(nome, email);


        // Exibe a mensagem formatada conforme solicitado
        System.out.println("Cliente " + cliente.getNome() + " cadastrado com sucesso! Email: " + cliente.getEmail());

        scanner.close();
    }
}