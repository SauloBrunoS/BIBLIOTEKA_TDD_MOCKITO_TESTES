package ufc.vv.biblioteka.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ufc.vv.biblioteka.model.Livro;
import ufc.vv.biblioteka.repository.EmprestimoRepository;
import ufc.vv.biblioteka.repository.LeitorRepository;
import ufc.vv.biblioteka.repository.LivroRepository;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class EmprestimoServiceTest {

    @InjectMocks
    private EmprestimoService emprestimoService;

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private LivroRepository livroRepository;

    @Mock
    private LeitorRepository leitorRepository;

    @Mock
    private UsuarioService usuarioService;

    @BeforeEach
    void setup() {
    }

    @Test
    void testEmprestarLivroLivroNaoEncontrado() {
        when(livroRepository.findById(anyInt())).thenReturn(Optional.empty());
        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> {
            emprestimoService.emprestarLivro(1, 1, "senha");
        });
    }

    @Test
    void testEmprestarLivroLeitorNaoEncontrado() {
        when(livroRepository.findById(anyInt())).thenReturn(Optional.of(new Livro()));
        when(leitorRepository.findById(anyInt())).thenReturn(Optional.empty());
        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> {
            emprestimoService.emprestarLivro(1, 1, "senha");
        });
    }

    @Test
    void testRenovarEmprestimoNaoEncontrado() {
        when(emprestimoRepository.findById(anyInt())).thenReturn(Optional.empty());
        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> {
            emprestimoService.renovarEmprestimo(1, "senha");
        });
    }

    @Test
    void testDevolverLivroEmprestimoNaoEncontrado() {
        when(emprestimoRepository.findById(anyInt())).thenReturn(Optional.empty());
        assertThrows(jakarta.persistence.EntityNotFoundException.class, () -> {
            emprestimoService.devolverLivro(1, "senha");
        });
    }
}

