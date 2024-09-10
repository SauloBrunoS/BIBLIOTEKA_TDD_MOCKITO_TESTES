package ufc.vv.biblioteka.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import net.jqwik.api.lifecycle.BeforeProperty;

class ReservaTest {

    private Validator validator;
    private Livro livro;
    private Leitor leitor;

    @BeforeEach
    @BeforeProperty
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        livro = new Livro();
        leitor = new Leitor();
    }

    @Test
    void testLivroNulo() {
        Reserva reserva = new Reserva(null, leitor);
        reserva.marcarComoEmEspera(); // não faz diferença o status da reserva

        Set<ConstraintViolation<Reserva>> violations = validator.validate(reserva);
        assertEquals(1, violations.size());
        assertEquals("Livro não pode ser nulo", violations.iterator().next().getMessage());
    }

    @Test
    void testLeitorNulo() {
        Reserva reserva = new Reserva(livro, null);
        reserva.marcarComoEmEspera(); // não faz diferença o status da reserva

        Set<ConstraintViolation<Reserva>> violations = validator.validate(reserva);
        assertEquals(1, violations.size());
        assertEquals("Leitor não pode ser nulo", violations.iterator().next().getMessage());
    }

    @Test
    void testStatusNulo() {
        Reserva reserva = new Reserva(new Livro(), new Leitor());

        Set<ConstraintViolation<Reserva>> violations = validator.validate(reserva);
        assertEquals(1, violations.size());
        assertEquals("Status da Reserva não pode ser nulo", violations.iterator().next().getMessage());
    }

    @Test
    void testMarcarComoEmAndamento() {
        Reserva reserva = new Reserva(new Livro(), new Leitor());
        reserva.marcarComoEmAndamento();

        assertEquals(StatusReserva.EM_ANDAMENTO, reserva.getStatus());
        assertEquals(LocalDate.now().plusDays(Reserva.PRAZO_RESERVA_ATIVA_EM_DIAS), reserva.getDataLimite());
    }

    @Test
    void testMarcarComoCancelada() {
        Reserva reserva = new Reserva(new Livro(), new Leitor());
        reserva.marcarComoCancelada();

        assertEquals(StatusReserva.CANCELADA, reserva.getStatus());
    }

    @Test
    void testMarcarComoEmEspera() {
        Reserva reserva = new Reserva(new Livro(), new Leitor());
        reserva.marcarComoEmEspera();

        assertEquals(StatusReserva.EM_ESPERA, reserva.getStatus());
    }

    @Test
    void testMarcarComoExpirada() {
        Reserva reserva = new Reserva(new Livro(), new Leitor());
        reserva.marcarComoExpirada();

        assertEquals(StatusReserva.EXPIRADA, reserva.getStatus());
    }

    @Test
    void testMarcarComoAtendida() {
        Reserva reserva = new Reserva(new Livro(), new Leitor());
        reserva.marcarComoAtendida();

        assertEquals(StatusReserva.ATENDIDA, reserva.getStatus());
    }

    @Test
    void testReservaValida() {
        Reserva reserva = new Reserva(new Livro(), new Leitor());
        reserva.marcarComoEmAndamento(); // não faz diferença o status da reserva

        Set<ConstraintViolation<Reserva>> violations = validator.validate(reserva);
        assertEquals(0, violations.size());
        assertNotNull(reserva.getDataCadastro(), "Data de cadastro deve ser preenchida automaticamente.");
        long diffInSeconds = ChronoUnit.SECONDS.between(reserva.getDataCadastro(), LocalDateTime.now());
        assertTrue(diffInSeconds <= 1, "A data de cadastro deve ser a data atual.");
        assertEquals(leitor, reserva.getLeitor(), "Leitor deve ser associado corretamente");
        assertEquals(livro, reserva.getLivro(), "Livro deve ser associado corretamente");

    }

}
