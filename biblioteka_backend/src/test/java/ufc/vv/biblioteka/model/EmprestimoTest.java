package ufc.vv.biblioteka.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

 class EmprestimoTest {

    private Leitor leitor;
    private Livro livro;
    private Reserva reserva;
    private Validator validator;

    @BeforeEach
    void setUp() {
        leitor = new Leitor();  
        livro = new Livro();   
        reserva = new Reserva(); 

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testEmprestimoValidoSemReserva() {
        Emprestimo emprestimo = new Emprestimo(leitor, livro);

        Set<ConstraintViolation<Emprestimo>> violations = validator.validate(emprestimo);
        assertTrue(violations.isEmpty(), "Deve passar em todas as validações");

        assertEquals(leitor, emprestimo.getLeitor(), "Leitor deve ser associado corretamente");
        assertEquals(livro, emprestimo.getLivro(), "Livro deve ser associado corretamente");
        assertNotNull(emprestimo.getDataEmprestimo(), "A data de empréstimo deve ser definida");
        assertEquals(LocalDate.now(), emprestimo.getDataEmprestimo(), "A date de empréstimo deve ser a de hoje");
        assertNotNull(emprestimo.getDataLimite(), "A data limite deve ser definida");
        assertEquals(LocalDate.now().plusDays(Emprestimo.DATA_LIMITE_PRAZO_DEVOLUCAO_EM_DIAS), emprestimo.getDataLimite(), "A date limite deve ser a de hoje mais o limite de dias para devolução do livro");
        assertFalse(emprestimo.isDevolvido(), "O empréstimo deve começar como não devolvido");
        assertEquals(0, emprestimo.getQuantidadeRenovacoes(), "A quantidade de renovações deve ser zero no início");
    }

    @Test
    void testEmprestimoInvalidoSemLeitor() {
        Emprestimo emprestimo = new Emprestimo(null, livro);

        Set<ConstraintViolation<Emprestimo>> violations = validator.validate(emprestimo);
        assertEquals(1, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Leitor não pode ser nulo")));
    }

    @Test
    void testEmprestimoInvalidoSemLivro() {
        Emprestimo emprestimo = new Emprestimo(leitor, null);

        Set<ConstraintViolation<Emprestimo>> violations = validator.validate(emprestimo);
        assertEquals(1, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Livro não pode ser nulo")));
   
    }

    @Test
    void testDevolverLivro() {
        Emprestimo emprestimo = new Emprestimo(leitor, livro);
        emprestimo.devolverLivro();

        assertTrue(emprestimo.isDevolvido(), "O livro deve estar marcado como devolvido");
        assertEquals(LocalDate.now(), emprestimo.getDataDevolucao(), "A data de devolução deve ser a data atual");
    }

    @Test
    void testMarcarDataLimite() {
        Emprestimo emprestimo = new Emprestimo(leitor, livro);
        emprestimo.marcarDataLimite();

        assertEquals(LocalDate.now().plusDays(Emprestimo.DATA_LIMITE_PRAZO_DEVOLUCAO_EM_DIAS), emprestimo.getDataLimite(), "A data limite deve ser 15 dias após a data atual");
    }

    @Test
    void testConstrutorComReserva() {
        Emprestimo emprestimo = new Emprestimo(leitor, livro, reserva);

        Set<ConstraintViolation<Emprestimo>> violations = validator.validate(emprestimo);
        assertTrue(violations.isEmpty(), "Deve passar em todas as validações");

        assertEquals(reserva, emprestimo.getReserva(), "A reserva deve ser associada corretamente");
        assertEquals(leitor, emprestimo.getLeitor(), "Leitor deve ser associado corretamente");
        assertEquals(livro, emprestimo.getLivro(), "Livro deve ser associado corretamente");
        assertNotNull(emprestimo.getDataEmprestimo(), "A data de empréstimo deve ser definida");
        assertEquals(LocalDate.now(), emprestimo.getDataEmprestimo(), "A date de empréstimo deve ser a de hoje");
        assertNotNull(emprestimo.getDataLimite(), "A data limite deve ser definida");
        assertEquals(LocalDate.now().plusDays(Emprestimo.DATA_LIMITE_PRAZO_DEVOLUCAO_EM_DIAS), emprestimo.getDataLimite(), "A date limite deve ser a de hoje mais o limite de dias para devolução do livro");
        assertFalse(emprestimo.isDevolvido(), "O empréstimo deve começar como não devolvido");
        assertEquals(0, emprestimo.getQuantidadeRenovacoes(), "A quantidade de renovações deve ser zero no início");
    }

}
