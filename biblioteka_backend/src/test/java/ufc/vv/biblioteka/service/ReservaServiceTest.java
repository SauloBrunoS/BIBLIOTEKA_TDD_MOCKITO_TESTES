package ufc.vv.biblioteka.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import ufc.vv.biblioteka.exception.ReservaEmAndamentoExistenteException;
import ufc.vv.biblioteka.model.Leitor;
import ufc.vv.biblioteka.model.Livro;
import ufc.vv.biblioteka.model.Reserva;
import ufc.vv.biblioteka.model.StatusReserva;
import ufc.vv.biblioteka.repository.LeitorRepository;
import ufc.vv.biblioteka.repository.LivroRepository;
import ufc.vv.biblioteka.repository.ReservaRepository;

import java.util.Collections;
@ExtendWith(MockitoExtension.class)
public class ReservaServiceTest {

    @Mock
    private LivroRepository livroRepository;

    @Mock
    private LeitorRepository leitorRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private ReservaService reservaService;

    private Livro livro;
    private Leitor leitor;
    private Reserva reserva;

    @BeforeEach
    public void setup() {
        livro = new Livro();
        livro.setId(1);
        livro.setNumeroCopiasDisponiveis(5);

        leitor = new Leitor();
        leitor.setId(1);
        leitor.setReservas(Collections.emptyList()); 
        leitor.setEmprestimos(Collections.emptyList()); 

        reserva = new Reserva();
        reserva.setId(1);
        reserva.setStatus(StatusReserva.EM_ANDAMENTO);
    }

    @Test
    public void testReservarLivro() {
        when(livroRepository.findById(1)).thenReturn(Optional.of(livro));
        when(leitorRepository.findById(1)).thenReturn(Optional.of(leitor));
        when(usuarioService.verificarSenha(1, "senha")).thenReturn(true);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

        Reserva resultado = reservaService.reservarLivro(1, 1, "senha");

        assertNotNull(resultado);
        assertEquals(StatusReserva.EM_ANDAMENTO, resultado.getStatus());
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    public void testReservarLivro_SenhaIncorreta() {
        when(livroRepository.findById(1)).thenReturn(Optional.of(livro));
        when(leitorRepository.findById(1)).thenReturn(Optional.of(leitor));
        when(usuarioService.verificarSenha(1, "senha")).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> {
            reservaService.reservarLivro(1, 1, "senha");
        });
    }

    @Test
    public void testReservarLivro_ReservaEmAndamentoExistente() {
        reserva.setStatus(StatusReserva.EM_ANDAMENTO);
        leitor.setReservas(Collections.singletonList(reserva));

        when(livroRepository.findById(1)).thenReturn(Optional.of(livro));
        when(leitorRepository.findById(1)).thenReturn(Optional.of(leitor));
        when(usuarioService.verificarSenha(1, "senha")).thenReturn(true);

        assertThrows(ReservaEmAndamentoExistenteException.class, () -> {
            reservaService.reservarLivro(1, 1, "senha");
        });
    }

    @Test
    public void testCancelarReserva() {
        when(reservaRepository.findById(1)).thenReturn(Optional.of(reserva));
        when(usuarioService.verificarSenha(1, "senha")).thenReturn(true);
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reserva);

        Reserva resultado = reservaService.cancelarReserva(1, "senha");

        assertNotNull(resultado);
        assertEquals(StatusReserva.CANCELADA, resultado.getStatus());
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    public void testAtualizarReservasExpiradas() {
        when(reservaRepository.findByStatusAndDataLimiteBefore(
                eq(StatusReserva.EM_ANDAMENTO), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(reserva));
        when(reservaRepository.saveAll(anyList())).thenReturn(Collections.singletonList(reserva));

        reservaService.atualizarReservasExpiradas();

        verify(reservaRepository).saveAll(anyList());
    }
}