package ufc.vv.biblioteka.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;

import jakarta.persistence.EntityNotFoundException;
import ufc.vv.biblioteka.exception.EmprestimoEmAndamentoExistenteException;
import ufc.vv.biblioteka.exception.LeitorNaoEncontradoException;
import ufc.vv.biblioteka.exception.LimiteExcedidoException;
import ufc.vv.biblioteka.exception.LivroNaoEncontradoException;
import ufc.vv.biblioteka.exception.ReservaEmAndamentoExistenteException;
import ufc.vv.biblioteka.exception.ReservaNaoPodeMaisSerCancelaException;
import ufc.vv.biblioteka.model.Emprestimo;
import ufc.vv.biblioteka.model.GerenciadorEmprestimoReserva;
import ufc.vv.biblioteka.model.Leitor;
import ufc.vv.biblioteka.model.Livro;
import ufc.vv.biblioteka.model.Reserva;
import ufc.vv.biblioteka.model.StatusReserva;
import ufc.vv.biblioteka.model.TipoUsuario;
import ufc.vv.biblioteka.model.Usuario;
import ufc.vv.biblioteka.repository.EmprestimoRepository;
import ufc.vv.biblioteka.repository.LeitorRepository;
import ufc.vv.biblioteka.repository.LivroRepository;
import ufc.vv.biblioteka.repository.ReservaRepository;
import ufc.vv.biblioteka.service.GerenciadorReserva;
import ufc.vv.biblioteka.service.ReservaService;
import ufc.vv.biblioteka.service.UsuarioService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

class ReservaServiceTest {

    @Mock
    private LivroRepository livroRepository;

    @Mock
    private LeitorRepository leitorRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private GerenciadorReserva gerenciadorReserva;

    private ReservaService reservaService;

    private Livro livro;
    private Leitor leitor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        reservaService = new ReservaService(livroRepository, leitorRepository, new GerenciadorEmprestimoReserva(),
                reservaRepository, usuarioService, gerenciadorReserva);

        livro = new Livro();
        livro.setId(1);

        leitor = new Leitor();
        leitor.setId(1);

        Usuario usuario = new Usuario("email@teste.com", "Senha@123", TipoUsuario.LEITOR);
        usuario.setId(1);

