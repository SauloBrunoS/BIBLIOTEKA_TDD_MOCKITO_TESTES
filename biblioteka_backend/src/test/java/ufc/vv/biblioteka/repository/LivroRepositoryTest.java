package ufc.vv.biblioteka.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import jakarta.transaction.Transactional;
import ufc.vv.biblioteka.model.Livro;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;


import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
public class LivroRepositoryTest {

    @Autowired
    private LivroRepository livroRepository;

    @Test
    public void deveEncontrarLivroPorTodosOsCampos() {
        // Dado
        Livro livro = new Livro();
        livro.setTitulo("Dom Quixote");
        livro.setIsbn("978-3-16-148410-0");
        livroRepository.save(livro);

        // Quando
        Page<Livro> livros = livroRepository.findByAllFields("Quixote", null, null, PageRequest.of(0, 10));

        // Então
        assertFalse(livros.isEmpty());
    }

    @Test
    public void deveEncontrarLivroPorIsbn() {
        // Dado
        Livro livro = new Livro();
        livro.setIsbn("1234567890123");
        livroRepository.save(livro);

        // Quando
        Optional<Livro> livroEncontrado = livroRepository.findByIsbn("1234567890123");

        // Então
        assertTrue(livroEncontrado.isPresent());
    }

    @Test
    public void deveFiltrarLivrosPorIsbn() {
        // Dado
        Livro livro = new Livro();
        livro.setIsbn("978-3-16-148410-0");
        livroRepository.save(livro);

        // Quando
        List<Livro> livros = livroRepository.findAllWithISBNFilter("978");

        // Então
        assertFalse(livros.isEmpty());
    }
}