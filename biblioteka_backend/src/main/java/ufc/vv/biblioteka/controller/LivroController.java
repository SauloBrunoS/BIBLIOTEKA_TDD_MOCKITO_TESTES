package ufc.vv.biblioteka.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.persistence.EntityNotFoundException;
import ufc.vv.biblioteka.model.Emprestimo;
import ufc.vv.biblioteka.model.Livro;
import ufc.vv.biblioteka.model.Reserva;
import ufc.vv.biblioteka.model.StatusReserva;
import ufc.vv.biblioteka.repository.EmprestimoRepository;
import ufc.vv.biblioteka.repository.LivroRepository;
import ufc.vv.biblioteka.repository.ReservaRepository;
import ufc.vv.biblioteka.service.LivroService;

@RepositoryRestController("/livros")
public class LivroController {

    private final LivroService livroService;

    private final LivroRepository livroRepository;

    private final EmprestimoRepository emprestimoRepository;

    private final ReservaRepository reservaRepository;

    @Autowired
    public LivroController(LivroService livroService, LivroRepository livroRepository,
            EmprestimoRepository emprestimoRepository, ReservaRepository reservaRepository) {
        this.livroService = livroService;
        this.livroRepository = livroRepository;
        this.emprestimoRepository = emprestimoRepository;
        this.reservaRepository = reservaRepository;
    }

    @PostMapping
    public ResponseEntity<?> adicionarLivro(@RequestBody Livro livro) {
        try {
            Livro novoLivro = livroService.adicionarLivro(livro);
            return ResponseEntity.ok(novoLivro);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null); 
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro inesperado ao criar o livro.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarLivro(@PathVariable int id, @RequestBody Livro livro) {
        try {
            Livro livroAtualizado = livroService.atualizarLivro(id, livro);
            return ResponseEntity.ok(livroAtualizado);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro inesperado ao atualizar o autor.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirLivro(@PathVariable int id) {
        try {
            livroService.excluirLivro(id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro inesperado ao excluir o livro.");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Livro> buscarLivroPorId(@PathVariable int id) {
        try {
            Livro livro = livroRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Livro não encontrado"));
            return ResponseEntity.ok(livro);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // 404 Not Found
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<Livro>> buscarLivro(@RequestParam String search, @RequestParam(required=false) Integer colecaoId, Integer autorId,
            Pageable pageable) {
        Page<Livro> livros = livroRepository.findByAllFields(search, colecaoId, autorId, pageable);
        return ResponseEntity.ok(livros);
    }

    @GetMapping("/{livroId}/reservas")
    public ResponseEntity<Page<Reserva>> getReservasPorLivrorId(@PathVariable int livroId, String search, @RequestParam(required = false) Integer leitorId,
            StatusReserva status,
            Pageable pageable) {
        Page<Reserva> reservas = reservaRepository.findByLivroIdAndSearch(livroId, search, leitorId, status, pageable);
        return ResponseEntity.ok(reservas);
    }

    @GetMapping("/{livroId}/emprestimos")
    public ResponseEntity<Page<Emprestimo>> getEmprestimosPorLivroId(@PathVariable("livroId") int livroId,
    @RequestParam(required = false) String search, @RequestParam(required = false) Integer leitorId,
    @RequestParam(required = false) Boolean devolvido,
    Pageable pageable) {     
        Page<Emprestimo> emprestimos = emprestimoRepository.findByLivroIdAndSearch(livroId, search, leitorId, devolvido, pageable);
        return ResponseEntity.ok(emprestimos);
    }

}