        leitor.setUsuario(usuario);

    }

    @Test
    void testReservarLivro_LivroNaoEncontrado() {
        when(livroRepository.findById(2)).thenReturn(Optional.empty());

        assertThrows(LivroNaoEncontradoException.class, () -> {
            reservaService.reservarLivro(2, 1, "Senha@123");
        });
    }

    @Test
    void testReservarLivro_LeitorNaoEncontrado() {
        when(livroRepository.findById(1)).thenReturn(Optional.of(livro));
        when(leitorRepository.findById(2)).thenReturn(Optional.empty());

        assertThrows(LeitorNaoEncontradoException.class, () -> {
            reservaService.reservarLivro(1, 2, "Senha@123");
        });
    }

    @Test
    void testReservarLivro_SenhaIncorreta() {
        when(livroRepository.findById(1)).thenReturn(Optional.of(livro));
        when(leitorRepository.findById(1)).thenReturn(Optional.of(leitor));
        when(usuarioService.verificarSenha(1, "Senha@1234")).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> {
            reservaService.reservarLivro(1, 1, "senha123");
        });
    }

    @Test
    void testReservarLivro_ReservaEmAndamentoExistente() {
        Reserva reserva = new Reserva(livro, leitor);
        reserva.marcarComoEmAndamento();
        livro.adicionarReserva(reserva);

        when(livroRepository.findById(1)).thenReturn(Optional.of(livro));
        when(leitorRepository.findById(1)).thenReturn(Optional.of(leitor));
        when(usuarioService.verificarSenha(1, "Senha@123")).thenReturn(true);

        assertThrows(ReservaEmAndamentoExistenteException.class, () -> {
            reservaService.reservarLivro(1, 1, "Senha@123");
        });
    }

    @Test
    void testReservarLivro_EmprestimoEmAndamentoExistente() {
        Emprestimo emprestimo = new Emprestimo(leitor, livro);
        leitor.adicionarEmprestimo(emprestimo);
        livro.setReservas(new ArrayList<>());

        when(livroRepository.findById(1)).thenReturn(Optional.of(livro));
        when(leitorRepository.findById(1)).thenReturn(Optional.of(leitor));
        when(usuarioService.verificarSenha(1, "Senha@123")).thenReturn(true);

        assertThrows(EmprestimoEmAndamentoExistenteException.class, () -> {
            reservaService.reservarLivro(1, 1, "Senha@123");
        });
    }

    @Test
    void testReservarLivro_LimiteReservasUltrapassado() {
        Livro livro2 = new Livro();
        livro2.setId(2);
        Livro livro3 = new Livro();
        livro.setId(3);
        Livro livro4 = new Livro();
        livro.setId(4);
        Livro livro5 = new Livro();
        livro.setId(5);
        Livro livro6 = new Livro();
        livro.setId(6);
        Reserva reserva2 = new Reserva(livro2, leitor);
        reserva2.marcarComoEmAndamento();
        Reserva reserva3 = new Reserva(livro3, leitor);
        reserva3.marcarComoEmEspera();
        Reserva reserva4 = new Reserva(livro4, leitor);
        reserva4.marcarComoEmAndamento();
        Reserva reserva5 = new Reserva(livro5, leitor);
        reserva5.marcarComoEmEspera();
        Reserva reserva6 = new Reserva(livro6, leitor);
        reserva6.marcarComoEmAndamento();
        livro.setReservas(new ArrayList<>());
        leitor.setReservas(new ArrayList<Reserva>(List.of(reserva2, reserva3, reserva4, reserva5, reserva6)));
        leitor.setEmprestimos(new ArrayList<>());

        when(livroRepository.findById(1)).thenReturn(Optional.of(livro));
        when(leitorRepository.findById(1)).thenReturn(Optional.of(leitor));
        when(usuarioService.verificarSenha(1, "Senha@123")).thenReturn(true);

        assertThrows(LimiteExcedidoException.class, () -> {

            reservaService.reservarLivro(1, 1, "Senha@123");
        });
    }

    @Test
    void testReservarLivro_Sucesso_ReservaEmAndamento() {
        livro.setReservas(new ArrayList<>());
        leitor.setReservas(new ArrayList<>());
        leitor.setEmprestimos(new ArrayList<>());

        livro.setNumeroCopiasDisponiveis(1);

        when(livroRepository.findById(1)).thenReturn(Optional.of(livro));
        when(leitorRepository.findById(1)).thenReturn(Optional.of(leitor));
        when(usuarioService.verificarSenha(1, "Senha@123")).thenReturn(true);
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reserva reserva = reservaService.reservarLivro(1, 1, "Senha@123");

        assertNotNull(reserva);
        assertEquals(livro, reserva.getLivro());
        assertEquals(leitor, reserva.getLeitor());
        verify(gerenciadorReserva).atualizarStatusReserva(livro, reserva);

        verify(reservaRepository).save(reserva);
    }

    @Test
    void testReservarLivro_Sucesso_ReservaEmEspera() {
        livro.setReservas(new ArrayList<>());
        leitor.setReservas(new ArrayList<>());
        leitor.setEmprestimos(new ArrayList<>());

        livro.setNumeroCopiasDisponiveis(0);

        when(livroRepository.findById(1)).thenReturn(Optional.of(livro));
        when(leitorRepository.findById(1)).thenReturn(Optional.of(leitor));
        when(usuarioService.verificarSenha(1, "Senha@123")).thenReturn(true);
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Reserva reserva = reservaService.reservarLivro(1, 1, "Senha@123");

        assertNotNull(reserva);
        assertEquals(livro, reserva.getLivro());
        assertEquals(leitor, reserva.getLeitor());
        verify(reservaRepository).save(reserva);
    }

    @Test
    void testCancelarReserva_ReservaNaoEncontrada() {
        when(reservaRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            reservaService.cancelarReserva(1, "Senha@123");
        });
    }

    @Test
    void testCancelarReserva_SenhaIncorreta() {
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reserva.setLeitor(leitor);

        when(reservaRepository.findById(1)).thenReturn(Optional.of(reserva));
        when(usuarioService.verificarSenha(1, "Senha@1234")).thenReturn(false); // Senha errada

        assertThrows(AccessDeniedException.class, () -> {
            reservaService.cancelarReserva(1, "Senha@1234");
        });

        verify(reservaRepository, never()).save(reserva);
    }

    @Test
    void testCancelarReserva_StatusAtendida() {
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reserva.setLeitor(leitor);
        reserva.marcarComoAtendida();

        when(reservaRepository.findById(1)).thenReturn(Optional.of(reserva));
        when(usuarioService.verificarSenha(1, "Senha@123")).thenReturn(true);

        assertThrows(ReservaNaoPodeMaisSerCancelaException.class, () -> {
            reservaService.cancelarReserva(1, "Senha@123");
        });

        verify(reservaRepository, never()).save(reserva);
    }

    @Test
    void testCancelarReserva_StatusCancelada() {
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reserva.setLeitor(leitor);
        reserva.marcarComoCancelada(); // Marcar reserva como CANCELADA

        when(reservaRepository.findById(1)).thenReturn(Optional.of(reserva));
        when(usuarioService.verificarSenha(1, "Senha@123")).thenReturn(true);

        assertThrows(ReservaNaoPodeMaisSerCancelaException.class, () -> {
            reservaService.cancelarReserva(1, "Senha@123");
        });

        verify(reservaRepository, never()).save(reserva); // Verifica que a reserva não foi salva
    }

    @Test
    void testCancelarReserva_StatusExpirada() {
        Reserva reserva = new Reserva();
        reserva.setId(1);
        reserva.setLeitor(leitor);
        reserva.marcarComoExpirada(); // Marcar reserva como EXPIRADA

        when(reservaRepository.findById(1)).thenReturn(Optional.of(reserva));
        when(usuarioService.verificarSenha(1, "Senha@123")).thenReturn(true);

        assertThrows(ReservaNaoPodeMaisSerCancelaException.class, () -> {
            reservaService.cancelarReserva(1, "Senha@123");
        });

        verify(reservaRepository, never()).save(reserva); // Verifica que a reserva não foi salva
    }

    @Test
    void testCancelarReserva_Sucesso_reservaEmAndamento() {
        Reserva reserva = new Reserva(livro, leitor);
        reserva.marcarComoEmAndamento();
        reserva.setId(1);

        when(reservaRepository.findById(1)).thenReturn(Optional.of(reserva));
        when(usuarioService.verificarSenha(1, "Senha@123")).thenReturn(true);

        reservaService.cancelarReserva(1, "Senha@123");

        assertEquals(StatusReserva.CANCELADA, reserva.getStatus());
        verify(gerenciadorReserva).ativarProximaReservaEmEspera(livro);
        verify(reservaRepository).save(reserva);
    }

    @Test
    void testCancelarReserva_Sucesso_reservaEmEspera() {
        Reserva reserva = new Reserva(livro, leitor);
        reserva.marcarComoEmEspera();
        reserva.setId(1);

        when(reservaRepository.findById(1)).thenReturn(Optional.of(reserva));
        when(usuarioService.verificarSenha(1, "Senha@123")).thenReturn(true);

        reservaService.cancelarReserva(1, "Senha@123");

        assertEquals(StatusReserva.CANCELADA, reserva.getStatus());
        verify(gerenciadorReserva).ativarProximaReservaEmEspera(livro);
        verify(reservaRepository).save(reserva);
    }

    @Test
    void testAtualizarReservasExpiradas() {
        Reserva reserva1 = new Reserva();
        reserva1.setLivro(livro);
        reserva1.marcarComoEmAndamento();
        reserva1.setId(1);

        Reserva reserva2 = new Reserva();
        reserva2.setId(2);
        Livro livro2 = new Livro();
        livro2.setId(2);
        reserva2.setLivro(livro2);
        reserva2.marcarComoEmAndamento();

        when(reservaRepository.findByStatusAndDataLimiteBefore(StatusReserva.EM_ANDAMENTO,
                LocalDate.now().minusDays(1)))
                .thenReturn(Arrays.asList(reserva1, reserva2));

        reservaService.atualizarReservasExpiradas();

        assertEquals(StatusReserva.EXPIRADA, reserva1.getStatus());
        assertEquals(StatusReserva.EXPIRADA, reserva2.getStatus());
        verify(reservaRepository).saveAll(Arrays.asList(reserva1, reserva2));

        verify(gerenciadorReserva).ativarProximaReservaEmEspera(reserva1.getLivro());
        verify(gerenciadorReserva).ativarProximaReservaEmEspera(reserva2.getLivro());

    }
}
