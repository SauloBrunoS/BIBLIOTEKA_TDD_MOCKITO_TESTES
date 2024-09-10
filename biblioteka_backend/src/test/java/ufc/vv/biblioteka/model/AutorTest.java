package ufc.vv.biblioteka.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.lifecycle.BeforeProperty;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AutorTest {

    private Validator validator;

    @BeforeEach
    @BeforeProperty
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testNomeCompletoNulo() {
        Autor autor = new Autor(null, LocalDate.of(1994, 1, 1), Nacionalidade.BRASIL);

        Set<ConstraintViolation<Autor>> violations = validator.validate(autor);

        assertEquals(1, violations.size());
        assertEquals("Nome completo não pode ser nulo ou vazio", violations.iterator().next().getMessage());
    }

    @Test
    void testNomeCompletoVazio() {
        Autor autor = new Autor("   ", LocalDate.of(1980, 1, 1), Nacionalidade.BRASIL);

        Set<ConstraintViolation<Autor>> violations = validator.validate(autor);

        assertEquals(1, violations.size());
        assertEquals("Nome completo não pode ser nulo ou vazio", violations.iterator().next().getMessage());
    }

    @ParameterizedTest
    @MethodSource("autorProvider")
    void testNomeCompleto(String nome, int expectedViolationCount, String expectedMessage) {
        Autor autor = new Autor(nome, LocalDate.of(1980, 1, 1), Nacionalidade.BRASIL);

        Set<ConstraintViolation<Autor>> violations = validator.validate(autor);

        assertEquals(expectedViolationCount, violations.size());
        if (expectedViolationCount > 0) {
            assertEquals(expectedMessage, violations.iterator().next().getMessage());
        }
    }

    private static Stream<Arguments> autorProvider() {
        return Stream.of(
                Arguments.of("Jo", 1, "O nome completo deve ter entre 3 e 100 caracteres."), // Teste do nome muito
                                                                                             // curto
                Arguments.of("Joe", 0, null), // Teste do nome com tamanho mínimo permitido
                Arguments.of(
                        "Maximiliano Alexander Sebastian Frederick Von Hohenberg III Duke of Eastwickshire and Protector of Gr",
                        1, "O nome completo deve ter entre 3 e 100 caracteres."), // Teste do nome muito grande
                Arguments.of(
                        "Maximiliano Alexander Sebastian Frederick Von Hohenberg III Duke of Eastwickshire and Protector of G",
                        0, null) // Teste do nome com tamanho máximo permitido
        );
    }

    @Property
    void testNomeCompletoComCaracteresInvalidos(@ForAll("validNamePart") String validNamePart,
            @ForAll("invalidCharacters") String invalidChar) {

        String nomeCompleto = validNamePart + invalidChar;

        Autor autor = new Autor(nomeCompleto, LocalDate.of(1980, 1, 1), Nacionalidade.BRASIL);

        Set<ConstraintViolation<Autor>> violations = validator.validate(autor);

        assertEquals(1, violations.size());
        assertEquals("O nome completo deve conter apenas letras, espaços, apóstrofos, hífens e números romanos.",
                violations.iterator().next().getMessage());
    }

    @Provide
    Arbitrary<String> validNamePart() {
        return Arbitraries.strings()
                .withChars("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz '-")
                .ofMinLength(2)
                .ofMaxLength(95);
    }

    @Provide
    Arbitrary<String> invalidCharacters() {
        return Arbitraries.strings().ofMinLength(1).ofMaxLength(5)
                .filter(str -> !str.matches("^[\\p{L}'\\-\\sIVXLCDM]+$"));
    }

    @Test
    void testDataNascimentoNula() {
        Autor autor = new Autor("José da Silva", null, Nacionalidade.BRASIL);

        Set<ConstraintViolation<Autor>> violations = validator.validate(autor);

        assertEquals(1, violations.size());
        assertEquals("Data de nascimento não pode ser nulo", violations.iterator().next().getMessage());
    }

    @Test
    void testDataNascimentoFutura() {
        Autor autor = new Autor("José da Silva", LocalDate.now().plusDays(1), Nacionalidade.BRASIL);

        Set<ConstraintViolation<Autor>> violations = validator.validate(autor);

        assertEquals(1, violations.size());
        assertEquals("A data de nascimento deve estar no passado.", violations.iterator().next().getMessage());
    }

    @Test
    void testNacionalidadeNula() {
        Autor autor = new Autor("José da Silva", LocalDate.of(1980, 1, 1), null);

        Set<ConstraintViolation<Autor>> violations = validator.validate(autor);

        assertEquals(1, violations.size());
        assertEquals("Nacionalidade não pode ser nula", violations.iterator().next().getMessage());
    }

    @Test
    void testAutorValido() {
        Autor autor = new Autor("José da Silva", LocalDate.of(1980, 1, 1), Nacionalidade.BRASIL);

        Set<ConstraintViolation<Autor>> violations = validator.validate(autor);

        assertEquals(0, violations.size());
        assertEquals("José da Silva", autor.getNomeCompleto());
        assertEquals(LocalDate.of(1980, 1, 1), autor.getDataNascimento());
        assertEquals(Nacionalidade.BRASIL, autor.getNacionalidade());
    }
}
