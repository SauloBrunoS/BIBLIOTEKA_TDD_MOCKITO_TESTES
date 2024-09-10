package ufc.vv.biblioteka.services;

import org.apache.commons.validator.routines.ISBNValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import ufc.vv.biblioteka.exception.LimiteExcedidoException;
import ufc.vv.biblioteka.model.Autor;
import ufc.vv.biblioteka.model.Colecao;
import ufc.vv.biblioteka.model.Emprestimo;
import ufc.vv.biblioteka.model.Livro;
import ufc.vv.biblioteka.model.Nacionalidade;
import ufc.vv.biblioteka.repository.AutorRepository;
import ufc.vv.biblioteka.repository.ColecaoRepository;
import ufc.vv.biblioteka.repository.LivroRepository;
import ufc.vv.biblioteka.service.GerenciadorReserva;
import ufc.vv.biblioteka.service.LivroService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LivroServiceTest {

    @Mock
    private LivroRepository livroRepository;

    @Mock
    private GerenciadorReserva gerenciadorReserva;

    @Mock
    private AutorRepository autorRepository;

    @Mock
    private ColecaoRepository colecaoRepository;

    private ISBNValidator isbnValidator;

    private LivroService livroService;

    private Autor autor, autorPassado;

    private Colecao colecao, colecaoPassada;

    @BeforeEach
    void setUp() {
        isbnValidator = new ISBNValidator();
        livroService = new LivroService(livroRepository, gerenciadorReserva, isbnValidator, autorRepository,
                colecaoRepository);
        autor = new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA);
        colecao = new Colecao("Clássicos Literários",
                "A coleção é um tesouro literário que reúne obras fundamentais que moldaram a cultura e a literatura ocidental ao longo dos séculos.");
        autorPassado = new Autor();
        autorPassado.setId(1);
        colecaoPassada = new Colecao();
        colecaoPassada.setId(1);
        autor.setId(1);
        colecao.setId(1);
    }

    @Test
    void testAdicionarLivro_Success() {
        Livro livro = new Livro("1984",
                List.of(autorPassado),
                "9788555780219", LocalDate.of(1949, 1, 1), 5, 400,
                List.of(colecaoPassada));

        when(autorRepository.findAllById(livro.getAutores().stream()
                .map(Autor::getId)
                .collect(Collectors.toList()))).thenReturn(List.of(autor));
        when(colecaoRepository.findAllById(livro.getColecoes().stream()
                .map(Colecao::getId)
                .collect(Collectors.toList()))).thenReturn(List.of(colecao));
        when(livroRepository.save(livro)).thenReturn(livro);
        Livro livroSalvo = livroService.adicionarLivro(livro);

        assertEquals(livroSalvo.getAutores(), List.of(autor));
        assertEquals(livroSalvo.getColecoes(), List.of(colecao));

        verify(livroRepository, times(1)).save(livro);
    }

    @Test
    void testAdicionarLivro_ComISBNInvalido_13digitos() {

        Livro livro = new Livro("1984",
                List.of(autorPassado),
                "1234567890123", LocalDate.of(1949, 1, 1), 5, 400,
                List.of(colecaoPassada));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> livroService.adicionarLivro(livro));

        assertEquals("O ISBN informado é inválido.", exception.getMessage());
        verify(livroRepository, never()).save(any(Livro.class));
    }

    @Test
    void testAdicionarLivro_ComISBNInvalido_10digitos() {

        Livro livro = new Livro("1984",
                List.of(autorPassado),
                "123454789X", LocalDate.of(1949, 1, 1), 5, 400,
                List.of(colecaoPassada));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> livroService.adicionarLivro(livro));

        assertEquals("O ISBN informado é inválido.", exception.getMessage());
        verify(livroRepository, never()).save(any(Livro.class));
    }

    @Test
    void testAdicionarLivro_ComISBNDuplicado() {

        Livro livro = new Livro("1984",
                List.of(autorPassado),
                "9788555780219", LocalDate.of(1949, 1, 1), 5, 400,
                List.of(colecaoPassada));

        when(livroRepository.existsByIsbn(livro.getIsbn())).thenReturn(true);

        assertThrows(DuplicateKeyException.class,
                () -> livroService.adicionarLivro(livro));

        verify(livroRepository, never()).save(any(Livro.class));
    }

    @Test
    void testAtualizarLivro_Success() {
        Livro livroAntigo = new Livro("1984", List.of(autor), "9788555780219", LocalDate.of(1949, 1, 1), 5, 400,
                List.of(colecao));

        Autor novoAutor = new Autor("Joaquim Maria Machado de Assis", LocalDate.of(1896, 8, 1), Nacionalidade.BRASIL);
        autor.setId(2);
        Colecao novaColecao = new Colecao("Livros de Suspense", "Coleção de livros de suspense.");
        novaColecao.setId(2);
        Autor novoAutorVazio = new Autor();
        novoAutorVazio.setId(2);
        Colecao novaColecaoVazia = new Colecao();
        novaColecaoVazia.setId(2);
        Livro livroAtualizado = new Livro("Dom Casmurro", List.of(novoAutorVazio), "857666514X",
                LocalDate.of(1949, 1, 1), 10,
                405, List.of(novaColecaoVazia));

        when(livroRepository.findById(1)).thenReturn(Optional.of(livroAntigo));
        when(autorRepository.findAllById(livroAtualizado.getAutores().stream()
                .map(Autor::getId)
                .collect(Collectors.toList()))).thenReturn(List.of(novoAutor));
        when(colecaoRepository.findAllById(livroAtualizado.getColecoes().stream()
                .map(Colecao::getId)
                .collect(Collectors.toList()))).thenReturn(List.of(novaColecao));
        when(livroRepository.save(livroAtualizado)).thenReturn(livroAtualizado);

        Livro resultado = livroService.atualizarLivro(1, livroAtualizado);

        assertEquals(livroAtualizado.getTitulo(), resultado.getTitulo());
        assertEquals(livroAtualizado.getIsbn(), resultado.getIsbn());
        assertEquals(livroAtualizado.getDataPublicacao(), resultado.getDataPublicacao());
        assertEquals(livroAtualizado.getNumeroCopiasDisponiveis(), resultado.getNumeroCopiasDisponiveis());
        assertEquals(livroAtualizado.getNumeroCopiasTotais(), resultado.getNumeroCopiasTotais());
        assertEquals(livroAtualizado.getQtdPaginas(), resultado.getQtdPaginas());
        assertEquals(livroAtualizado.getAutores(), resultado.getAutores());
        assertEquals(livroAtualizado.getColecoes(), resultado.getColecoes());

        verify(gerenciadorReserva).ajustarReservasParaNovoNumeroDeCopias(livroAntigo, livroAtualizado);
        verify(livroRepository, times(1)).save(livroAtualizado);
    }

    @Test
    void testAtualizarLivro_ComISBNInvalido_13digitos() {
        Livro livroAntigo = new Livro("1984", List.of(autor), "9788555780219", LocalDate.of(1949, 1, 1), 5, 400,
                List.of(colecao));
        Livro livroAtualizado = new Livro("1984", List.of(autorPassado), "1234567890123", LocalDate.of(1949, 1, 1), 5,
                400, List.of(colecaoPassada));

        when(livroRepository.findById(1)).thenReturn(Optional.of(livroAntigo));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> livroService.atualizarLivro(1, livroAtualizado));

        assertEquals("O ISBN informado é inválido.", exception.getMessage());
        verify(livroRepository, never()).save(any(Livro.class));
    }

    @Test
    void testAtualizarLivro_ComISBNInvalido_10digitos() {
        Livro livroAntigo = new Livro("1984", List.of(autor), "9788555780219", LocalDate.of(1949, 1, 1), 5, 400,
                List.of(colecao));
        Livro livroAtualizado = new Livro("1984", List.of(autorPassado), "123454789X", LocalDate.of(1949, 1, 1), 5,
                400, List.of(colecaoPassada));

        when(livroRepository.findById(1)).thenReturn(Optional.of(livroAntigo));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> livroService.atualizarLivro(1, livroAtualizado));

        assertEquals("O ISBN informado é inválido.", exception.getMessage());
        verify(livroRepository, never()).save(any(Livro.class));
    }

    @Test
    void testAtualizarLivro_ComISBNDuplicado() {
        Livro livroAntigo = new Livro("1984", List.of(autor), "9788555780219", LocalDate.of(1949, 1, 1), 5, 400,
                List.of(colecao));
        Livro livroAtualizado = new Livro("1984", List.of(autorPassado), "9781234567897", LocalDate.of(1949, 1, 1), 5,
                400, List.of(colecaoPassada));

        when(livroRepository.findById(1)).thenReturn(Optional.of(livroAntigo));
        when(livroRepository.existsByIsbn(livroAtualizado.getIsbn())).thenReturn(true);

        assertThrows(DuplicateKeyException.class,
                () -> livroService.atualizarLivro(1, livroAtualizado));

        verify(livroRepository, never()).save(any(Livro.class));
    }

    @Test
    void testAtualizarLivro_AjustarReservasParaNovoNumeroDeCopias_Excecao() {
        Livro livroAntigo = new Livro("1984", List.of(autor), "9788555780219",
                LocalDate.of(1949, 1, 1), 5, 400, List.of(colecao));
        livroAntigo.setId(1);

        Livro livroAtualizado = new Livro("1984", List.of(autorPassado), "9788555780219",
                LocalDate.of(1949, 1, 1), 2, 400, List.of(colecaoPassada)); // Reduzindo o número de cópias

        when(livroRepository.findById(livroAntigo.getId())).thenReturn(Optional.of(livroAntigo));
        doThrow(new LimiteExcedidoException(
                "A quantidade de cópias do livro não pode ser reduzida a essa quantidade por ser menor que a quantidade de cópias desse livro que estão relacionadas a empréstimos e reservas em andamento"))
                .when(gerenciadorReserva).ajustarReservasParaNovoNumeroDeCopias(livroAntigo, livroAtualizado);

        assertThrows(LimiteExcedidoException.class,
                () -> livroService.atualizarLivro(livroAntigo.getId(), livroAtualizado));

        verify(gerenciadorReserva, times(1)).ajustarReservasParaNovoNumeroDeCopias(livroAntigo, livroAtualizado);
        verify(livroRepository, never()).save(any(Livro.class));
    }

    @Test
    void testExcluirLivro_AssociadoComEmprestimosOuReservas() {
        Livro livroAntigo = new Livro("1984", List.of(autor), "9788555780219",
                LocalDate.of(1949, 1, 1), 5, 400, List.of(colecao));
        livroAntigo.setId(1);
        livroAntigo.setEmprestimos(List.of(new Emprestimo())); // Simula empréstimos associados.

        when(livroRepository.findById(1)).thenReturn(Optional.of(livroAntigo));

        assertThrows(DataIntegrityViolationException.class,
                () -> livroService.excluirLivro(1));

        verify(livroRepository, never()).delete(any(Livro.class));
    }

    @Test
    void testExcluirLivro_Success() {
        Livro livroAntigo = new Livro("1984", List.of(autor), "9788555780219",
                LocalDate.of(1949, 1, 1), 5, 400, List.of(colecao));
        livroAntigo.setId(1);

        when(livroRepository.findById(1)).thenReturn(Optional.of(livroAntigo));

        livroService.excluirLivro(1);

        verify(livroRepository, times(1)).delete(livroAntigo);
    }
}
