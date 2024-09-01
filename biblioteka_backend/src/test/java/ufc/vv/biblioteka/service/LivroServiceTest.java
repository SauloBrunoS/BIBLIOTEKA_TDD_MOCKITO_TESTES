package ufc.vv.biblioteka.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import ufc.vv.biblioteka.model.Emprestimo;
import ufc.vv.biblioteka.model.Livro;
import ufc.vv.biblioteka.model.StatusReserva;
import ufc.vv.biblioteka.repository.AutorRepository;
import ufc.vv.biblioteka.repository.ColecaoRepository;
import ufc.vv.biblioteka.repository.EmprestimoRepository;
import ufc.vv.biblioteka.repository.LivroRepository;
import ufc.vv.biblioteka.repository.ReservaRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LivroServiceTest {

    @InjectMocks
    private LivroService livroService;

    @Mock
    private LivroRepository livroRepository;

    @Mock
    private AutorRepository autorRepository;

    @Mock
    private ColecaoRepository colecaoRepository;

    @Mock
    private EmprestimoRepository emprestimorRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAdicionarLivroSucesso() {
        Livro livro = new Livro();
        livro.setTitulo("Teste Livro");
        livro.setIsbn("978-3-16-148410-0");
        livro.setDataPublicacao(LocalDate.now());
        livro.setNumeroCopiasDisponiveis(5);
        livro.setNumeroCopiasTotais(10);
        livro.setQtdPaginas(100);
        livro.setAutores(Collections.emptyList());
        livro.setColecoes(Collections.emptyList());

        when(autorRepository.findAllById(anyList())).thenReturn(Collections.emptyList());
        when(colecaoRepository.findAllById(anyList())).thenReturn(Collections.emptyList());
        when(livroRepository.save(livro)).thenReturn(livro);

        Livro resultado = livroService.adicionarLivro(livro);

        assertNotNull(resultado);
        assertEquals(livro.getTitulo(), resultado.getTitulo());
        verify(livroRepository, times(1)).save(livro);
    }

    @Test
    void testAtualizarLivroComSucesso() {
        Livro livroExistente = new Livro();
        livroExistente.setId(1);
        livroExistente.setNumeroCopiasTotais(10);
        livroExistente.setNumeroCopiasDisponiveis(5);

        Livro livroAtualizado = new Livro();
        livroAtualizado.setId(1);
        livroAtualizado.setNumeroCopiasTotais(15);

        when(livroRepository.findById(1)).thenReturn(Optional.of(livroExistente));
        when(emprestimorRepository.countByDevolvidoFalse()).thenReturn(0);
        when(reservaRepository.countByStatus(StatusReserva.EM_ANDAMENTO)).thenReturn(0);
        when(livroRepository.save(livroAtualizado)).thenReturn(livroAtualizado);

        Livro resultado = livroService.atualizarLivro(1, livroAtualizado);

        assertNotNull(resultado);
        assertEquals(15, resultado.getNumeroCopiasTotais());
        assertEquals(10, resultado.getNumeroCopiasDisponiveis());
        verify(livroRepository, times(1)).save(livroAtualizado);
    }

    @Test
    void testExcluirLivroComEmpréstimos() {
        Livro livro = new Livro();
        livro.setId(1);
        livro.setEmprestimos(Collections.singletonList(mock(Emprestimo.class)));

        when(livroRepository.findById(1)).thenReturn(Optional.of(livro));

        assertThrows(DataIntegrityViolationException.class, () -> livroService.excluirLivro(1));

        verify(livroRepository, never()).delete(livro);
    }

    @Test
    void testValidarLivroComISBNInvalido() {
        Livro livro = new Livro();
        livro.setTitulo("Teste Livro");
        livro.setIsbn("123");
        livro.setDataPublicacao(LocalDate.now());
        livro.setNumeroCopiasDisponiveis(5);
        livro.setNumeroCopiasTotais(10);
        livro.setQtdPaginas(100);
        livro.setAutores(Collections.emptyList());
        livro.setColecoes(Collections.emptyList());

        assertThrows(IllegalArgumentException.class, () -> livroService.adicionarLivro(livro));
    }
}
