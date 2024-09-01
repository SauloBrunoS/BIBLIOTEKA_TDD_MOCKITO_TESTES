package ufc.vv.biblioteka.service;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import ufc.vv.biblioteka.model.Colecao;
import ufc.vv.biblioteka.model.Livro;
import ufc.vv.biblioteka.repository.ColecaoRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;

import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyInt;

public class ColecaoServiceTest {

    @Mock
    private ColecaoRepository colecaoRepository;

    @InjectMocks
    private ColecaoService colecaoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveCriarColecao() {
        Colecao colecao = new Colecao();
        colecao.setNome("Coleção Aventura");

        when(colecaoRepository.existsByNome(any())).thenReturn(false);
        when(colecaoRepository.save(any(Colecao.class))).thenReturn(colecao);

        Colecao result = colecaoService.createColecao(colecao);

        assertNotNull(result);
        verify(colecaoRepository).save(colecao);
    }

    @Test
    void deveLancarExcecaoSeColecaoJaExistir() {
        Colecao colecao = new Colecao();
        colecao.setNome("Coleção Aventura");

        when(colecaoRepository.existsByNome(any())).thenReturn(true);

        assertThrows(DuplicateKeyException.class, () -> colecaoService.createColecao(colecao));
    }

    @Test
    void deveAtualizarColecao() {
        Colecao existingColecao = new Colecao();
        existingColecao.setId(1);
        existingColecao.setNome("Coleção Aventura");

        when(colecaoRepository.findById(anyInt())).thenReturn(java.util.Optional.of(existingColecao));
        when(colecaoRepository.save(any(Colecao.class))).thenReturn(existingColecao);

        Colecao updatedColecao = new Colecao();
        updatedColecao.setNome("Coleção Atualizada");

        Colecao result = colecaoService.updateColecao(1, updatedColecao);

        assertEquals("Coleção Atualizada", result.getNome());
        verify(colecaoRepository).save(existingColecao);
    }

    @Test
    void deveExcluirColecao() {
        Colecao colecao = new Colecao();
        colecao.setId(1);

        when(colecaoRepository.findById(anyInt())).thenReturn(java.util.Optional.of(colecao));

        colecaoService.deleteColecaoById(1);

        verify(colecaoRepository).delete(colecao);
    }

    @Test
    void deveLancarExcecaoSeColecaoTiverLivros() {
        Colecao colecao = new Colecao();
        colecao.setId(1);
        colecao.setLivros(List.of(new Livro()));

        when(colecaoRepository.findById(anyInt())).thenReturn(java.util.Optional.of(colecao));

        assertThrows(DataIntegrityViolationException.class, () -> colecaoService.deleteColecaoById(1));
    }
}
