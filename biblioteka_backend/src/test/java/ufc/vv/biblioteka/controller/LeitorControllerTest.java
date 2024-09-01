package ufc.vv.biblioteka.controller;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.persistence.EntityNotFoundException;
import ufc.vv.biblioteka.model.Emprestimo;
import ufc.vv.biblioteka.model.Leitor;
import ufc.vv.biblioteka.model.Reserva;
import ufc.vv.biblioteka.model.StatusReserva;
import ufc.vv.biblioteka.repository.EmprestimoRepository;
import ufc.vv.biblioteka.repository.LeitorRepository;
import ufc.vv.biblioteka.repository.ReservaRepository;
import ufc.vv.biblioteka.service.LeitorService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LeitorControllerTest {

    @Mock
    private LeitorRepository leitorRepository;

    @Mock
    private LeitorService leitorService;

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private LeitorController leitorController;

    @Test
    void testGetLeitorById_Found() {
        Leitor leitor = new Leitor();
        when(leitorRepository.findById(1)).thenReturn(Optional.of(leitor));

        ResponseEntity<Leitor> response = leitorController.getLeitorById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(leitor, response.getBody());
    }

    @Test
    void testGetLeitorById_NotFound() {
        when(leitorRepository.findById(1)).thenReturn(Optional.empty());

        ResponseEntity<Leitor> response = leitorController.getLeitorById(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testCriarLeitor_Success() {
        Leitor leitor = new Leitor();
        when(leitorService.criarLeitor(any(Leitor.class))).thenReturn(leitor);

        ResponseEntity<?> response = leitorController.criarLeitor(leitor);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(leitor, response.getBody());
    }

    @Test
    void testCriarLeitor_DuplicateKeyException() {
        Leitor leitor = new Leitor();
        when(leitorService.criarLeitor(any(Leitor.class))).thenThrow(DuplicateKeyException.class);

        ResponseEntity<?> response = leitorController.criarLeitor(leitor);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void testAtualizarLeitor_Success() {
        Leitor leitor = new Leitor();
        when(leitorService.atualizarLeitor(anyInt(), any(Leitor.class))).thenReturn(leitor);

        ResponseEntity<?> response = leitorController.atualizarLeitor(1, leitor);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(leitor, response.getBody());
    }

    @Test
    void testAtualizarLeitor_NotFound() {
        when(leitorService.atualizarLeitor(anyInt(), any(Leitor.class))).thenThrow(EntityNotFoundException.class);

        ResponseEntity<?> response = leitorController.atualizarLeitor(1, new Leitor());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testExcluirLeitor_Success() {
        ResponseEntity<?> response = leitorController.excluirLeitor(1);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(leitorService, times(1)).excluirLeitor(1);
    }

    @Test
    void testExcluirLeitor_NotFound() {
        doThrow(EntityNotFoundException.class).when(leitorService).excluirLeitor(1);

        ResponseEntity<?> response = leitorController.excluirLeitor(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetEmprestimosPorLeitorId() {
        Page<Emprestimo> emprestimos = mock(Page.class);
        when(emprestimoRepository.findByLeitorIdAndSearch(anyInt(), anyString(), anyInt(), anyBoolean(), any(Pageable.class)))
            .thenReturn(emprestimos);

        ResponseEntity<Page<Emprestimo>> response = leitorController.getEmprestimosPorLeitorId(1, "search", 1, true, Pageable.unpaged());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(emprestimos, response.getBody());
    }

    @Test
    void testGetReservasPorLeitorId() {
        Page<Reserva> reservas = mock(Page.class);
        when(reservaRepository.findByLeitorIdAndSearch(anyInt(), anyString(), anyInt(), any(StatusReserva.class), any(Pageable.class)))
            .thenReturn(reservas);

        ResponseEntity<Page<Reserva>> response = leitorController.getReservasPorLeitorId(1, "search", 1, StatusReserva.EM_ANDAMENTO, Pageable.unpaged());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reservas, response.getBody());
    }

    @Test
    void testBuscarLeitores() {
        Page<Leitor> leitores = mock(Page.class);
        when(leitorRepository.findByAllFields(anyString(), any(Pageable.class))).thenReturn(leitores);

        ResponseEntity<Page<Leitor>> response = leitorController.buscarLeitores("search", Pageable.unpaged());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(leitores, response.getBody());
    }
}
