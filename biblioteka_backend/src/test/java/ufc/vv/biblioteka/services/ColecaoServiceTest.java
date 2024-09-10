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
import ufc.vv.biblioteka.repository.ColecaoRepository;
import ufc.vv.biblioteka.service.ColecaoService;

class ColecaoServiceTest {

    @Mock
    private ColecaoRepository colecaoRepository;

    @InjectMocks
    private ColecaoService colecaoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateColecao_Success() {
        Colecao colecao = new Colecao("Livros de Suspense", "Coleção de livros de suspense.");
        when(colecaoRepository.existsByNomeIgnoreCase(colecao.getNome())).thenReturn(false);

        colecaoService.createColecao(colecao);

        verify(colecaoRepository, times(1)).save(colecao);
    }

    @Test
    void testCreateColecao_DuplicateKeyException() {
        Colecao colecao = new Colecao("Livros de Suspense", "Coleção de livros de suspense.");
        when(colecaoRepository.existsByNomeIgnoreCase(colecao.getNome())).thenReturn(true);

        assertThrows(DuplicateKeyException.class, () -> colecaoService.createColecao(colecao));
        verify(colecaoRepository, never()).save(any(Colecao.class));
    }

    @Test
    void testUpdateColecao_Success() {
        Colecao existingColecao = new Colecao("Livros de Suspense", "Coleção de livros de suspense.");
        existingColecao.setId(1);
        Colecao updatedColecao = new Colecao("Livros de Mistério", "Coleção de livros de mistério.");
        when(colecaoRepository.findByIdWithLivros(1)).thenReturn(Optional.of(existingColecao));
        when(colecaoRepository.existsByNomeIgnoreCase(updatedColecao.getNome())).thenReturn(false);
        when(colecaoRepository.save(existingColecao)).thenReturn(existingColecao);

        Colecao result = colecaoService.updateColecao(1, updatedColecao);

        assertNotNull(result);
        assertEquals(updatedColecao.getNome(), result.getNome());
        assertEquals(updatedColecao.getDescricao(), result.getDescricao());

        verify(colecaoRepository, times(1)).save(existingColecao);
    }

    @Test
    void testUpdateColecao_EntityNotFoundException() {
        when(colecaoRepository.findByIdWithLivros(2)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> colecaoService.updateColecao(2, new Colecao()));
        verify(colecaoRepository, never()).save(any(Colecao.class));
    }

    @Test
    void testUpdateColecao_DuplicateKeyException() {
        Colecao existingColecao = new Colecao("Livros de Suspense", "Coleção de livros de suspense.");
        existingColecao.setId(1);
        Colecao updatedColecao = new Colecao("Livros Clássicos", "Coleção de livros clássicos.");
        when(colecaoRepository.findByIdWithLivros(1)).thenReturn(Optional.of(existingColecao));
        when(colecaoRepository.existsByNomeIgnoreCase(updatedColecao.getNome())).thenReturn(true);

        assertThrows(DuplicateKeyException.class, () -> colecaoService.updateColecao(1, updatedColecao));
        verify(colecaoRepository, never()).save(any(Colecao.class));
    }

    @Test
    void testDeleteColecao_Success() {
        Colecao colecao = new Colecao("Livros de Suspense", "Coleção de livros de suspense.");
        colecao.setId(1);
        when(colecaoRepository.findByIdWithLivros(1)).thenReturn(Optional.of(colecao));

        colecaoService.deleteColecaoById(1);

        verify(colecaoRepository, times(1)).delete(colecao);
    }

    @Test
    void testDeleteColecao_DataIntegrityViolationException() {
        Colecao colecao = new Colecao("Livros de Suspense", "Coleção de livros de suspense.");
        colecao.adicionarLivro(new Livro("O retrato de Dorian Gray", List.of(new Autor("Oscar Wilde", LocalDate.of(1854, 10, 16), Nacionalidade.IRLANDA)), "9786580210756", LocalDate.of(1890, 6, 20), 10, 400, List.of(colecao)));
        colecao.setId(1);
        when(colecaoRepository.findByIdWithLivros(1)).thenReturn(Optional.of(colecao));

        assertThrows(DataIntegrityViolationException.class, () -> colecaoService.deleteColecaoById(1));
        verify(colecaoRepository, never()).delete(any(Colecao.class));
    }

    @Test
    void testDeleteColecao_EntityNotFoundException() {
        when(colecaoRepository.findByIdWithLivros(2)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> colecaoService.deleteColecaoById(2));
        verify(colecaoRepository, never()).delete(any(Colecao.class));
    }
}
