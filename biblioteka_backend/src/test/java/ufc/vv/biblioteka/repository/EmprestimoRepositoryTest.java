package ufc.vv.biblioteka.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;

import jakarta.transaction.Transactional;
import ufc.vv.biblioteka.model.Emprestimo;
import ufc.vv.biblioteka.model.Leitor;
import ufc.vv.biblioteka.model.Livro;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import java.time.LocalDate;
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
public class EmprestimoRepositoryTest {

    @Autowired
    private EmprestimoRepository emprestimoRepository;

    @Test
    public void deveEncontrarEmprestimoPorLeitorIdEData() {
        // Dado
        Emprestimo emprestimo = new Emprestimo();
        Leitor leitor = new Leitor();
        leitor.setId(1); // Assume que o leitor com ID 1 existe
        emprestimo.setLeitor(leitor);
        emprestimo.setDataEmprestimo(LocalDate.now());
        emprestimoRepository.save(emprestimo);

        // Quando
        Page<Emprestimo> emprestimos = emprestimoRepository.findByLeitorIdAndSearch(1, "now", null, null, PageRequest.of(0, 10));

        // Então
        assertFalse(emprestimos.isEmpty());
    }

    @Test
    public void deveEncontrarEmprestimoPorLivroId() {
        // Dado
        Emprestimo emprestimo = new Emprestimo();
        Livro livro = new Livro();
        livro.setId(1); // Assume que o livro com ID 1 existe
        emprestimo.setLivro(livro);
        emprestimoRepository.save(emprestimo);

        // Quando
        Page<Emprestimo> emprestimos = emprestimoRepository.findByLivroIdAndSearch(1, "", null, null, PageRequest.of(0, 10));

        // Então
        assertFalse(emprestimos.isEmpty());
    }

    @Test
    public void deveEncontrarEmprestimoNaoDevolvidoPorLivroELeitor() {
        // Dado
        Emprestimo emprestimo = new Emprestimo();
        Livro livro = new Livro();
        Leitor leitor = new Leitor();
        livro.setId(1);
        leitor.setId(1);
        emprestimo.setLivro(livro);
        emprestimo.setLeitor(leitor);
        emprestimo.setDevolvido(false);
        emprestimoRepository.save(emprestimo);

        // Quando
        Optional<Emprestimo> emprestimoEncontrado = emprestimoRepository.findByLivroAndLeitorAndDevolvidoFalse(livro, leitor);

        // Então
        assertTrue(emprestimoEncontrado.isPresent());
    }
}
