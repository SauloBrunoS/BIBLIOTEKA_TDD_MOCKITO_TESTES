package ufc.vv.biblioteka.model;

import org.junit.jupiter.api.Test;

import ufc.vv.biblioteka.exception.LivroIndisponivelException;

import static org.junit.jupiter.api.Assertions.*;

class LivroTest {

    @Test
    void emprestarLivro_ShouldDecreaseAvailableCopies() {
        Livro livro = new Livro();
        livro.setNumeroCopiasDisponiveis(3);
        livro.emprestarLivro();
        assertEquals(2, livro.getNumeroCopiasDisponiveis());
    }

    @Test
    void devolverLivro_ShouldIncreaseAvailableCopies() {
        Livro livro = new Livro();
        livro.setNumeroCopiasDisponiveis(1);
        livro.devolverLivro();
        assertEquals(2, livro.getNumeroCopiasDisponiveis());
    }

    @Test
    void emprestarLivro_ShouldThrowExceptionWhenNoCopiesAvailable() {
        Livro livro = new Livro();
        livro.setNumeroCopiasDisponiveis(0);
        assertThrows(LivroIndisponivelException.class, livro::emprestarLivro);
    }
}
