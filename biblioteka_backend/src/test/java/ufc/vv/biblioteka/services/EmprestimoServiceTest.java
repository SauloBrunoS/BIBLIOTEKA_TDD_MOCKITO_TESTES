package ufc.vv.biblioteka.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.access.AccessDeniedException;

import jakarta.persistence.EntityNotFoundException;
import ufc.vv.biblioteka.exception.DataRenovacaoException;
import ufc.vv.biblioteka.exception.EmprestimoEmAndamentoExistenteException;
import ufc.vv.biblioteka.exception.EmprestimoJaDevolvidoException;
import ufc.vv.biblioteka.exception.LeitorNaoEncontradoException;
import ufc.vv.biblioteka.exception.LimiteExcedidoException;
import ufc.vv.biblioteka.exception.LivroIndisponivelException;
import ufc.vv.biblioteka.exception.LivroNaoEncontradoException;
import ufc.vv.biblioteka.exception.ReservaEmEsperaException;
import ufc.vv.biblioteka.model.Emprestimo;
import ufc.vv.biblioteka.model.GerenciadorEmprestimoReserva;
import ufc.vv.biblioteka.model.GerenciadorRenovacao;
import ufc.vv.biblioteka.model.Leitor;
import ufc.vv.biblioteka.model.Livro;
import ufc.vv.biblioteka.model.Reserva;
import ufc.vv.biblioteka.model.TipoUsuario;
import ufc.vv.biblioteka.model.Usuario;
import ufc.vv.biblioteka.repository.EmprestimoRepository;
import ufc.vv.biblioteka.repository.LeitorRepository;
import ufc.vv.biblioteka.repository.LivroRepository;
import ufc.vv.biblioteka.service.EmprestimoService;
import ufc.vv.biblioteka.service.GerenciadorReserva;
import ufc.vv.biblioteka.service.UsuarioService;

import org.mockito.MockitoAnnotations;

class EmprestimoServiceTest {

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private LivroRepository livroRepository;

    @Mock
    private LeitorRepository leitorRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private GerenciadorReserva gerenciadorReserva;

    private GerenciadorEmprestimoReserva gerenciadorEmprestimoReserva;
    private GerenciadorRenovacao gerenciadorRenovacao;

    private EmprestimoService emprestimoService;

    private Livro livro;
    private Leitor leitor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gerenciadorEmprestimoReserva = new GerenciadorEmprestimoReserva();
        gerenciadorRenovacao = new GerenciadorRenovacao();
        emprestimoService = new EmprestimoService(emprestimoRepository, usuarioService, gerenciadorReserva,
                livroRepository, leitorRepository, gerenciadorEmprestimoReserva, gerenciadorRenovacao);

        livro = new Livro();
        livro.setId(1);

        leitor = new Leitor();
        leitor.setId(1);

        Usuario usuario = new Usuario("email@teste.com", "Senha@123", TipoUsuario.LEITOR);
        usuario.setId(1);

