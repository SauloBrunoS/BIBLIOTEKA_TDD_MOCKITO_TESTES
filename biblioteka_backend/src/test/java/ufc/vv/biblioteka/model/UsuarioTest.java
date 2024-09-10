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
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.Chars;
import net.jqwik.api.constraints.NumericChars;
import net.jqwik.api.lifecycle.BeforeProperty;
import net.jqwik.web.api.Email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioTest {

    private Validator validator;

    @BeforeEach
    @BeforeProperty
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testEmailNaoPodeSerNulo() {
        Usuario usuario = new Usuario(null, "Senha@123", TipoUsuario.LEITOR);

        Set<ConstraintViolation<Usuario>> violations = validator.validate(usuario, OnCreate.class, OnUpdate.class);

        assertEquals(1, violations.size());
        assertEquals("Email não pode ser nulo ou vazio", violations.iterator().next().getMessage());
    }

    @Test
    void testEmailNaoPodeSerVazio() {
        Usuario usuario = new Usuario("", "Senha@123", TipoUsuario.LEITOR);

        Set<ConstraintViolation<Usuario>> violations = validator.validate(usuario, OnCreate.class, OnUpdate.class);

        assertEquals(1, violations.size());
        assertEquals("Email não pode ser nulo ou vazio", violations.iterator().next().getMessage());
    }

    @Property
    @Label("Testando e-mails válidos")
    void testEmailsValidos(@ForAll @Email String email) {
        Usuario usuario = new Usuario(email, "Senha@123", TipoUsuario.LEITOR);

        Set<ConstraintViolation<Usuario>> violations = validator.validate(usuario, OnCreate.class, OnUpdate.class);
        assertTrue(violations.isEmpty(), "E-mail deveria ser válido");
    }

    @ParameterizedTest
    @CsvSource({
            "usuario.exemplo.com", // Sem @
            "usuario@", // Sem domínio
            "@exemplo.com", // Sem nome de usuário
            "usuario@.com", // Ponto no início do domínio
            "usuario@exemplo..com", // Ponto duplo no domínio
            "usu ario@exemplo.com", // Espaço no nome do usuário
            "usuario@exemplo-.com", // Hífen no final do domínio
            "usuario@-exemplo.com", // Hífen no início do domínio
            "usuario@exemplo@com" // Múltiplos @
    })
    void testEmailsInvalidos(String emailInvalido) {
        Usuario usuario = new Usuario(emailInvalido, "Senha@123", TipoUsuario.LEITOR);

        Set<ConstraintViolation<Usuario>> violations = validator.validate(usuario, OnCreate.class, OnUpdate.class);

        assertEquals(1, violations.size());
        assertEquals("E-mail deve ser válido", violations.iterator().next().getMessage());
    }

    @Test
    void testSenhaNaoPodeSerNula() {
        Usuario usuario = new Usuario("teste@gmail.com", null, TipoUsuario.LEITOR);

        Set<ConstraintViolation<Usuario>> violations = validator.validate(usuario, OnCreate.class);

        assertEquals(1, violations.size());
        assertEquals("Senha não pode ser nula ou vazia", violations.iterator().next().getMessage());
    }

    @Test
    void testSenhaNaoPodeSerVazia() {
        Usuario usuario = new Usuario("teste@gmail.com", "        ", TipoUsuario.LEITOR);

        Set<ConstraintViolation<Usuario>> violations = validator.validate(usuario, OnCreate.class);
        assertEquals(1, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals(
                "A senha deve conter pelo menos um número, uma letra maiúscula, uma letra minúscula, e um caractere especial. Também não pode conter espaços!")));
    }

    @Property
    void testSenhaMenosDeOitoCaracteres(@ForAll @AlphaChars String senha) {
        Assume.that(senha.length() < 4);

        Usuario usuario = new Usuario("teste@gmail.com", senha + "Aa9@", TipoUsuario.LEITOR);
        Set<ConstraintViolation<Usuario>> violations = validator.validate(usuario, OnCreate.class);

        assertEquals(1, violations.size());
        assertEquals("A senha deve ter entre 8 e 64 caracteres", violations.iterator().next().getMessage());
    }

    @Property
    void testSenhaSemNumero(@ForAll @AlphaChars String senha) {
        Assume.that(senha.length() >= 5 && senha.length() <= 61); // Senha com tamanho válido

        Usuario usuario = new Usuario("teste@gmail.com", senha + "@Aa", TipoUsuario.LEITOR);
        Set<ConstraintViolation<Usuario>> violations = validator.validate(usuario, OnCreate.class);

        assertEquals(1, violations.size());
        assertEquals(
                "A senha deve conter pelo menos um número, uma letra maiúscula, uma letra minúscula, e um caractere especial. Também não pode conter espaços!",
                violations.iterator().next().getMessage());
    }

    @Property
    void testSenhaSemMaiuscula(@ForAll @AlphaChars String senha) {
        Assume.that(senha.length() >= 6 && senha.length() <= 62); // Senha com tamanho válido
        senha = senha.toLowerCase();

        Usuario usuario = new Usuario("teste@gmail.com", senha + "1@", TipoUsuario.LEITOR); // Adiciona número e
                                                                                            // caractere especial
        Set<ConstraintViolation<Usuario>> violations = validator.validate(usuario, OnCreate.class);

        assertEquals(1, violations.size());
        assertEquals(
                "A senha deve conter pelo menos um número, uma letra maiúscula, uma letra minúscula, e um caractere especial. Também não pode conter espaços!",
                violations.iterator().next().getMessage());
    }

    @Property
    void testSenhaSemMinuscula(@ForAll @AlphaChars String senha) {
        Assume.that(senha.length() >= 6 && senha.length() <= 62); // Senha com tamanho válido
        senha = senha.toUpperCase();

        Usuario usuario = new Usuario("teste@gmail.com", senha + "1@", TipoUsuario.LEITOR); // Adiciona número e
                                                                                            // caractere especial
        Set<ConstraintViolation<Usuario>> violations = validator.validate(usuario, OnCreate.class);

        assertEquals(1, violations.size());
        assertEquals(
                "A senha deve conter pelo menos um número, uma letra maiúscula, uma letra minúscula, e um caractere especial. Também não pode conter espaços!",
                violations.iterator().next().getMessage());
    }

    @Provide
    Arbitrary<String> alphanumericStrings() {
        return Arbitraries.strings().withChars('a', 'z').withChars('A', 'Z').withChars('0', '9');
    }

    @Property
    void testSenhaSemCaractereEspecial(@ForAll("alphanumericStrings") String senha) {
        Assume.that(senha.length() >= 5 && senha.length() <= 61); // Senha com tamanho válido

        Usuario usuario = new Usuario("teste@gmail.com", senha + "aA1", TipoUsuario.LEITOR); // Adiciona letra
                                                                                             // minúscula, letra
                                                                                             // maiúscula
                                                                                             // e número
        Set<ConstraintViolation<Usuario>> violations = validator.validate(usuario, OnCreate.class);

        assertEquals(1, violations.size());
        assertEquals(
                "A senha deve conter pelo menos um número, uma letra maiúscula, uma letra minúscula, e um caractere especial. Também não pode conter espaços!",
                violations.iterator().next().getMessage());
    }

    @Provide
    Arbitrary<String> alphanumericStringsComEspacos() {
        return Arbitraries.strings().withChars('a', 'z').withChars('A', 'Z').withChars('0', '9').withChars(" ");
    }

    @Property
    void testSenhaComEspacos(@ForAll("alphanumericStringsComEspacos") String senha) {
        Assume.that(senha.length() >= 4 && senha.length() <= 60); // Senha com tamanho válido
        // Contém espaços
        Assume.that(senha.contains(" "));

        Usuario usuario = new Usuario("teste@gmail.com", senha + "a@A1", TipoUsuario.LEITOR); // Adiciona letra
                                                                                              // minúscula, letra
                                                                                              // maiúscula
                                                                                              // e número
        Set<ConstraintViolation<Usuario>> violations = validator.validate(usuario, OnCreate.class);

        assertEquals(1, violations.size());
        assertEquals(
                "A senha deve conter pelo menos um número, uma letra maiúscula, uma letra minúscula, e um caractere especial. Também não pode conter espaços!",
                violations.iterator().next().getMessage());
    }

    @Property
    void testSenhaValida(@ForAll @AlphaChars String letras,
            @ForAll @NumericChars String numeros,
            @ForAll @Chars({ '@', '#', '$', '%', '^', '&', '+' }) String especiais) {

        String senha = letras + numeros + especiais;

        Assume.that(senha.length() >= 4 && senha.length() <= 60);

        senha = "Aa" + senha + "9@";

        Usuario usuario = new Usuario("test@gmail.com", senha, TipoUsuario.LEITOR);
        Set<ConstraintViolation<Usuario>> violations = validator.validate(usuario, OnCreate.class);

        assertTrue(violations.isEmpty(), "A senha válida não deveria gerar violações.");
    }

    @Test
    void testTipoUsuarioNaoPodeSerNulo() {
        Usuario usuario = new Usuario("teste@gmail.com", "Senha@123", null); // Tipo de usuário nulo

        Set<ConstraintViolation<Usuario>> violations = validator.validate(usuario, OnSave.class);

        assertEquals(1, violations.size());
        assertEquals(
                "Tipo de usuário não pode ser nulo",
                violations.iterator().next().getMessage());
    }

    @Test
    void testUsuarioValido() {
        Usuario usuario = new Usuario("teste@gmail.com", "Senha@123", TipoUsuario.LEITOR);

        Set<ConstraintViolation<Usuario>> violations = validator.validate(usuario, OnCreate.class, OnUpdate.class,
                OnSave.class);

        assertEquals(0, violations.size());
        assertEquals("teste@gmail.com", usuario.getEmail());
        assertEquals("Senha@123", usuario.getSenha());
        assertEquals(TipoUsuario.LEITOR, usuario.getTipoUsuario());

    }
}
