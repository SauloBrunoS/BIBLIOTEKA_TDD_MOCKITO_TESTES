package ufc.vv.biblioteka.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import jakarta.persistence.EntityNotFoundException;
import ufc.vv.biblioteka.model.Autor;
import ufc.vv.biblioteka.model.Colecao;
import ufc.vv.biblioteka.model.Livro;
import ufc.vv.biblioteka.model.Nacionalidade;
import ufc.vv.biblioteka.repository.AutorRepository;
import ufc.vv.biblioteka.service.AutorService;

class AutorServiceTest {

    @Mock
    private AutorRepository autorRepository;

    @InjectMocks
    private AutorService autorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateAutor_Success() {
        Autor autor = new Autor("José da Silva", LocalDate.of(1980, 1, 1), Nacionalidade.BRASIL);
        when(autorRepository.existsByNomeCompletoIgnoreCase(autor.getNomeCompleto())).thenReturn(false);

        autorService.createAutor(autor);

        verify(autorRepository, times(1)).save(autor);
    }

    @Test
    void testCreateAutor_DuplicateKeyException() {
        Autor autor = new Autor("José da Silva", LocalDate.of(1980, 1, 1), Nacionalidade.BRASIL);
        when(autorRepository.existsByNomeCompletoIgnoreCase(autor.getNomeCompleto())).thenReturn(true);

        assertThrows(DuplicateKeyException.class, () -> autorService.createAutor(autor));
        verify(autorRepository, never()).save(any(Autor.class));
    }

    @Test
    void testUpdateAutor_Success() {
        Autor existingAutor = new Autor("José da Silva", LocalDate.of(1980, 1, 1), Nacionalidade.BRASIL);
        existingAutor.setId(1);
        Autor updatedAutor = new Autor("José da Silva Mendes", LocalDate.of(1990, 1, 1), Nacionalidade.SUECIA);
        when(autorRepository.findById(1)).thenReturn(Optional.of(existingAutor));
        when(autorRepository.existsByNomeCompletoIgnoreCase(updatedAutor.getNomeCompleto())).thenReturn(false);
        when(autorRepository.save(existingAutor)).thenReturn(existingAutor);

        Autor result = autorService.updateAutor(1, updatedAutor);

        assertNotNull(result);
        assertEquals(updatedAutor.getNomeCompleto(), result.getNomeCompleto());
        assertEquals(updatedAutor.getDataNascimento(), result.getDataNascimento());
        assertEquals(updatedAutor.getNacionalidade(), result.getNacionalidade());

        verify(autorRepository, times(1)).save(existingAutor);
    }

    @Test
    void testUpdateAutor_EntityNotFoundException() {
        when(autorRepository.findById(2)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> autorService.updateAutor(2, new Autor()));
        verify(autorRepository, never()).save(any(Autor.class));
    }

    @Test
    void testUpdateAutor_DuplicateKeyException() {
        Autor existingAutor = new Autor("José da Silva", LocalDate.of(1980, 1, 1), Nacionalidade.BRASIL);
        existingAutor.setId(1);
        Autor updatedAutor = new Autor("George Orwell", LocalDate.of(1990, 1, 1), Nacionalidade.BRASIL);
        when(autorRepository.findById(1)).thenReturn(Optional.of(existingAutor));
        when(autorRepository.existsByNomeCompletoIgnoreCase(updatedAutor.getNomeCompleto())).thenReturn(true);

        assertThrows(DuplicateKeyException.class, () -> autorService.updateAutor(1, updatedAutor));
        verify(autorRepository, never()).save(any(Autor.class));
    }

    @Test
    void testDeleteAutor_Success() {
        Autor autor = new Autor("José da Silva", LocalDate.of(1980, 1, 1), Nacionalidade.BRASIL);
        autor.setId(1);
        when(autorRepository.findById(1)).thenReturn(Optional.of(autor));

        autorService.deleteAutorById(1);

        verify(autorRepository, times(1)).delete(autor);
    }

    @Test
    void testDeleteAutor_DataIntegrityViolationException() {
        Autor autor = new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA);
        autor.adicionarLivro(new Livro("1984", List.of(autor),
                "9788555780219", LocalDate.of(1949, 1, 1), 10, 400,
                List.of(new Colecao("Livros distópicos"))));
        autor.setId(3);
        when(autorRepository.findById(3)).thenReturn(Optional.of(autor));

        assertThrows(DataIntegrityViolationException.class, () -> autorService.deleteAutorById(3));
        verify(autorRepository, never()).delete(any(Autor.class));
    }

    @Test
    void testDeleteAutor_EntityNotFoundException() {
        when(autorRepository.findById(2)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> autorService.deleteAutorById(2));
        verify(autorRepository, never()).delete(any(Autor.class));
    }

}