        leitor.setUsuario(usuario);
    }

    @Test
    void testEmprestarLivro_LivroNaoEncontrado() {
        when(livroRepository.findById(2)).thenReturn(Optional.empty());

        assertThrows(LivroNaoEncontradoException.class, () -> {
            emprestimoService.emprestarLivro(2, 1, "Senha@123");
        });
    }

    @Test
    void testEmprestarLivro_LeitorNaoEncontrado() {
        when(livroRepository.findById(1)).thenReturn(Optional.of(livro));
        when(leitorRepository.findById(2)).thenReturn(Optional.empty());

        assertThrows(LeitorNaoEncontradoException.class, () -> {
            emprestimoService.emprestarLivro(1, 2, "Senha@123");
        });
    }

    @Test
    void testEmprestarLivro_SenhaIncorreta() {
        when(livroRepository.findById(1)).thenReturn(Optional.of(livro));
        when(leitorRepository.findById(1)).thenReturn(Optional.of(leitor));
        when(usuarioService.verificarSenha(1, "Senha@1234")).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> {
            emprestimoService.emprestarLivro(1, 1, "Senha@1234");
        });
    }

    @Test
    void testEmprestarLivro_EmprestimoEmAndamentoExistente() {
        Emprestimo emprestimo = new Emprestimo(leitor, livro);
        leitor.adicionarEmprestimo(emprestimo);

        when(livroRepository.findById(1)).thenReturn(Optional.of(livro));
        when(leitorRepository.findById(1)).thenReturn(Optional.of(leitor));
        when(usuarioService.verificarSenha(1, "Senha@123")).thenReturn(true);

        assertThrows(EmprestimoEmAndamentoExistenteException.class, () -> {
            emprestimoService.emprestarLivro(1, 1, "Senha@123");
        });
    }

    @Test
    void testEmprestarLivro_LimiteEmprestimosUltrapassado() {
        Livro livro1 = new Livro();
        livro1.setId(2);

        Livro livro2 = new Livro();
        livro2.setId(3);

        Livro livro3 = new Livro();
        livro3.setId(4);

        Livro livro4 = new Livro();
        livro4.setId(5);

        Livro livro5 = new Livro();
        livro5.setId(6);

        Emprestimo emprestimo1 = new Emprestimo(leitor, livro1);
        Emprestimo emprestimo2 = new Emprestimo(leitor, livro2);
        Emprestimo emprestimo3 = new Emprestimo(leitor, livro3);
        Emprestimo emprestimo4 = new Emprestimo(leitor, livro4);
        Emprestimo emprestimo5 = new Emprestimo(leitor, livro5);

        leitor.setEmprestimos(Arrays.asList(emprestimo1, emprestimo2, emprestimo3, emprestimo4, emprestimo5));

        when(livroRepository.findById(1)).thenReturn(Optional.of(livro));
        when(leitorRepository.findById(1)).thenReturn(Optional.of(leitor));
        when(usuarioService.verificarSenha(1, "Senha@123")).thenReturn(true);

        assertThrows(LimiteExcedidoException.class, () -> {
            emprestimoService.emprestarLivro(1, 1, "Senha@123");
        });
    }

    @Test
    void testEmprestarLivro_SemReserva_LivroSemCopiasDisponiveis() {
        livro.setNumeroCopiasDisponiveis(0);
        leitor.setEmprestimos(new ArrayList<>());

        when(livroRepository.findById(1)).thenReturn(Optional.of(livro));
        when(leitorRepository.findById(1)).thenReturn(Optional.of(leitor));
        when(usuarioService.verificarSenha(1, "Senha@123")).thenReturn(true);
        when(gerenciadorReserva.atualizarReservaSeExistente(leitor.getId(), livro)).thenReturn(null);

        LivroIndisponivelException exception = assertThrows(LivroIndisponivelException.class, () -> {
            emprestimoService.emprestarLivro(1, 1, "Senha@123");
        });

        assertEquals("Livro não possui cópias para empréstimo", exception.getMessage());

    }

    @Test
    void testEmprestarLivro_SemReserva_LivroReservado() {
        livro.setNumeroCopiasDisponiveis(1);
        Reserva reserva = new Reserva(livro, leitor);
        reserva.marcarComoEmAndamento();
        livro.adicionarReserva(reserva);

        leitor.setEmprestimos(new ArrayList<>());

        when(livroRepository.findById(1)).thenReturn(Optional.of(livro));
        when(leitorRepository.findById(1)).thenReturn(Optional.of(leitor));
        when(usuarioService.verificarSenha(1, "Senha@123")).thenReturn(true);
        when(gerenciadorReserva.atualizarReservaSeExistente(leitor.getId(), livro)).thenReturn(null);

        LivroIndisponivelException exception = assertThrows(LivroIndisponivelException.class, () -> {
            emprestimoService.emprestarLivro(1, 1, "Senha@123");
        });

        assertEquals("Todas as cópias do livro estão reservadas e não estão disponíveis para empréstimo.",
                exception.getMessage());
    }

    @Test
    void testEmprestarLivro_SemReserva_Sucesso() {
        livro.setNumeroCopiasDisponiveis(1);

        livro.setReservas(new ArrayList<>());

        leitor.setEmprestimos(new ArrayList<>());

        when(livroRepository.findById(1)).thenReturn(Optional.of(livro));
        when(leitorRepository.findById(1)).thenReturn(Optional.of(leitor));
        when(usuarioService.verificarSenha(1, "Senha@123")).thenReturn(true);
        when(gerenciadorReserva.atualizarReservaSeExistente(leitor.getId(), livro)).thenReturn(null);
        when(emprestimoRepository.save(any(Emprestimo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Emprestimo emprestimo = emprestimoService.emprestarLivro(1, 1, "Senha@123");
        assertEquals(0, livro.getNumeroCopiasDisponiveis());
        verify(livroRepository).save(livro);
        assertEquals(livro, emprestimo.getLivro());
        assertEquals(leitor, emprestimo.getLeitor());
        verify(emprestimoRepository).save(emprestimo);
    }

    @Test
    void testEmprestarLivro_Sucesso_ComReserva() {
        livro.setNumeroCopiasDisponiveis(1);
        Reserva reserva = new Reserva(livro, leitor);
        reserva.marcarComoEmAndamento();
        reserva.setId(1);
        livro.adicionarReserva(reserva);

        leitor.setEmprestimos(new ArrayList<>());

        when(livroRepository.findById(1)).thenReturn(Optional.of(livro));
        when(leitorRepository.findById(1)).thenReturn(Optional.of(leitor));
        when(usuarioService.verificarSenha(1, "Senha@123")).thenReturn(true);
        when(gerenciadorReserva.atualizarReservaSeExistente(leitor.getId(), livro)).thenReturn(reserva);
        when(emprestimoRepository.save(any(Emprestimo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Emprestimo emprestimo = emprestimoService.emprestarLivro(1, 1, "Senha@123");

        assertEquals(0, livro.getNumeroCopiasDisponiveis());
        verify(livroRepository).save(livro);
        assertEquals(livro, emprestimo.getLivro());
        assertEquals(leitor, emprestimo.getLeitor());
        assertEquals(reserva, emprestimo.getReserva());
        assertEquals(emprestimo, reserva.getEmprestimo());
        verify(emprestimoRepository).save(emprestimo);
    }

    @Test
    void testDevolverLivro_EmprestimoNaoEncontrado() {
        when(emprestimoRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            emprestimoService.devolverLivro(1, "Senha@123");
        });
    }

    @Test
    void testDevolverLivro_SenhaIncorreta() {
        Emprestimo emprestimo = new Emprestimo(leitor, livro);
        emprestimo.setId(1);

        when(emprestimoRepository.findById(1)).thenReturn(Optional.of(emprestimo));
        when(usuarioService.verificarSenha(1, "Senha@1234")).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> {
            emprestimoService.devolverLivro(1, "Senha@1234");
        });
    }

    @Test
    void testDevolverLivro_EmprestimoJaDevolvido() {
        Emprestimo emprestimo = new Emprestimo(leitor, livro);
        emprestimo.setId(1);
        emprestimo.devolverLivro();

        when(emprestimoRepository.findById(1)).thenReturn(Optional.of(emprestimo));
        when(usuarioService.verificarSenha(1, "Senha@123")).thenReturn(true);

        assertThrows(EmprestimoJaDevolvidoException.class, () -> {
            emprestimoService.devolverLivro(1, "Senha@123");
        });
    }

    @Test
    void testDevolverLivro_Sucesso() {
        livro.setNumeroCopiasDisponiveis(1);
        Emprestimo emprestimo = new Emprestimo(leitor, livro);
        emprestimo.setId(1);

        when(emprestimoRepository.findById(1)).thenReturn(Optional.of(emprestimo));
        when(usuarioService.verificarSenha(1, "Senha@123")).thenReturn(true);

        emprestimoService.devolverLivro(1, "Senha@123");

        assertEquals(true, emprestimo.isDevolvido());
        assertEquals(LocalDate.now(), emprestimo.getDataDevolucao());
        assertEquals(2, livro.getNumeroCopiasDisponiveis());
        verify(emprestimoRepository).save(emprestimo);
        verify(livroRepository).save(livro);
        verify(gerenciadorReserva).ativarProximaReservaEmEspera(livro);
    }

    @Test
    void testRenovarEmprestimo_EmprestimoNaoEncontrado() {
        when(emprestimoRepository.findById(2)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            emprestimoService.renovarEmprestimo(2, "Senha@123");
        });
    }

    @Test
    void testRenovarEmprestimo_SenhaIncorreta() {
        Emprestimo emprestimo = new Emprestimo(leitor, livro);
        emprestimo.setId(1);
        when(emprestimoRepository.findById(1)).thenReturn(Optional.of(emprestimo));
        when(usuarioService.verificarSenha(emprestimo.getLeitor().getUsuario().getId(), "Senha@1234"))
                .thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> {
            emprestimoService.renovarEmprestimo(1, "Senha@1234");
        });

    }

    @Test
    void testRenovarEmprestimo_EmprestimoJaDevolvido() {
        Emprestimo emprestimo = new Emprestimo(leitor, livro);
        emprestimo.setId(1);
        emprestimo.devolverLivro();

        when(emprestimoRepository.findById(1)).thenReturn(Optional.of(emprestimo));
        when(usuarioService.verificarSenha(emprestimo.getLeitor().getUsuario().getId(), "Senha@123")).thenReturn(true);

        assertThrows(EmprestimoJaDevolvidoException.class, () -> {
            emprestimoService.renovarEmprestimo(1, "Senha@123");
        });

    }

    @Test
    void testRenovarEmprestimo_DataLimiteAindaNaoAtingida() {
        Emprestimo emprestimo = new Emprestimo(leitor, livro);
        emprestimo.setDataLimite(LocalDate.now().minusDays(1));
        emprestimo.setId(1);

        when(emprestimoRepository.findById(1)).thenReturn(Optional.of(emprestimo));
        when(usuarioService.verificarSenha(emprestimo.getLeitor().getUsuario().getId(), "Senha@123")).thenReturn(true);

        DataRenovacaoException dataRenovacaoException = assertThrows(DataRenovacaoException.class, () -> {
            emprestimoService.renovarEmprestimo(1, "Senha@123");
        });

        assertEquals("A renovação só pode ser feita na data limite do empréstimo", dataRenovacaoException.getMessage());
    }

    @Test
    void testRenovarEmprestimo_RenovacaoForaDaDataLimite() {
        Emprestimo emprestimo = new Emprestimo(leitor, livro);
        emprestimo.setDataLimite(LocalDate.now().plusDays(1));
        emprestimo.setId(1);

        when(emprestimoRepository.findById(1)).thenReturn(Optional.of(emprestimo));
        when(usuarioService.verificarSenha(emprestimo.getLeitor().getUsuario().getId(), "Senha@123")).thenReturn(true);

        DataRenovacaoException exception = assertThrows(DataRenovacaoException.class, () -> {
            emprestimoService.renovarEmprestimo(1, "Senha@123");
        });

        assertEquals("A renovação não pode mais ser realizada! Data limite do empréstimo foi ultrapassada", exception.getMessage());
    }

    @Test
    void testRenovarEmprestimo_ReservaEmEsperaExistente() {
        Emprestimo emprestimo = new Emprestimo(leitor, livro);
        emprestimo.setDataLimite(LocalDate.now());
        emprestimo.setId(1);

        Leitor leitor2 = new Leitor();
        leitor2.setId(2);

        Reserva reservaEmEspera = new Reserva(livro, leitor2);
        reservaEmEspera.marcarComoEmEspera();
        livro.adicionarReserva(reservaEmEspera);

        when(emprestimoRepository.findById(1)).thenReturn(Optional.of(emprestimo));
        when(usuarioService.verificarSenha(emprestimo.getLeitor().getUsuario().getId(), "Senha@123")).thenReturn(true);

        assertThrows(ReservaEmEsperaException.class, () -> {
            emprestimoService.renovarEmprestimo(1, "Senha@123");
        });

    }

    @Test
    void testRenovarEmprestimo_Sucesso() {
        Emprestimo emprestimo = new Emprestimo(leitor, livro);
        emprestimo.setDataLimite(LocalDate.now());
        emprestimo.setId(1);

        livro.setReservas(new ArrayList<>());

        when(emprestimoRepository.findById(1)).thenReturn(Optional.of(emprestimo));
        when(usuarioService.verificarSenha(emprestimo.getLeitor().getUsuario().getId(), "Senha@123")).thenReturn(true);

        emprestimoService.renovarEmprestimo(1, "Senha@123");

        assertEquals(1, emprestimo.getQuantidadeRenovacoes());
        assertEquals(LocalDate.now().plusDays(Emprestimo.DATA_LIMITE_PRAZO_DEVOLUCAO_EM_DIAS), emprestimo.getDataLimite());

        verify(emprestimoRepository, times(1)).save(emprestimo);
    }

}
