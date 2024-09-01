package ufc.vv.biblioteka.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ufc.vv.biblioteka.exception.LimiteExcedidoException;
import ufc.vv.biblioteka.exception.ReservaNaoPodeMaisSerCancelaException;
import ufc.vv.biblioteka.model.Reserva;
import ufc.vv.biblioteka.service.ReservaService;

import jakarta.persistence.EntityNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservaControllerTest {

    @Mock
    private ReservaService reservaService;

    @InjectMocks
    private ReservaController reservaController;

    @Test
    void testReservarLivro_Success() {
        Reserva reserva = new Reserva();
        when(reservaService.reservarLivro(anyInt(), anyInt(), anyString())).thenReturn(reserva);

        ResponseEntity<?> response = reservaController.reservarLivro(1, 1, "senha");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reserva, response.getBody());
    }

    @Test
    void testReservarLivro_EntityNotFound() {
        when(reservaService.reservarLivro(anyInt(), anyInt(), anyString())).thenThrow(EntityNotFoundException.class);

        ResponseEntity<?> response = reservaController.reservarLivro(1, 1, "senha");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testReservarLivro_LimiteExcedido() {
        when(reservaService.reservarLivro(anyInt(), anyInt(), anyString())).thenThrow(LimiteExcedidoException.class);

        ResponseEntity<?> response = reservaController.reservarLivro(1, 1, "senha");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void testCancelarReserva_Success() {
        Reserva reserva = new Reserva();
        when(reservaService.cancelarReserva(anyInt(), anyString())).thenReturn(reserva);

        ResponseEntity<?> response = reservaController.cancelarReserva(1, "senha");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reserva, response.getBody());
    }

    @Test
    void testCancelarReserva_EntityNotFound() {
        when(reservaService.cancelarReserva(anyInt(), anyString())).thenThrow(EntityNotFoundException.class);

        ResponseEntity<?> response = reservaController.cancelarReserva(1, "senha");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testCancelarReserva_ReservaNaoPodeMaisSerCancela() {
        when(reservaService.cancelarReserva(anyInt(), anyString())).thenThrow(ReservaNaoPodeMaisSerCancelaException.class);

        ResponseEntity<?> response = reservaController.cancelarReserva(1, "senha");

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }
}
