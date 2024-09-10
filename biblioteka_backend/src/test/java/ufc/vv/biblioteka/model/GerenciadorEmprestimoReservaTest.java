package ufc.vv.biblioteka.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GerenciadorEmprestimoReservaTest {

    private GerenciadorEmprestimoReserva gerenciador;

    @BeforeEach
    void setUp() {
        gerenciador = new GerenciadorEmprestimoReserva();
    }

    private Emprestimo criarEmprestimo(boolean devolvido) {
        Emprestimo emprestimo = new Emprestimo(new Leitor(), new Livro());
        if (devolvido)
            emprestimo.devolverLivro();
        return emprestimo;
    }

   private Reserva criarReserva(StatusReserva status) {
    Reserva reserva = new Reserva(new Livro(), new Leitor());
    atualizarStatusReserva(reserva, status);
    return reserva;
}

private void atualizarStatusReserva(Reserva reserva, StatusReserva status) {
    switch (status) {
        case EM_ANDAMENTO:
            reserva.marcarComoEmAndamento();
            break;
        case EM_ESPERA:
            reserva.marcarComoEmEspera();
            break;
        case CANCELADA:
            reserva.marcarComoCancelada();
            break;
        case EXPIRADA:
            reserva.marcarComoExpirada();
            break;
        case ATENDIDA:
            reserva.marcarComoAtendida();
            break;
        default:
            throw new IllegalArgumentException("Status de reserva desconhecido: " + status);
    }
}

    @Test
    void testGetQuantidadeEmprestimosRestantes_listaNula() {
        assertThrows(IllegalArgumentException.class, () -> gerenciador.getQuantidadeEmprestimosRestantes(null));
    }

    @Test
    void testGetQuantidadeEmprestimosRestantes_listaEmprestimosVazia() {
        List<Emprestimo> emprestimos = new ArrayList<>();
        assertEquals(5, gerenciador.getQuantidadeEmprestimosRestantes(emprestimos));
    }

    @Test
    void testGetQuantidadeEmprestimosRestantes_listaComUmEmprestimoDevolvido() {
        List<Emprestimo> emprestimos = List.of(
                criarEmprestimo(true));
        assertEquals(5, gerenciador.getQuantidadeEmprestimosRestantes(emprestimos));
    }

    @Test
    void testGetQuantidadeEmprestimosRestantes_listaComUmEmprestimoNaoDevolvido() {
        List<Emprestimo> emprestimos = List.of(
                criarEmprestimo(false));
        assertEquals(4, gerenciador.getQuantidadeEmprestimosRestantes(emprestimos));
    }

    @Test
    void testGetQuantidadeEmprestimosRestantes_comMaisDeUmEmprestimoNaoDevolvidoEDevolvido() {
        List<Emprestimo> emprestimos = List.of(
                criarEmprestimo(false),
                criarEmprestimo(true),
                criarEmprestimo(true),
                criarEmprestimo(false)
                );
        assertEquals(3, gerenciador.getQuantidadeEmprestimosRestantes(emprestimos));
    }

    @Test
    void testGetQuantidadeEmprestimosRestantes_comQuantidadeMaximaDeEmprestimosNaoDevolvidos() {
        List<Emprestimo> emprestimos = List.of(
                criarEmprestimo(false),
                criarEmprestimo(false),
                criarEmprestimo(true),
                criarEmprestimo(false),
                criarEmprestimo(true),
                criarEmprestimo(false),
                criarEmprestimo(false));
        assertEquals(0, gerenciador.getQuantidadeEmprestimosRestantes(emprestimos));
    }

    @Test
    void testGetQuantidadeEmprestimosRestantes_comQuantidadeDeEmprestimosNaoDevolvidosMaiorDoQueOPermitido() {
        List<Emprestimo> emprestimos = List.of(
                criarEmprestimo(false),
                criarEmprestimo(false),
                criarEmprestimo(true),
                criarEmprestimo(false),
                criarEmprestimo(true),
                criarEmprestimo(false),
                criarEmprestimo(false),
                criarEmprestimo(false)

        );
        assertThrows(IllegalArgumentException.class, () -> gerenciador.getQuantidadeEmprestimosRestantes(emprestimos));
    }

    @Test
    void testGetQuantidadeReservasRestantes_listaVazia() {
        List<Reserva> reservas = new ArrayList<>();
        assertEquals(5, gerenciador.getQuantidadeReservasRestantes(reservas));
    }

    @Test
    void testGetQuantidadeReservasRestantes_listaNula() {
        assertThrows(IllegalArgumentException.class, () -> gerenciador.getQuantidadeReservasRestantes(null));
    }

    @Test
    void testGetQuantidadeReservasRestantes_comUmaReservasEmAndamento() {
        List<Reserva> reservas = List.of(
                criarReserva(StatusReserva.EM_ANDAMENTO));
        assertEquals(4, gerenciador.getQuantidadeReservasRestantes(reservas));
    }

    @Test
    void testGetQuantidadeReservasRestantes_comUmaReservasEmEspera() {
        List<Reserva> reservas = List.of(
                criarReserva(StatusReserva.EM_ESPERA));
        assertEquals(4, gerenciador.getQuantidadeReservasRestantes(reservas));
    }

    @Test
    void testGetQuantidadeReservasRestantes_comOutrosTiposDeReserva() {
        List<Reserva> reservas = List.of(
                criarReserva(StatusReserva.ATENDIDA),
                criarReserva(StatusReserva.CANCELADA),
                criarReserva(StatusReserva.EXPIRADA));
        assertEquals(5, gerenciador.getQuantidadeReservasRestantes(reservas));
    }

    @Test
    void testGetQuantidadeReservasRestantes_comTodosOsTiposDeReserva() {
        List<Reserva> reservas = List.of(
                criarReserva(StatusReserva.EM_ANDAMENTO),
                criarReserva(StatusReserva.EM_ESPERA),
                criarReserva(StatusReserva.ATENDIDA),
                criarReserva(StatusReserva.EXPIRADA),
                criarReserva(StatusReserva.CANCELADA),
                criarReserva(StatusReserva.EXPIRADA),
                criarReserva(StatusReserva.ATENDIDA),
                criarReserva(StatusReserva.EM_ANDAMENTO),
                criarReserva(StatusReserva.CANCELADA),
                criarReserva(StatusReserva.EM_ESPERA));
        assertEquals(1, gerenciador.getQuantidadeReservasRestantes(reservas));
    }

    @Test
    void testGetQuantidadeReservasRestantes_comLimiteDeReservasEmAndamentoOuEmEspera() {
        List<Reserva> reservas = List.of(
                criarReserva(StatusReserva.EM_ANDAMENTO),
                criarReserva(StatusReserva.EM_ANDAMENTO),
                criarReserva(StatusReserva.ATENDIDA),
                criarReserva(StatusReserva.CANCELADA),
                criarReserva(StatusReserva.EM_ESPERA),
                criarReserva(StatusReserva.CANCELADA),
                criarReserva(StatusReserva.ATENDIDA),
                criarReserva(StatusReserva.EXPIRADA),
                criarReserva(StatusReserva.EM_ANDAMENTO),
                criarReserva(StatusReserva.EM_ESPERA),
                criarReserva(StatusReserva.EXPIRADA));
        assertEquals(0, gerenciador.getQuantidadeReservasRestantes(reservas));
    }

    @Test
    void testGetQuantidadeReservasRestantes_comMaisReservasEmAndamentoOuEmEsperaDoQuePermitido() {
        List<Reserva> reservas = List.of(
                criarReserva(StatusReserva.EM_ANDAMENTO),
                criarReserva(StatusReserva.EM_ANDAMENTO),
                criarReserva(StatusReserva.ATENDIDA),
                criarReserva(StatusReserva.CANCELADA),
                criarReserva(StatusReserva.EM_ESPERA),
                criarReserva(StatusReserva.CANCELADA),
                criarReserva(StatusReserva.ATENDIDA),
                criarReserva(StatusReserva.EXPIRADA),
                criarReserva(StatusReserva.EM_ANDAMENTO),
                criarReserva(StatusReserva.EM_ESPERA),
                criarReserva(StatusReserva.EM_ANDAMENTO),
                criarReserva(StatusReserva.EXPIRADA));

        assertThrows(IllegalArgumentException.class, () -> gerenciador.getQuantidadeReservasRestantes(reservas));
    }

}
