package ufc.vv.biblioteka.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeProperty;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LeitorTest {

        private Validator validator;

        @BeforeEach
        @BeforeProperty
        void setUp() {
                ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
                validator = factory.getValidator();
        }

        @Test
        void testNomeCompletoNulo() {
                Leitor leitor = new Leitor(null, "88987654321", "94182774094",
                                new Usuario("saulobruno@alu.ufc.br", "Saulo10#12", TipoUsuario.LEITOR));

                Set<ConstraintViolation<Leitor>> violations = validator.validate(leitor, OnCreate.class,
                                OnUpdate.class);

                assertEquals(1, violations.size());
                assertEquals("Nome completo não pode ser nulo ou vazio", violations.iterator().next().getMessage());
        }

        @Test
        void testNomeCompletoVazio() {
                Leitor leitor = new Leitor("   ", "88987654321", "94182774094",
                                new Usuario("saulobruno@alu.ufc.br", "Saulo10#12", TipoUsuario.LEITOR));

                Set<ConstraintViolation<Leitor>> violations = validator.validate(leitor, OnCreate.class,
                                OnUpdate.class);

                assertEquals(1, violations.size());
                assertEquals("Nome completo não pode ser nulo ou vazio", violations.iterator().next().getMessage());
        }

        private static Stream<Object[]> provideTestCases() {
                return Stream.of(
                                new Object[] { "Sa", 1, "O nome completo deve ter entre 3 e 100 caracteres." }, // Nome
                                                                                                                // muito
                                                                                                                // curto
                                new Object[] { "Sau", 0, "" }, // Nome no limite mínimo permitido
                                new Object[] { "Saulo Maximiliano Alexander Sebastian Frederick Von Hohenberg III Duke of Eastwickshire and Protector",
                                                1, "O nome completo deve ter entre 3 e 100 caracteres." }, // Nome muito
                                                                                                           // longo
                                new Object[] { "Saulo Maximiliano Alexander Sebastian Frederick Von Hohenberg III Duke of Eastwikshire and Protector",
                                                0, "" } // Nome no limite máximo permitido
                );
        }

        @ParameterizedTest
        @MethodSource("provideTestCases")
        void testNomeCompleto(String nome, int expectedViolations, String expectedMessage) {
                Leitor leitor = new Leitor(nome, "88987654321", "94182774094",
                                new Usuario("saulobruno@alu.ufc.br", "Saulo10#12", TipoUsuario.LEITOR));

                Set<ConstraintViolation<Leitor>> violations = validator.validate(leitor, OnCreate.class,
                                OnUpdate.class);

                assertEquals(expectedViolations, violations.size());
                if (!violations.isEmpty()) {
                        assertEquals(expectedMessage, violations.iterator().next().getMessage());
                }
        }

        @Property
        void testNomeCompletoComCaracteresInvalidos(@ForAll("validNamePart") String validNamePart,
                        @ForAll("invalidCharacters") String invalidChar) {

                String nomeCompleto = validNamePart + invalidChar;

                Leitor leitor = new Leitor(nomeCompleto, "88987654321", "94182774094",
                                new Usuario("saulobruno@alu.ufc.br", "Saulo10#12", TipoUsuario.LEITOR));

                Set<ConstraintViolation<Leitor>> violations = validator.validate(leitor, OnCreate.class,
                                OnUpdate.class);

                assertEquals(1, violations.size());
                assertEquals("O nome completo deve conter apenas letras, espaços, apóstrofos e hífens.",
                                violations.iterator().next().getMessage());
        }

        @Provide
        Arbitrary<String> validNamePart() {
                return Arbitraries.strings()
                                .withChars("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzÀ-ÖØ-öø-ÿ '-")
                                .ofMinLength(2)
                                .ofMaxLength(95);
        }

        @Provide
        Arbitrary<String> invalidCharacters() {
                return Arbitraries.strings().ofMinLength(1).ofMaxLength(5)
                                .filter(str -> !str.matches("^[A-Za-zÀ-ÖØ-öø-ÿ'\\-\\s]+$"));
        }

        @Test
        void testTelefoneNulo() {
                Leitor leitor = new Leitor("Saulo Bruno", null, "94182774094",
                                new Usuario("saulobruno@alu.ufc.br", "Saulo10#12", TipoUsuario.LEITOR));

                Set<ConstraintViolation<Leitor>> violations = validator.validate(leitor, OnCreate.class,
                                OnUpdate.class);

                assertEquals(1, violations.size());
                assertEquals("Telefone não pode ser nulo", violations.iterator().next().getMessage());
        }

        @Test
        void testTelefoneVazio() {
                Leitor leitor = new Leitor("Saulo Bruno", "", "94182774094",
                                new Usuario("saulobruno@alu.ufc.br", "Saulo10#12", TipoUsuario.LEITOR));

                Set<ConstraintViolation<Leitor>> violations = validator.validate(leitor, OnCreate.class,
                                OnUpdate.class);

                assertEquals(1, violations.size());

                assertTrue(violations.stream()
                                .anyMatch(v -> v.getMessage().equals("Telefone deve ter exatamente 10 ou 11 dígitos")));
        }

        private static Stream<Object[]> provideTelefoneTestCases() {
                return Stream.of(
                                // Telefone com baixa quantidade de caracteres
                                new Object[] { "889876543", 1, "Telefone deve ter exatamente 10 ou 11 dígitos" },
                                // Telefone com quantidade correta de caracteres (10)
                                new Object[] { "2198765432", 0, "" },
                                // Telefone com alta quantidade de caracteres
                                new Object[] { "889876543981", 1, "Telefone deve ter exatamente 10 ou 11 dígitos" },
                                // Telefone com caracteres diferentes de números
                                new Object[] { "88987654a21", 1, "Telefone deve ter exatamente 10 ou 11 dígitos" });
        }

        @ParameterizedTest
        @MethodSource("provideTelefoneTestCases")
        void testTelefone(String telefone, int expectedViolations, String expectedMessage) {
                Leitor leitor = new Leitor("Saulo Bruno", telefone, "94182774094",
                                new Usuario("saulobruno@alu.ufc.br", "Saulo10#12", TipoUsuario.LEITOR));

                Set<ConstraintViolation<Leitor>> violations = validator.validate(leitor, OnCreate.class,
                                OnUpdate.class);

                assertEquals(expectedViolations, violations.size());
                if (!violations.isEmpty()) {
                        assertEquals(expectedMessage, violations.iterator().next().getMessage());
                }
        }

        @Test
        void testCPFNulo() {
                Leitor leitor = new Leitor("Saulo Bruno", "88987654321", null,
                                new Usuario("saulobruno@alu.ufc.br", "Saulo10#12", TipoUsuario.LEITOR));

                Set<ConstraintViolation<Leitor>> violations = validator.validate(leitor, OnCreate.class,
                                OnUpdate.class);

                assertEquals(1, violations.size());
                assertEquals("CPF não pode ser nulo ou vazio", violations.iterator().next().getMessage());

        }

        @Test
        void testCPFVazio() {
                Leitor leitor = new Leitor("Saulo Bruno", "88987654321", "",
                                new Usuario("saulobruno@alu.ufc.br", "Saulo10#12", TipoUsuario.LEITOR));

                Set<ConstraintViolation<Leitor>> violations = validator.validate(leitor, OnCreate.class,
                                OnUpdate.class);

                assertEquals(1, violations.size());
                assertEquals("CPF deve ser válido", violations.iterator().next().getMessage());

        }

        @ParameterizedTest(name = "{index} => cpf={0}")
        @CsvSource({
                        // CPF com baixo número de caracteres
                        "1234567890, CPF deve ser válido",
                        // CPF com alto número de caracteres
                        "123456789011, CPF deve ser válido",
                        // CPF com o número correto de caracteres, mas inválido
                        "12345678901, CPF deve ser válido",
                        // CPF com número correto de caracteres, mas com caracteres inválidos
                        "636641310a2, CPF deve ser válido"
        })
        void testCPFInvalido(String cpf, String expectedMessage) {
                Leitor leitor = new Leitor("Saulo Bruno", "88987654321", cpf,
                                new Usuario("saulobruno@alu.ufc.br", "Saulo10#12", TipoUsuario.LEITOR));

                Set<ConstraintViolation<Leitor>> violations = validator.validate(leitor, OnCreate.class,
                                OnUpdate.class);

                assertEquals(1, violations.size());
                assertEquals(expectedMessage, violations.iterator().next().getMessage());
        }

        @Test
        void testUsuarioNulo() {
                Leitor leitor = new Leitor("Saulo Bruno", "88987654321", "94182774094",
                                null);

                Set<ConstraintViolation<Leitor>> violations = validator.validate(leitor, OnCreate.class,
                                OnUpdate.class);

                assertEquals(1, violations.size());

                assertEquals("Usuário não pode ser nulo", violations.iterator().next().getMessage());

        }

        @Test
        void testLeitorValido() {
                Leitor leitor = new Leitor("Saulo Bruno", "88987654321", "94182774094",
                                new Usuario("saulobruno@alu.ufc.br", "Saulo10#12", TipoUsuario.LEITOR));

                Set<ConstraintViolation<Leitor>> violations = validator.validate(leitor);

                assertTrue(violations.isEmpty());
                assertEquals("Saulo Bruno", leitor.getNomeCompleto());
                assertEquals("88987654321", leitor.getTelefone());
                assertEquals("94182774094", leitor.getCpf());
                assertEquals(new Usuario("saulobruno@alu.ufc.br", "Saulo10#12", TipoUsuario.LEITOR),
                                leitor.getUsuario());

        }
}
