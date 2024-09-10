package ufc.vv.biblioteka.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ufc.vv.biblioteka.exception.LimiteExcedidoException;
import ufc.vv.biblioteka.model.Leitor;
import ufc.vv.biblioteka.model.Livro;
import ufc.vv.biblioteka.model.Reserva;
import ufc.vv.biblioteka.model.StatusReserva;
import ufc.vv.biblioteka.repository.EmprestimoRepository;
import ufc.vv.biblioteka.repository.ReservaRepository;
import ufc.vv.biblioteka.service.GerenciadorReserva;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

class GerenciadorReservaTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @InjectMocks
    private GerenciadorReserva gerenciadorReserva;

    private Livro livro;
    private Leitor leitor;
    private Leitor novoLeitor;
    private Reserva reservaEmAndamento;
    private Reserva reservaEmEspera;
    private Reserva reservaExpirada;
    private Reserva reservaAtendida;
    private Reserva reservaCancelada;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        leitor = new Leitor();
        leitor.setId(1);
        livro = new Livro();
        livro.setNumeroCopiasTotais(4);
        livro.setNumeroCopiasDisponiveis(2);

        reservaEmAndamento = new Reserva(livro, leitor);
        reservaEmAndamento.marcarComoEmAndamento();
        reservaEmAndamento.setId(1);

        novoLeitor = new Leitor();
        novoLeitor.setId(2);

        reservaEmEspera = new Reserva(livro, novoLeitor);
        reservaEmEspera.marcarComoEmEspera();
        reservaEmEspera.setId(2);

        reservaExpirada = new Reserva(livro, leitor);
        reservaExpirada.marcarComoExpirada();

        reservaAtendida = new Reserva(livro, leitor);
        reservaAtendida.marcarComoAtendida();

        reservaCancelada = new Reserva(livro, leitor);
        reservaCancelada.marcarComoCancelada();

        livro.setReservas(new ArrayList<Reserva>(Arrays.asList(reservaEmAndamento, reservaEmEspera, reservaAtendida,
                reservaExpirada, reservaCancelada)));
    }

    @Test
    void testAtualizarReservaSeExistente_ListaNula() {
        livro.setReservas(null);
        assertThrows(IllegalStateException.class,
                () -> gerenciadorReserva.atualizarReservaSeExistente(leitor.getId(), livro));
    }

    @Test
    void testAtualizarReservaSeExistenteSemReserva_ListaVazia() {
        livro.setReservas(new ArrayList<>());
        Reserva atualizada = gerenciadorReserva.atualizarReservaSeExistente(leitor.getId(), livro);

        assertNull(atualizada);
    }

    @Test
    void testAtualizarReservaSeExistenteSemReserva_IdIncorreto() {
        Reserva atualizada = gerenciadorReserva.atualizarReservaSeExistente(3, livro);

        assertNull(atualizada);
    }

    @Test
    void testAtualizarReservaSeExistenteReservaEmAndamentoExistente() {
        Reserva atualizada = gerenciadorReserva.atualizarReservaSeExistente(leitor.getId(), livro);

        assertNotNull(atualizada);
        assertEquals(StatusReserva.ATENDIDA, atualizada.getStatus());
        assertEquals(atualizada, reservaEmAndamento);
    }

    @Test
    void testAtualizarReservaSeExistenteSemReserva_ListaSemReservasEmAndamento() {
        Reserva novaReservaEmEspera = new Reserva(livro, leitor);
        novaReservaEmEspera.marcarComoEmEspera();
        livro.setReservas(List.of(novaReservaEmEspera, reservaAtendida, reservaExpirada, reservaCancelada));
        Reserva atualizada = gerenciadorReserva.atualizarReservaSeExistente(leitor.getId(), livro);

        assertNull(atualizada);
    }

    @Test
    void testAtualizarReservaSeExistenteSemReserva_ListaSemReservasEmAndamentoParaOLeitor() {
        Leitor maisUmLeitor = new Leitor();
        leitor.setId(3);
        Reserva novaReservaEmAndamento = new Reserva(livro, maisUmLeitor);
        novaReservaEmAndamento.setId(3);
        novaReservaEmAndamento.marcarComoEmAndamento();

        livro.setReservas(new ArrayList<Reserva>(Arrays.asList(novaReservaEmAndamento, reservaEmEspera, reservaAtendida,
                reservaExpirada, reservaCancelada)));
        Reserva atualizada = gerenciadorReserva.atualizarReservaSeExistente(leitor.getId(), livro);

        assertNull(atualizada);
    }

    @Test
    void testAtualizarReservaSeExistenteMaisDeUmaReservaEmAndamento() {
        Leitor maisUmLeitor = new Leitor();
        leitor.setId(3);
        Reserva novaReservaEmAndamento = new Reserva(livro, maisUmLeitor);
        novaReservaEmAndamento.setId(3);
        novaReservaEmAndamento.marcarComoEmAndamento();
        livro.adicionarReserva(novaReservaEmAndamento);

        Reserva reservaAtualizada = gerenciadorReserva.atualizarReservaSeExistente(leitor.getId(), livro);

        assertNotNull(reservaAtualizada);
        assertEquals(StatusReserva.ATENDIDA, reservaAtualizada.getStatus());
        assertEquals(reservaAtualizada, reservaEmAndamento);
        assertNotEquals(reservaAtualizada, novaReservaEmAndamento);
        assertEquals(StatusReserva.EM_ANDAMENTO, novaReservaEmAndamento.getStatus());
    }

    @Test
    void testAtivarProximaReservaEmEspera_listaNula() {
        livro.setReservas(null);
        assertThrows(IllegalStateException.class,
                () -> gerenciadorReserva.ativarProximaReservaEmEspera(livro));
    }

    @Test
    void testAtivarProximaReservaEmEspera_ListaReservasVazia() {
        livro.setReservas(new ArrayList<>());

        gerenciadorReserva.ativarProximaReservaEmEspera(livro);

        verify(reservaRepository, times(0)).save(any(Reserva.class));
    }

    @Test
    void testAtivarProximaReservaEmEspera_SemReservasEmEspera() {
        livro.setReservas(List.of(reservaEmAndamento, reservaAtendida, reservaExpirada, reservaCancelada));

        gerenciadorReserva.ativarProximaReservaEmEspera(livro);

        verify(reservaRepository, times(0)).save(any(Reserva.class));
    }

    @Test
    void testAtivarProximaReservaEmEspera() {
        gerenciadorReserva.ativarProximaReservaEmEspera(livro);

        verify(reservaRepository, times(1)).save(reservaEmEspera);
        assertEquals(StatusReserva.EM_ANDAMENTO, reservaEmEspera.getStatus());
    }

    @Test
    void testAtivarProximaReservaEmEspera_MaisDeUmaReservaEmEspera() {
        Leitor maisUmLeitor = new Leitor();
        leitor.setId(3);
        Reserva novaReservaEmEspera = new Reserva(livro, maisUmLeitor);
        novaReservaEmEspera.marcarComoEmEspera();
        livro.adicionarReserva(novaReservaEmEspera);

        gerenciadorReserva.ativarProximaReservaEmEspera(livro);

        verify(reservaRepository, times(1)).save(reservaEmEspera);
        assertEquals(StatusReserva.EM_ANDAMENTO, reservaEmEspera.getStatus());
        assertEquals(StatusReserva.EM_ESPERA, novaReservaEmEspera.getStatus());
    }

    @Test
    void testAtualizarStatusReserva_listaNula() {
        livro.setReservas(null);
        assertThrows(IllegalStateException.class,
                () -> gerenciadorReserva.atualizarStatusReserva(livro, reservaEmEspera));
    }

    @Test
    void testAtualizarStatusReserva_LivroComMaisCopiasDisponiveisDoQueReservasEmAndamento() {
        gerenciadorReserva.atualizarStatusReserva(livro, reservaEmEspera);

        assertEquals(StatusReserva.EM_ANDAMENTO, reservaEmEspera.getStatus());
    }

    @Test
    void testAtualizarStatusReserva_LivroComMesmaQuantidadeDeCopiasDisponiveisDoQueReservasEmAndamento() {
        livro.setNumeroCopiasDisponiveis(1);
        gerenciadorReserva.atualizarStatusReserva(livro, reservaEmEspera);

        assertEquals(StatusReserva.EM_ESPERA, reservaEmEspera.getStatus());
    }

    @Test
    void testAjustarReservasParaNovoNumeroDeCopias_Sucesso_AumentarCopias() {
        Livro livroNovo = new Livro();
        livroNovo.setNumeroCopiasTotais(5); // Definindo mais cópias

        when(emprestimoRepository.countByDevolvidoFalseAndLivroId(livro.getId())).thenReturn(2); // 2 livros emprestados
        when(reservaRepository.countByStatusAndLivroId(StatusReserva.EM_ANDAMENTO, livro.getId())).thenReturn(2); // 2
                                                                                                                  // reservas
                                                                                                                  // em
                                                                                                                  // andamento

        gerenciadorReserva.ajustarReservasParaNovoNumeroDeCopias(livro, livroNovo);

        assertEquals(3, livroNovo.getNumeroCopiasDisponiveis());
        assertEquals(StatusReserva.EM_ANDAMENTO, reservaEmEspera.getStatus());
    }

    @Test
    void testAjustarReservasParaNovoNumeroDeCopias_Sucesso_AumentarCopias_MaisDeUmaReservaEmEspera_MaisDeUmaCopiaAumentada() {
        Leitor maisUmLeitor = new Leitor();
        novoLeitor.setId(3);

        Reserva novaReservaEmEspera = new Reserva(livro, maisUmLeitor);
        novaReservaEmEspera.setId(4);
        novaReservaEmEspera.marcarComoEmEspera();
        livro.adicionarReserva(novaReservaEmEspera);

        Livro livroNovo = new Livro();
        livroNovo.setNumeroCopiasTotais(6); // Definindo mais cópias

        when(emprestimoRepository.countByDevolvidoFalseAndLivroId(livro.getId())).thenReturn(2); // 2 livros emprestados
        when(reservaRepository.countByStatusAndLivroId(StatusReserva.EM_ANDAMENTO, livro.getId())).thenReturn(2); // 2
                                                                                                                  // reservas
                                                                                                                  // em
                                                                                                                  // andamento

        gerenciadorReserva.ajustarReservasParaNovoNumeroDeCopias(livro, livroNovo);

        assertEquals(4, livroNovo.getNumeroCopiasDisponiveis());
        assertEquals(StatusReserva.EM_ANDAMENTO, reservaEmEspera.getStatus());
        assertEquals(StatusReserva.EM_ANDAMENTO, novaReservaEmEspera.getStatus());
    }

    @Test
    void testAjustarReservasParaNovoNumeroDeCopias_Sucesso_AumentarCopias_MaisDeUmaReservaEmEspera_ApenasUmaCopiaAumentada() {
        Leitor maisUmLeitor = new Leitor();
        novoLeitor.setId(3);

        Reserva novaReservaEmEspera = new Reserva(livro, maisUmLeitor);
        novaReservaEmEspera.setId(4);
        novaReservaEmEspera.marcarComoEmEspera();
        livro.adicionarReserva(novaReservaEmEspera);

        Livro livroNovo = new Livro();
        livroNovo.setNumeroCopiasTotais(5); // Definindo mais cópias

        when(emprestimoRepository.countByDevolvidoFalseAndLivroId(livro.getId())).thenReturn(2); // 2 livros emprestados
        when(reservaRepository.countByStatusAndLivroId(StatusReserva.EM_ANDAMENTO, livro.getId())).thenReturn(2); // 2
                                                                                                                  // reservas
                                                                                                                  // em
                                                                                                                  // andamento

        gerenciadorReserva.ajustarReservasParaNovoNumeroDeCopias(livro, livroNovo);

        assertEquals(3, livroNovo.getNumeroCopiasDisponiveis());
        assertEquals(StatusReserva.EM_ANDAMENTO, reservaEmEspera.getStatus());
        assertEquals(StatusReserva.EM_ESPERA, novaReservaEmEspera.getStatus());
    }

    @Test
    void testAjustarReservasParaNovoNumeroDeCopias_Sucesso_QuantidadeDeCopiasNaoAlterada() {
        Livro livroNovo = new Livro();
        livroNovo.setNumeroCopiasTotais(4);

        when(emprestimoRepository.countByDevolvidoFalseAndLivroId(livro.getId())).thenReturn(2); // 2 livros emprestados
        when(reservaRepository.countByStatusAndLivroId(StatusReserva.EM_ANDAMENTO, livro.getId())).thenReturn(2); // 2
                                                                                                                  // reservas
                                                                                                                  // em
                                                                                                                  // andamento

        gerenciadorReserva.ajustarReservasParaNovoNumeroDeCopias(livro, livroNovo);

        assertEquals(2, livroNovo.getNumeroCopiasDisponiveis());
        assertEquals(StatusReserva.EM_ESPERA, reservaEmEspera.getStatus());
    }

    @Test
    void testAjustarReservasParaNovoNumeroDeCopias_Sucesso_DiminuirCopias_NovoNumeroCopiasTotaisMaiorDoQueSomaDeEmprestimosEReservas() {
        Livro livroNovo = new Livro();
        livro.setNumeroCopiasTotais(5);
        livro.setNumeroCopiasDisponiveis(4);
        livroNovo.setNumeroCopiasTotais(3);

        when(emprestimoRepository.countByDevolvidoFalseAndLivroId(livro.getId())).thenReturn(1);
        when(reservaRepository.countByStatusAndLivroId(StatusReserva.EM_ANDAMENTO, livro.getId())).thenReturn(1);

        gerenciadorReserva.ajustarReservasParaNovoNumeroDeCopias(livro, livroNovo);

        assertEquals(2, livroNovo.getNumeroCopiasDisponiveis());
    }

    @Test
    void testAjustarReservasParaNovoNumeroDeCopias_Sucesso_DiminuirCopias_Limite() {
        Livro livroNovo = new Livro();
        livro.setNumeroCopiasTotais(4);
        livro.setNumeroCopiasDisponiveis(3);
        livroNovo.setNumeroCopiasTotais(2);

        when(emprestimoRepository.countByDevolvidoFalseAndLivroId(livro.getId())).thenReturn(1); // 2 livros emprestados
        when(reservaRepository.countByStatusAndLivroId(StatusReserva.EM_ANDAMENTO, livro.getId())).thenReturn(1); // 2
                                                                                                                  // reservas
                                                                                                                  // em
                                                                                                                  // andamento

        gerenciadorReserva.ajustarReservasParaNovoNumeroDeCopias(livro, livroNovo);

        assertEquals(1, livroNovo.getNumeroCopiasDisponiveis());
    }

    @Test
    void testAjustarReservasParaNovoNumeroDeCopias_Erro_DiminuirCopias_LimiteUltrapassado() {
        Livro livroNovo = new Livro();
        livro.setNumeroCopiasTotais(4);
        livro.setNumeroCopiasDisponiveis(3);
        livroNovo.setNumeroCopiasTotais(1);
        when(emprestimoRepository.countByDevolvidoFalseAndLivroId(livro.getId())).thenReturn(1);
        when(reservaRepository.countByStatusAndLivroId(StatusReserva.EM_ANDAMENTO, livro.getId())).thenReturn(1);

        assertThrows(LimiteExcedidoException.class,
                () -> gerenciadorReserva.ajustarReservasParaNovoNumeroDeCopias(livro, livroNovo));
    }
}
