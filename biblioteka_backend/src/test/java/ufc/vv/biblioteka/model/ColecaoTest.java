package ufc.vv.biblioteka.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.lifecycle.BeforeProperty;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;

 class ColecaoTest {

    private Validator validator;

    @BeforeEach
    @BeforeProperty
     void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
     void testNomeNulo() {
        Colecao colecao = new Colecao(null,
                "A coleção é um tesouro literário que reúne obras fundamentais que moldaram a cultura e a literatura ocidental ao longo dos séculos.");

        Set<ConstraintViolation<Colecao>> violations = validator.validate(colecao);

        assertEquals(1, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Nome não pode ser nulo ou vazio")));
    }

    @Test
     void testNomeVazio() {
        Colecao colecao = new Colecao("     ",
                "A coleção é um tesouro literário que reúne obras fundamentais que moldaram a cultura e a literatura ocidental ao longo dos séculos.");

        Set<ConstraintViolation<Colecao>> violations = validator.validate(colecao);

        assertEquals(1, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Nome não pode ser nulo ou vazio")));
    }

    @ParameterizedTest
    @MethodSource("colecaoProvider")
     void testNomeColecao(String nome, int expectedViolationCount, String expectedMessage) {
        Colecao colecao = new Colecao(
                nome,
                "A coleção é um tesouro literário que reúne obras fundamentais que moldaram a cultura e a literatura ocidental ao longo dos séculos.");

        Set<ConstraintViolation<Colecao>> violations = validator.validate(colecao);

        assertEquals(expectedViolationCount, violations.size());
        if (expectedViolationCount > 0) {
            assertEquals(expectedMessage, violations.iterator().next().getMessage());
        }
    }

    private static Stream<Arguments> colecaoProvider() {
        return Stream.of(
                Arguments.of("AB", 1, "O nome deve ter entre 3 e 100 caracteres."), // Teste do nome muito curto
                Arguments.of("ABC", 0, null), // Teste do nome com tamanho mínimo permitido
                Arguments.of(
                        "Coleção Magnífica dos Clássicos Literários e Históricos da Humanidade: Uma Jornada Através dos Século",
                        1,
                        "O nome deve ter entre 3 e 100 caracteres."), // Teste do nome muito grande
                Arguments.of(
                        "Coleção Magnífica dos Clássicos Literários e Históricos da Humanidade: Uma Jornada Através dos Sécul",
                        0,
                        null) // Teste do nome com tamanho máximo permitido
        );
    }

    @Property
    void testNomeComCaracteresInvalidos(@ForAll("validNamePart") String validNamePart,
            @ForAll("invalidCharacters") String invalidChar) {

        String nome = validNamePart + invalidChar;

        Colecao colecao = new Colecao(nome,
                "A coleção é um tesouro literário que reúne obras fundamentais que moldaram a cultura e a literatura ocidental ao longo dos séculos.");

        Set<ConstraintViolation<Colecao>> violations = validator.validate(colecao);

        assertEquals(1, violations.size());
        assertEquals(
                "O nome pode conter apenas letras, números, espaços, apóstrofos, hífens, pontos, vírgulas, parênteses, barras, dois pontos e ponto e vírgula.",
                violations.iterator().next().getMessage());
    }

    @Provide
    Arbitrary<String> validNamePart() {
        return Arbitraries.strings()
                .withChars("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 '-.,()/;:")
                .ofMinLength(2)
                .ofMaxLength(95);
    }

    @Provide
    Arbitrary<String> invalidCharacters() {
        return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(5)
                .filter(str -> !str.matches("^[\\p{L}\\p{N}'\\-\\s.,()/:;]+$"));
    }

    @Test
     void testDescricaoNula() {
        Colecao colecao = new Colecao("Clássicos Literários", null);

        Set<ConstraintViolation<Colecao>> violations = validator.validate(colecao);

        assertEquals(1, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Descrição não pode ser nula ou vazia")));
    }

    @Test
     void testDescricaoVazia() {
        Colecao colecao = new Colecao("Clássicos Literários", "                 ");

        Set<ConstraintViolation<Colecao>> violations = validator.validate(colecao);

        assertEquals(1, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Descrição não pode ser nula ou vazia")));
    }

    @ParameterizedTest
    @MethodSource("colecaoDescricaoProvider")
     void testDescricaoColecao(String nome, String descricao, int expectedViolationCount, String expectedMessage) {
        Colecao colecao = new Colecao(nome, descricao);

        Set<ConstraintViolation<Colecao>> violations = validator.validate(colecao);

        assertEquals(expectedViolationCount, violations.size());

        if (expectedViolationCount > 0) {
            assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals(expectedMessage)));
        }
    }

    private static Stream<Arguments> colecaoDescricaoProvider() {
        return Stream.of(
                Arguments.of("Clássicos Literários", "Clássicos", 1, "A descrição deve ter no mínimo 10 caracteres."), // Teste da descrição muito curta
                Arguments.of("Clássicos Literários", "Clássicos.", 0, null) // Teste da descrição mínima permitida
        );
    }

    @Test
     void testValidacaoBemSucedida() {
        Colecao colecao = new Colecao("Clássicos Literários",
                "A coleção é um tesouro literário que reúne obras fundamentais que moldaram a cultura e a literatura ocidental ao longo dos séculos.");

        Set<ConstraintViolation<Colecao>> violations = validator.validate(colecao);

        assertEquals(0, violations.size());
        assertEquals("Clássicos Literários", colecao.getNome());
        assertEquals( "A coleção é um tesouro literário que reúne obras fundamentais que moldaram a cultura e a literatura ocidental ao longo dos séculos.", colecao.getDescricao());
    }

}
