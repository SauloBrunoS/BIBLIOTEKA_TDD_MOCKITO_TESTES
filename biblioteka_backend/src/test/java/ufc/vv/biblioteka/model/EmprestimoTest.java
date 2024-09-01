package ufc.vv.biblioteka.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

class EmprestimoTest {

    @Test
    void devolverLivro_ShouldSetDataDevolucaoAndMarkAsDevolvido() {
        Emprestimo emprestimo = new Emprestimo();
        LocalDate dataDevolucao = LocalDate.of(2024, 8, 30);
        emprestimo.devolverLivro(dataDevolucao);
        assertEquals(dataDevolucao, emprestimo.getDataDevolucao());
        assertTrue(emprestimo.isDevolvido());
    }

    @Test
    void calcularValorTotal_ShouldCalculateTotalBasedOnMultaAndValorBase() {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setDataEmprestimo(LocalDate.of(2024, 8, 1));
        emprestimo.setDataDevolucao(LocalDate.of(2024, 8, 20));
        emprestimo.setDataLimite(LocalDate.of(2024, 8, 15));
        
        double valorTotal = emprestimo.calcularValorTotal();
        assertEquals(emprestimo.getValorTotal(), valorTotal);
    }

    @Test
    void setDataLimite_ShouldSetCorrectLimitDate() {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setDataLimite(LocalDate.of(2024, 8, 1));
        assertEquals(LocalDate.of(2024, 8, 16), emprestimo.getDataLimite());
    }

    @Test
    void renovar_ShouldIncreaseRenovationCountAndUpdateLimitDate() {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.renovar();
        assertEquals(1, emprestimo.getQuantidadeRenovacoes());
        assertNotNull(emprestimo.getDataLimite());
    }
}
