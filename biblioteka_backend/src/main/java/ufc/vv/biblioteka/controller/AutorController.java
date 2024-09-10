package ufc.vv.biblioteka.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
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
import jakarta.validation.Valid;
import ufc.vv.biblioteka.model.Autor;
import ufc.vv.biblioteka.model.Livro;
import ufc.vv.biblioteka.model.Nacionalidade;
import ufc.vv.biblioteka.repository.AutorRepository;
import ufc.vv.biblioteka.repository.LivroRepository;
import ufc.vv.biblioteka.service.AutorService;

import org.springframework.data.domain.Page;

@RepositoryRestController("/autores")
public class AutorController {

    private final AutorRepository autorRepository;

    private final AutorService autorService;

    private final LivroRepository livroRepository;

    @Autowired
    public AutorController(AutorRepository autorRepository, AutorService autorService,
            LivroRepository livroRepository) {
        this.autorRepository = autorRepository;
        this.autorService = autorService;
        this.livroRepository = livroRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Autor> getAutorById(@PathVariable int id) {
        return autorRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ResponseObject> createAutor(@Valid @RequestBody Autor autor) {
        try {
            Autor createdAutor = autorService.createAutor(autor);
            return ResponseEntity.ok(new ResponseObject(HttpStatus.OK, "Autor criado com sucesso", createdAutor));
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ResponseObject(HttpStatus.CONFLICT, "Autor já existente", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseObject(HttpStatus.BAD_REQUEST, "Dados inválidos", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado ao criar o autor",
                            null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject> updateAutor(@PathVariable int id, @Valid @RequestBody Autor autorDetails) {
        try {
            Autor updatedAutor = autorService.updateAutor(id, autorDetails);
            return ResponseEntity.ok(new ResponseObject(HttpStatus.OK, "Autor atualizado com sucesso", updatedAutor));
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ResponseObject(HttpStatus.CONFLICT, "Autor já existente", e.getMessage()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject(HttpStatus.NOT_FOUND, "Autor não encontrado", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseObject(HttpStatus.BAD_REQUEST, "Dados inválidos", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado ao atualizar o autor",
                            null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject> deleteAutor(@PathVariable int id) {
        try {
            autorService.deleteAutorById(id);
            return ResponseEntity.ok(new ResponseObject(HttpStatus.OK, "Autor excluído com sucesso", null));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseObject(HttpStatus.NOT_FOUND, "Autor não encontrado", null));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ResponseObject(HttpStatus.CONFLICT,
                            "Não é possível excluir o autor: existem dados associados", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseObject(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado ao excluir o autor",
                            null));
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<Page<Autor>> buscarAutores(@RequestParam String search, Nacionalidade nacionalidade,
            Pageable pageable) {
        Page<Autor> autores = autorRepository.findByAllFields(search, nacionalidade, pageable);
        return ResponseEntity.ok(autores);
    }

    @GetMapping("/{id}/livros")
    public ResponseEntity<Page<Livro>> getLivrosByAutor(@PathVariable("id") int idAutor, @RequestParam String search,
            @RequestParam(required = false) Integer idColecao, Pageable pageable) {
        Page<Livro> livros = livroRepository.findByAllFields(search, idColecao, idAutor, pageable);
        return ResponseEntity.ok(livros);
    }
}