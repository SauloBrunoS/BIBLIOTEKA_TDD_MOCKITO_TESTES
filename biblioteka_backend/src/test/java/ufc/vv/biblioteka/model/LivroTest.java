package ufc.vv.biblioteka.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Assume;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.NumericChars;
import net.jqwik.api.lifecycle.BeforeProperty;
import ufc.vv.biblioteka.exception.LivroIndisponivelException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

 class LivroTest {

        private Validator validator;

        @BeforeEach
        @BeforeProperty
         void setUp() {
                ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
                validator = factory.getValidator();
        }

        @Test
         void testTituloNulo() {
                Livro livro = new Livro(null,
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                "9788555780219", LocalDate.of(1949, 1, 1), 10, 400,
                                List.of(new Colecao("Livros distópicos")));

                Set<ConstraintViolation<Livro>> violations = validator.validate(livro);
                for (ConstraintViolation<Livro> violation : violations) {
                        System.out.println(violation);
                }
                assertEquals(1, violations.size());
                assertEquals("Título não pode ser nulo ou vazio", violations.iterator().next().getMessage());
        }

        @Test
         void testTituloVazio() {
                Livro livro = new Livro("   ",
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                "9788555780219", LocalDate.of(1949, 1, 1), 10, 400,
                                List.of(new Colecao("Livros distópicos")));

                Set<ConstraintViolation<Livro>> violations = validator.validate(livro);

                assertEquals(1, violations.size());
                assertEquals("Título não pode ser nulo ou vazio", violations.iterator().next().getMessage());
        }

        @Property
        void testTituloComCaracteresInvalidos(@ForAll("validTitlePart") String validTitlePart,
                        @ForAll("invalidCharacters") String invalidChar) {

                String titulo = validTitlePart + invalidChar;

                Livro livro = new Livro(
                                titulo,
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                "9788555780219", LocalDate.of(1949, 1, 1), 10, 400,
                                List.of(new Colecao("Livros distópicos")));

                Set<ConstraintViolation<Livro>> violations = validator.validate(livro);

                assertEquals(1, violations.size());
                assertEquals("O título deve conter apenas letras, números, espaços, apóstrofos e hífens.",
                                violations.iterator().next().getMessage());
        }

        @Provide
        Arbitrary<String> validTitlePart() {
                return Arbitraries.strings()
                                .withChars("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 '-")
                                .ofMinLength(2)
                                .ofMaxLength(95);
        }

        @Provide
        Arbitrary<String> invalidCharacters() {
                return Arbitraries.strings()
                                .ofMinLength(1)
                                .ofMaxLength(5)
                                .filter(str -> !str.matches("^[\\p{L}\\p{N}'\\-\\s]+$"));
        }

        @ParameterizedTest(name = "{index} => titulo={0}, expectedViolations={1}, expectedMessage={2}")
        @CsvSource({
                        // Título com pouca quantidade de caracteres
                        "'19', 1, 'O título deve ter entre 3 e 100 caracteres.'",
                        // Título com a quantidade mínima de caracteres
                        "'198', 0, ''",
                        // Título com a quantidade máxima de caracteres
                        "'The Extraordinary Journey of an Unlikely Hero Who Discovered the Secrets of the Hidden World Beneath', 0, ''",
                        // Título com grande quantidade de caracteres
                        "'The Extraordinary Journey of an Unlikely Hero Who Discovered the Secrets of the Hidden World Above It', 1, 'O título deve ter entre 3 e 100 caracteres.'"
        })
         void testTituloLivro(String titulo, int expectedViolations, String expectedMessage) {
                Livro livro = new Livro(
                                titulo,
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                "9788555780219", LocalDate.of(1949, 1, 1), 10, 400,
                                List.of(new Colecao("Livros distópicos")));

                Set<ConstraintViolation<Livro>> violations = validator.validate(livro);

                assertEquals(expectedViolations, violations.size());
                if (expectedViolations > 0) {
                        assertEquals(expectedMessage, violations.iterator().next().getMessage());
                }
        }

        @Test
         void testIsbnNulo() {
                Livro livro = new Livro("1984",
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                null, LocalDate.of(1949, 1, 1), 10, 400, List.of(new Colecao("Livros distópicos")));

                Set<ConstraintViolation<Livro>> violations = validator.validate(livro);

                assertEquals(1, violations.size());
                assertEquals("ISBN não pode ser nulo", violations.iterator().next().getMessage());
        }

        @Test
         void testIsbnVazio() {
                Livro livro = new Livro("1984",
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                "             ", LocalDate.of(1949, 1, 1), 10, 400,
                                List.of(new Colecao("Livros distópicos")));

                Set<ConstraintViolation<Livro>> violations = validator.validate(livro);

                assertEquals(1, violations.size());

                assertEquals("ISBN deve ter exatamente 10 dígitos, podendo opcionalmente ter um X no final, ou 13 dígitos",
                                violations.iterator().next().getMessage());

        }

        private Livro criarLivroComIsbn(String isbn) {
                return new Livro("1984",
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                isbn, LocalDate.of(1949, 1, 1), 10, 400, List.of(new Colecao("Livros distópicos")));
        }

        @Property
        @Label("Teste de ISBN com formato incorreto (não 10 ou 13 dígitos)")
        void testIsbnFormatoIncorreto(@ForAll @NumericChars String isbn) {
                Assume.that(isbn.length() != 10 && isbn.length() != 13);

                Livro livro = criarLivroComIsbn(isbn);
                Set<ConstraintViolation<Livro>> violations = validator.validate(livro);

                Assertions.assertThat(violations).anyMatch(v -> v.getMessage().equals(
                                "ISBN deve ter exatamente 10 dígitos, podendo opcionalmente ter um X no final, ou 13 dígitos"));
        }

        @Test
         void testIsbnComFormatoCorreto_10digitos() {
                Livro livro = new Livro(
                                "1984",
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                "850115444X", LocalDate.of(1949, 1, 1), 10, 400,
                                List.of(new Colecao("Livros distópicos")));

                Set<ConstraintViolation<Livro>> violations = validator.validate(livro);

                assertEquals(0, violations.size());
        }

        @Test
         void testIsbnComFormatoIncorreto_9DigitosECaractereDiferenteDeXNoFinal() {
                Livro livro = new Livro(
                                "1984",
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                "123456789a", LocalDate.of(1949, 1, 1), 10, 400,
                                List.of(new Colecao("Livros distópicos")));

                Set<ConstraintViolation<Livro>> violations = validator.validate(livro);

                assertEquals(1, violations.size());
                assertEquals(
                                "ISBN deve ter exatamente 10 dígitos, podendo opcionalmente ter um X no final, ou 13 dígitos",
                                violations.iterator().next().getMessage());
        }

        @Test
         void testIsbnComFormatoIncorreto_12DigitosEXNoFinal() {
                Livro livro = new Livro(
                                "1984",
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                "123456789012X", LocalDate.of(1949, 1, 1), 10, 400,
                                List.of(new Colecao("Livros distópicos")));

                Set<ConstraintViolation<Livro>> violations = validator.validate(livro);

                assertEquals(1, violations.size());
                assertEquals("ISBN deve ter exatamente 10 dígitos, podendo opcionalmente ter um X no final, ou 13 dígitos",
                                violations.iterator().next().getMessage());
        }

        @Test
         void testIsbnComFormatoIncorreto_NaoDigitosNoMeio() {
                Livro livro = new Livro(
                                "1984",
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                "12X4567890", LocalDate.of(1949, 1, 1), 10, 400,
                                List.of(new Colecao("Livros distópicos")));

                Set<ConstraintViolation<Livro>> violations = validator.validate(livro);

                assertEquals(1, violations.size());
                assertEquals(
                                "ISBN deve ter exatamente 10 dígitos, podendo opcionalmente ter um X no final, ou 13 dígitos",
                                violations.iterator().next().getMessage());
        }

        @Test
         void testDataacaoFutura() {
                Livro livro = new Livro("1984",
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                "9788555780219", LocalDate.now().plusDays(1), 10, 400,
                                List.of(new Colecao("Livros distópicos")));

                Set<ConstraintViolation<Livro>> violations = validator.validate(livro);

                assertEquals(1, violations.size());
                assertEquals("Data de publicação deve ser passada", violations.iterator().next().getMessage());
        }

        @Test
         void testDataacaoNula() {
                Livro livro = new Livro("1984",
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                "123456789X", null, 10, 400, List.of(new Colecao("Livros distópicos")));

                Set<ConstraintViolation<Livro>> violations = validator.validate(livro);

                assertEquals(1, violations.size());
                assertEquals("A data de publicação não pode ser nula", violations.iterator().next().getMessage());
        }

        @Test
         void testDataacaoHoje() {
                Livro livro = new Livro("1984",
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                "9788555780219", LocalDate.now(), 10, 400,
                                List.of(new Colecao("Livros distópicos")));

                Set<ConstraintViolation<Livro>> violations = validator.validate(livro);

                assertEquals(1, violations.size());
                assertEquals("Data de publicação deve ser passada", violations.iterator().next().getMessage());
        }

        @Test
         void testNumeroCopiasNegativo() {
                Livro livro = new Livro("1984",
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                "9788555780219", LocalDate.of(1949, 1, 1), -1, 400,
                                List.of(new Colecao("Livros distópicos")));

                Set<ConstraintViolation<Livro>> violations = validator.validate(livro);

                assertEquals(2, violations.size());
                assertTrue(violations.stream().anyMatch(v -> v.getMessage()
                                .equals("O número de cópias disponíveis deve ser maior ou igual a zero")));
                assertTrue(violations.stream().anyMatch(v -> v.getMessage()
                                .equals("O número de cópias totais deve ser maior ou igual a zero")));
        }

        @Test
         void testNumeroCopiaZero() {
                Livro livro = new Livro("1984",
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                "9788555780219", LocalDate.of(1949, 1, 1), 0, 400,
                                List.of(new Colecao("Livros distópicos")));

                Set<ConstraintViolation<Livro>> violations = validator.validate(livro);

                assertEquals(0, violations.size());
        }

        @Test
         void testQtdPaginasZero() {
                Livro livro = new Livro("1984",
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                "9788555780219", LocalDate.of(1949, 1, 1), 3, 0,
                                List.of(new Colecao("Livros distópicos")));

                Set<ConstraintViolation<Livro>> violations = validator.validate(livro);

                assertEquals(1, violations.size());
                assertEquals("A quantidade de páginas deve ser maior que zero",
                                violations.iterator().next().getMessage());
        }

        @Test
         void testQtdPaginasUm() {
                Livro livro = new Livro("1984",
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                "9788555780219", LocalDate.of(1949, 1, 1), 3, 1,
                                List.of(new Colecao("Livros distópicos")));

                Set<ConstraintViolation<Livro>> violations = validator.validate(livro);

                assertEquals(0, violations.size());
        }

        @Test
         void testLivroListaAutoresNula() {
                Livro livro = new Livro("1984",
                                null,
                                "9788555780219", LocalDate.of(1949, 1, 1), 3, 200,
                                List.of(new Colecao("Livros distópicos")));

                Set<ConstraintViolation<Livro>> violations = validator.validate(livro);

                assertEquals(1, violations.size());
                assertEquals("A lista de autores não pode ser nula", violations.iterator().next().getMessage());
        }

        @Test
         void testLivroListaAutoresVazia() {
                Livro livro = new Livro("1984",
                                Collections.emptyList(),
                                "9788555780219", LocalDate.of(1949, 1, 1), 3, 200,
                                List.of(new Colecao("Livros distópicos")));

                Set<ConstraintViolation<Livro>> violations = validator.validate(livro);

                assertEquals(1, violations.size());
                assertEquals("O livro deve ter pelo menos um autor", violations.iterator().next().getMessage());
        }

        @Test
         void testLivroListaAutoresMaisDeUmAutor() {
                Livro livro = new Livro("1984",
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA),
                                                new Autor("Joaquim Maria Machado de Assis", LocalDate.of(1896, 4, 8),
                                                                Nacionalidade.BRASIL)),
                                "9788555780219", LocalDate.of(1949, 1, 1), 3, 200,
                                List.of(new Colecao("Livros distópicos")));

                Set<ConstraintViolation<Livro>> violations = validator.validate(livro);

                assertEquals(0, violations.size());
        }

        @Test
         void testLivroListaColecoesNula() {
                Livro livro = new Livro("1984",
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                "9788555780219", LocalDate.of(1949, 1, 1), 3, 200, null);

                Set<ConstraintViolation<Livro>> violations = validator.validate(livro);

                assertEquals(1, violations.size());
                assertEquals("A lista de coleções não pode ser nula", violations.iterator().next().getMessage());
        }

        @Test
         void testLivroListaColecoesVazia() {
                Livro livro = new Livro("1984",
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                "9788555780219", LocalDate.of(1949, 1, 1), 3, 200, Collections.emptyList());

                Set<ConstraintViolation<Livro>> violations = validator.validate(livro);

                assertEquals(1, violations.size());
                assertEquals("O livro deve pertencer a pelo menos uma coleção",
                                violations.iterator().next().getMessage());
        }

        @Test
         void testLivroListaColecoesComDuasColecoes() {
                Livro livro = new Livro("1984",
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                "9788555780219", LocalDate.of(1949, 1, 1), 3, 200,
                                List.of(new Colecao("Livros distópicos"), new Colecao("Clássicos da literatura")));

                Set<ConstraintViolation<Livro>> violations = validator.validate(livro);

                assertEquals(0, violations.size());
                assertEquals(List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                livro.getAutores());
                assertEquals(List.of(new Colecao("Livros distópicos"), new Colecao("Clássicos da literatura")),
                                livro.getColecoes());
                assertEquals("1984", livro.getTitulo());
                assertEquals("9788555780219", livro.getIsbn());
                assertEquals(LocalDate.of(1949, 1, 1), livro.getDataPublicacao());
                assertEquals(200, livro.getQtdPaginas());
                assertEquals(3, livro.getNumeroCopiasDisponiveis());
                assertEquals(3, livro.getNumeroCopiasTotais());
        }

        @Test
         void testLivroValido() {
                Livro livro = new Livro(
                                "1984",
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                "9788555780219", LocalDate.of(1949, 1, 1), 10, 400,
                                List.of(new Colecao("Livros distópicos")));

                Set<ConstraintViolation<Livro>> violations = validator.validate(livro);

                assertTrue(violations.isEmpty());
        }

        @Test
         void testEmprestarLivroComCopiaDisponivel() {
                Livro livro = new Livro("1984",
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                "9788555780219", LocalDate.of(1949, 1, 1), 1, 200, List.of(new Colecao("Clássicos")));

                int copiasDisponiveisAntes = livro.getNumeroCopiasDisponiveis();

                livro.emprestarLivro();

                assertEquals(copiasDisponiveisAntes - 1, livro.getNumeroCopiasDisponiveis());
        }

        @Test
         void testEmprestarLivroSemCopiaDisponivel() {
                Livro livro = new Livro("1984",
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                "9788555780219", LocalDate.of(1949, 1, 1), 0, 200, List.of(new Colecao("Clássicos")));

                assertThrows(LivroIndisponivelException.class, livro::emprestarLivro);
                assertTrue(livro.getNumeroCopiasDisponiveis() == 0);
        }

        @Test
         void testEmprestarEDevolverLivro() {
                Livro livro = new Livro("1984",
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                "9788555780219", LocalDate.of(1949, 1, 1), 3, 200, List.of(new Colecao("Clássicos")));

                int copiasDisponiveisAntes = livro.getNumeroCopiasDisponiveis();

                livro.emprestarLivro();
                assertEquals(copiasDisponiveisAntes - 1, livro.getNumeroCopiasDisponiveis());
                livro.devolverLivro();
                assertEquals(copiasDisponiveisAntes, livro.getNumeroCopiasDisponiveis());
        }

        @Test
         void testDevolverLivroSemEmprestimo() {
                Livro livro = new Livro("1984",
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                "9788555780219", LocalDate.of(1949, 1, 1), 3, 200, List.of(new Colecao("Clássicos")));

                assertThrows(IllegalStateException.class, livro::devolverLivro);
        }

        @Test
         void testEmprestarAteExaurirCopias() {
                Livro livro = new Livro("1984",
                                List.of(new Autor("George Orwell", LocalDate.of(1907, 1, 1), Nacionalidade.INGLATERRA)),
                                "9788555780219", LocalDate.of(1949, 1, 1), 2, 200, List.of(new Colecao("Clássicos")));

                livro.emprestarLivro();
                livro.emprestarLivro();

                assertThrows(LivroIndisponivelException.class, livro::emprestarLivro);
        }

}