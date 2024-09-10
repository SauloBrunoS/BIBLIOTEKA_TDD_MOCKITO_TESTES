package ufc.vv.biblioteka.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ufc.vv.biblioteka.exception.LimiteExcedidoException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class GerenciadorRenovacaoTest {

    private GerenciadorRenovacao gerenciadorRenovacao;

    @BeforeEach
    void setUp() {
        gerenciadorRenovacao = new GerenciadorRenovacao();
    }

    @Test
    void testRenovar_emprestimoNulo() {
        assertThrows(IllegalArgumentException.class, () -> gerenciadorRenovacao.renovar(null));
    }

    @Test
    void testRenovar_sucesso() {
        Emprestimo emprestimo = new Emprestimo(new Leitor(), new Livro());
        gerenciadorRenovacao.renovar(emprestimo);
        assertEquals(1, emprestimo.getQuantidadeRenovacoes());
        assertNotNull(emprestimo.getDataLimite());
        assertTrue(emprestimo.getDataLimite()
                .isEqual(LocalDate.now().plusDays(Emprestimo.DATA_LIMITE_PRAZO_DEVOLUCAO_EM_DIAS)));
    }

    @Test
    void testRenovar_atingeLimiteMaximo() {
        Emprestimo emprestimo = new Emprestimo(new Leitor(), new Livro());
        gerenciadorRenovacao.renovar(emprestimo);
        gerenciadorRenovacao.renovar(emprestimo);
        gerenciadorRenovacao.renovar(emprestimo);
        assertThrows(LimiteExcedidoException.class, () -> gerenciadorRenovacao.renovar(emprestimo));
        assertEquals(3, emprestimo.getQuantidadeRenovacoes());
    }

    @Test
    void testRenovar_noLimite() {
        Emprestimo emprestimo = new Emprestimo(new Leitor(), new Livro());
        gerenciadorRenovacao.renovar(emprestimo);
        gerenciadorRenovacao.renovar(emprestimo);
        gerenciadorRenovacao.renovar(emprestimo);
        assertEquals(3, emprestimo.getQuantidadeRenovacoes());
        assertNotNull(emprestimo.getDataLimite());
        assertTrue(emprestimo.getDataLimite()
                .isEqual(LocalDate.now().plusDays(Emprestimo.DATA_LIMITE_PRAZO_DEVOLUCAO_EM_DIAS)));
    }

}
