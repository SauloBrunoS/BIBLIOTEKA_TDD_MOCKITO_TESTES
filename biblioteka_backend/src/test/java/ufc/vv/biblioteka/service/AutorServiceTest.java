package ufc.vv.biblioteka.service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import jakarta.persistence.EntityNotFoundException;
import ufc.vv.biblioteka.model.Autor;
import ufc.vv.biblioteka.model.Livro;
import ufc.vv.biblioteka.repository.AutorRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import java.util.List;

public class AutorServiceTest {

    @Mock
    private AutorRepository autorRepository;

    @InjectMocks
    private AutorService autorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveCriarAutor() {
        Autor autor = new Autor();
        autor.setNomeCompleto("João Silva");

        when(autorRepository.existsByNomeCompleto(any())).thenReturn(false);
        when(autorRepository.save(any(Autor.class))).thenReturn(autor);

        Autor result = autorService.createAutor(autor);

        assertNotNull(result);
        verify(autorRepository).save(autor);
    }

    @Test
    void deveLancarExcecaoSeAutorJaExistir() {
        Autor autor = new Autor();
        autor.setNomeCompleto("João Silva");

        when(autorRepository.existsByNomeCompleto(any())).thenReturn(true);

        assertThrows(DuplicateKeyException.class, () -> autorService.createAutor(autor));
    }

    @Test
    void deveAtualizarAutor() {
        Autor existingAutor = new Autor();
        existingAutor.setId(1);
        existingAutor.setNomeCompleto("João Silva");

        when(autorRepository.findById(anyInt())).thenReturn(java.util.Optional.of(existingAutor));
        when(autorRepository.save(any(Autor.class))).thenReturn(existingAutor);

        Autor updatedAutor = new Autor();
        updatedAutor.setNomeCompleto("João Atualizado");

        Autor result = autorService.updateAutor(1, updatedAutor);

        assertEquals("João Atualizado", result.getNomeCompleto());
        verify(autorRepository).save(existingAutor);
    }

    @Test
    void deveLancarExcecaoSeAutorNaoExistir() {
        when(autorRepository.findById(anyInt())).thenReturn(java.util.Optional.empty());

        Autor updatedAutor = new Autor();
        updatedAutor.setNomeCompleto("João Atualizado");

        assertThrows(EntityNotFoundException.class, () -> autorService.updateAutor(1, updatedAutor));
    }

    @Test
    void deveExcluirAutor() {
        Autor autor = new Autor();
        autor.setId(1);

        when(autorRepository.findById(anyInt())).thenReturn(java.util.Optional.of(autor));

        autorService.deleteAutorById(1);

        verify(autorRepository).delete(autor);
    }

    @Test
    void deveLancarExcecaoSeAutorTiverLivros() {
        Autor autor = new Autor();
        autor.setId(1);
        autor.setLivros(List.of(new Livro()));

        when(autorRepository.findById(anyInt())).thenReturn(java.util.Optional.of(autor));

        assertThrows(DataIntegrityViolationException.class, () -> autorService.deleteAutorById(1));
    }
}
