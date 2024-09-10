package ufc.vv.biblioteka.model;

import java.util.List;

public class GerenciadorEmprestimoReserva {

    public static final int LIMITE_EMPRESTIMOS = 5;
    public static final int LIMITE_RESERVAS_EM_ANDAMENTO_OU_ESPERA = 5;

    private int getQuantidadeEmprestimosNaoDevolvidos(List<Emprestimo> emprestimos) {
        if (emprestimos == null) {
            throw new IllegalArgumentException("Lista de emprestimos não pode ser nula");
        }
        return (int) emprestimos.stream()
                .filter(emprestimo -> !emprestimo.isDevolvido())
                .count();
    }

    public int getQuantidadeEmprestimosRestantes(List<Emprestimo> emprestimos) {
        int qtdEmprestimosNaoDevolvidos = getQuantidadeEmprestimosNaoDevolvidos(emprestimos);
        int qtdEmprestimosRestantes = LIMITE_EMPRESTIMOS - qtdEmprestimosNaoDevolvidos;
        if (qtdEmprestimosRestantes < 0) throw new IllegalArgumentException("Lista de Empréstimos possui mais empréstimos não devolvidos do que o permitido");
        return qtdEmprestimosRestantes;
    }

    private int getQuantidadeReservasEmAndamentoOuEspera(List<Reserva> reservas) {
        if (reservas == null) {
            throw new IllegalArgumentException("Lista de reservas não pode ser nula");
        }
        return (int) reservas.stream()
                .filter(reserva -> reserva.getStatus() == StatusReserva.EM_ANDAMENTO
                        || reserva.getStatus() == StatusReserva.EM_ESPERA)
                .count();
    }

    public int getQuantidadeReservasRestantes(List<Reserva> reservas) {
        int qtdReservasEmAndamento = getQuantidadeReservasEmAndamentoOuEspera(reservas);
        int quantidadeReservasRestantes = LIMITE_RESERVAS_EM_ANDAMENTO_OU_ESPERA - qtdReservasEmAndamento;
        if(quantidadeReservasRestantes < 0) throw new IllegalArgumentException("Lista de Reservas possui mais reservas em andamento ou espera do que o permitido");
        return quantidadeReservasRestantes;
    }
}
