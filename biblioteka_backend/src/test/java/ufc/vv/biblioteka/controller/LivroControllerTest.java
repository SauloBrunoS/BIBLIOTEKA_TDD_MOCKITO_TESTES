package ufc.vv.biblioteka.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ufc.vv.biblioteka.model.Emprestimo;
import ufc.vv.biblioteka.model.Livro;
import ufc.vv.biblioteka.model.Reserva;
import ufc.vv.biblioteka.model.StatusReserva;
import ufc.vv.biblioteka.repository.EmprestimoRepository;
import ufc.vv.biblioteka.repository.LivroRepository;
import ufc.vv.biblioteka.repository.ReservaRepository;
import ufc.vv.biblioteka.service.LivroService;

import jakarta.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class LivroControllerTest {

    @Mock
    private LivroService livroService;

    @Mock
    private LivroRepository livroRepository;

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private LivroController livroController;

    @Test
    void testAdicionarLivro_Success() {
        Livro livro = new Livro();
        when(livroService.adicionarLivro(any(Livro.class))).thenReturn(livro);

        ResponseEntity<?> response = livroController.adicionarLivro(livro);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(livro, response.getBody());
    }

    @Test
    void testAdicionarLivro_BadRequest() {
        when(livroService.adicionarLivro(any(Livro.class))).thenThrow(IllegalArgumentException.class);

        ResponseEntity<?> response = livroController.adicionarLivro(new Livro());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testAtualizarLivro_Success() {
        Livro livro = new Livro();
        when(livroService.atualizarLivro(anyInt(), any(Livro.class))).thenReturn(livro);

        ResponseEntity<?> response = livroController.atualizarLivro(1, livro);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(livro, response.getBody());
    }

    @Test
    void testAtualizarLivro_NotFound() {
        when(livroService.atualizarLivro(anyInt(), any(Livro.class))).thenThrow(EntityNotFoundException.class);

        ResponseEntity<?> response = livroController.atualizarLivro(1, new Livro());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testExcluirLivro_Success() {
        ResponseEntity<?> response = livroController.excluirLivro(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(livroService, times(1)).excluirLivro(1);
    }

    @Test
    void testExcluirLivro_NotFound() {
        doThrow(EntityNotFoundException.class).when(livroService).excluirLivro(1);

        ResponseEntity<?> response = livroController.excluirLivro(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testExcluirLivro_Conflict() {
        doThrow(DataIntegrityViolationException.class).when(livroService).excluirLivro(1);

        ResponseEntity<?> response = livroController.excluirLivro(1);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void testBuscarLivroPorId_Success() {
        Livro livro = new Livro();
        when(livroRepository.findById(anyInt())).thenReturn(Optional.of(livro));

        ResponseEntity<Livro> response = livroController.buscarLivroPorId(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(livro, response.getBody());
    }

    @Test
    void testBuscarLivroPorId_NotFound() {
        when(livroRepository.findById(anyInt())).thenReturn(Optional.empty());

        ResponseEntity<Livro> response = livroController.buscarLivroPorId(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testBuscarLivro_Success() {
        Page<Livro> livros = mock(Page.class);
        when(livroRepository.findByAllFields(anyString(), any(), any(), any(Pageable.class))).thenReturn(livros);

        ResponseEntity<Page<Livro>> response = livroController.buscarLivro("search", null, null, Pageable.unpaged());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(livros, response.getBody());
    }

    @Test
    void testGetReservasPorLivroId() {
        Page<Reserva> reservas = mock(Page.class);
        when(reservaRepository.findByLivroIdAndSearch(anyInt(), anyString(), any(), any(StatusReserva.class), any(Pageable.class)))
            .thenReturn(reservas);

        ResponseEntity<Page<Reserva>> response = livroController.getReservasPorLivrorId(1, "search", null, StatusReserva.PENDENTE, Pageable.unpaged());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reservas, response.getBody());
    }

    @Test
    void testGetEmprestimosPorLivroId() {
        Page<Emprestimo> emprestimos = mock(Page.class);
        when(emprestimoRepository.findByLivroIdAndSearch(anyInt(), anyString(), any(), anyBoolean(), any(Pageable.class)))
            .thenReturn(emprestimos);

        ResponseEntity<Page<Emprestimo>> response = livroController.getEmprestimosPorLivroId(1, "search", null, true, Pageable.unpaged());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(emprestimos, response.getBody());
    }
}
