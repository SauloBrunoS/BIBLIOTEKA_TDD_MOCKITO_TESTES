package ufc.vv.biblioteka.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.persistence.EntityNotFoundException;
import ufc.vv.biblioteka.exception.EmprestimoEmAndamentoExistenteException;
import ufc.vv.biblioteka.exception.LeitorNaoEncontradoException;
import ufc.vv.biblioteka.exception.LimiteExcedidoException;
import ufc.vv.biblioteka.exception.LivroNaoEncontradoException;
import ufc.vv.biblioteka.exception.ReservaEmAndamentoExistenteException;
import ufc.vv.biblioteka.exception.ReservaNaoPodeMaisSerCancelaException;
import ufc.vv.biblioteka.model.Reserva;
import ufc.vv.biblioteka.service.ReservaService;

@RepositoryRestController("/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    @Autowired
    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @PostMapping("/reservar")
    public ResponseEntity<ResponseObject> reservarLivro(@RequestParam int livroId, @RequestParam int leitorId,
            @RequestParam String senha) {
        try {
            Reserva reserva = reservaService.reservarLivro(livroId, leitorId, senha);
            return ResponseEntity.ok(new ResponseObject(HttpStatus.OK, "Reserva realizada com sucesso", reserva));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseObject(HttpStatus.FORBIDDEN, "Acesso negado", e.getMessage()));
        } catch (LeitorNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject(HttpStatus.NOT_FOUND, "Leitor não encontrado", null));
        } catch (LivroNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject(HttpStatus.NOT_FOUND, "Livro não encontrado", null));
        } catch (ReservaEmAndamentoExistenteException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseObject(HttpStatus.FORBIDDEN, "Reserva em andamento existente",
                            e.getMessage()));
        } catch (EmprestimoEmAndamentoExistenteException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseObject(HttpStatus.FORBIDDEN, "Empréstimo em andamento existente",
                            e.getMessage()));
        } catch (LimiteExcedidoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseObject(HttpStatus.FORBIDDEN, "Limite de reservas excedido", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado ao reservar o livro",
                            null));
        }
    }

    @PostMapping("/cancelar")
    public ResponseEntity<ResponseObject> cancelarReserva(@RequestParam int reservaId, @RequestParam String senha) {
        try {
            Reserva reserva = reservaService.cancelarReserva(reservaId, senha);
            return ResponseEntity.ok(new ResponseObject(HttpStatus.OK, "Reserva cancelada com sucesso", reserva));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseObject(HttpStatus.FORBIDDEN, "Acesso negado", e.getMessage()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject(HttpStatus.NOT_FOUND, "Reserva não encontrada", null));
        } catch (ReservaNaoPodeMaisSerCancelaException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseObject(HttpStatus.FORBIDDEN, "Não é mais possível cancelar a reserva",
                            e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado ao cancelar a reserva",
                            null));
        }
    }
}