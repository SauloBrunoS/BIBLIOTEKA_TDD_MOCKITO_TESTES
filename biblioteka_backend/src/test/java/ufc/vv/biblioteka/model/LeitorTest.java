package ufc.vv.biblioteka.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class LeitorTest {

    @Test
    void getQuantidadeEmprestimosNaoDevolvidos_ShouldCountCorrectly() {
        Leitor leitor = new Leitor();
        Emprestimo e1 = new Emprestimo();
        Emprestimo e2 = new Emprestimo();
        e1.setDevolvido(false);
        e2.setDevolvido(true);
        leitor.setEmprestimos(List.of(e1, e2));

        assertEquals(1, leitor.getQuantidadeEmprestimosNaoDevolvidos());
    }

    @Test
    void getQuantidadeEmprestimosRestantes_ShouldReturnCorrectRemainingCount() {
        Leitor leitor = new Leitor();
        leitor.setEmprestimos(List.of(new Emprestimo(), new Emprestimo()));
        leitor.getEmprestimos().get(0).setDevolvido(false);
        leitor.getEmprestimos().get(1).setDevolvido(true);

        assertEquals(4, leitor.getQuantidadeEmprestimosRestantes());
    }
}
