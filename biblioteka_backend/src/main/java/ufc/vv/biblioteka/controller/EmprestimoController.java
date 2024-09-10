package ufc.vv.biblioteka.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;
import ufc.vv.biblioteka.exception.LimiteExcedidoException;
import ufc.vv.biblioteka.exception.LivroIndisponivelException;
import ufc.vv.biblioteka.exception.LivroNaoEncontradoException;
import ufc.vv.biblioteka.model.Emprestimo;
import ufc.vv.biblioteka.exception.DataRenovacaoException;
import ufc.vv.biblioteka.exception.EmprestimoEmAndamentoExistenteException;
import ufc.vv.biblioteka.exception.EmprestimoJaDevolvidoException;
import ufc.vv.biblioteka.exception.LeitorNaoEncontradoException;
import ufc.vv.biblioteka.service.EmprestimoService;
import ufc.vv.biblioteka.exception.ReservaEmEsperaException;

@RepositoryRestController("/emprestimos")
@Log4j2
public class EmprestimoController {

    private final EmprestimoService emprestimoService;

    @Autowired
    public EmprestimoController(EmprestimoService emprestimoService) {
        this.emprestimoService = emprestimoService;
    }

    @PostMapping("/emprestar")
    public ResponseEntity<ResponseObject> emprestarLivro(@RequestParam("livroId") int livroId, @RequestParam("leitorId") int leitorId,
            @RequestParam String senha) {
        try {
            Emprestimo emprestimo = emprestimoService.emprestarLivro(livroId, leitorId, senha);
            return ResponseEntity.ok(new ResponseObject(HttpStatus.OK, "Livro emprestado com sucesso", emprestimo));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseObject(HttpStatus.FORBIDDEN, "Acesso negado", e.getMessage()));
        } catch (LivroIndisponivelException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ResponseObject(HttpStatus.CONFLICT, "Livro indisponível", e.getMessage()));
        } catch (LimiteExcedidoException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseObject(HttpStatus.FORBIDDEN, "Limite de empréstimos excedido", e.getMessage()));
        } catch (EmprestimoEmAndamentoExistenteException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseObject(HttpStatus.FORBIDDEN, "Empréstimo em andamento existente",
                            e.getMessage()));
        } catch (LivroNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject(HttpStatus.NOT_FOUND, "Livro não encontrado", null));
        } catch (LeitorNaoEncontradoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject(HttpStatus.NOT_FOUND, "Leitor não encontrado", null));
        } catch (Exception e) {
            log.error(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado ao emprestar livro",
                            null));
        }
    }

    @PostMapping("/devolver")
    public ResponseEntity<ResponseObject> devolverLivro(@RequestParam int emprestimoId, @RequestParam String senha) {
        try {
            Emprestimo emprestimo = emprestimoService.devolverLivro(emprestimoId, senha);
            return ResponseEntity.ok(new ResponseObject(HttpStatus.OK, "Livro devolvido com sucesso", emprestimo));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseObject(HttpStatus.FORBIDDEN, "Acesso negado", e.getMessage()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject(HttpStatus.NOT_FOUND, "Empréstimo não encontrado", null));
        } catch (EmprestimoJaDevolvidoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject(HttpStatus.NOT_FOUND, "Empréstimo já devolvido", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado ao devolver livro",
                            null));
        }
    }

    @PostMapping("/renovar")
    public ResponseEntity<ResponseObject> renovarEmprestimo(@RequestParam int emprestimoId,
            @RequestParam String senha) {
        try {
            Emprestimo emprestimo = emprestimoService.renovarEmprestimo(emprestimoId, senha);
            return ResponseEntity.ok(new ResponseObject(HttpStatus.OK, "Empréstimo renovado com sucesso", emprestimo));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ResponseObject(HttpStatus.FORBIDDEN, "Acesso negado", e.getMessage()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject(HttpStatus.NOT_FOUND, "Empréstimo não encontrado", null));
        } catch (EmprestimoJaDevolvidoException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject(HttpStatus.NOT_FOUND, "Empréstimo já devolvido", null));
        } catch (DataRenovacaoException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ResponseObject(HttpStatus.CONFLICT, "Data de renovação inválida", e.getMessage()));
        } catch (LimiteExcedidoException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ResponseObject(HttpStatus.CONFLICT, "Limite de renovações excedido", e.getMessage()));
        } catch (ReservaEmEsperaException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ResponseObject(HttpStatus.CONFLICT, "Há reservas em espera para este empréstimo",
                            e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado ao renovar empréstimo",
                            null));
        }
    }
}
