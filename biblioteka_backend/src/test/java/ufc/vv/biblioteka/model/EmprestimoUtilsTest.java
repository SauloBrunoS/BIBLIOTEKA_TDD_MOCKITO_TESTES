package ufc.vv.biblioteka.model;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class EmprestimoUtilsTest {

    @Test
    void testCalcularMulta_DataLimiteNula() {
        LocalDate dataDevolucao = LocalDate.now();
        assertThrows(IllegalArgumentException.class, () -> EmprestimoUtils.calcularMulta(dataDevolucao, null));
    }

    @Test
    void testCalcularMulta_DevolucaoAntesDoPrazo() {
        LocalDate dataDevolucao = LocalDate.of(2024, 9, 5);
        LocalDate dataLimite = LocalDate.of(2024, 9, 10);
        double multa = EmprestimoUtils.calcularMulta(dataDevolucao, dataLimite);
        assertEquals(0.0, multa);
    }

    @Test
    void testCalcularMulta_DevolucaoAposADataLimite() {
        LocalDate dataDevolucao = LocalDate.of(2024, 9, 15);
        LocalDate dataLimite = LocalDate.of(2024, 9, 10);
        double multa = EmprestimoUtils.calcularMulta(dataDevolucao, dataLimite);
        assertEquals(5.0*EmprestimoUtils.TAXA_MULTA_POR_DIA, multa); 
    }

    @Test
    void testCalcularMulta_DevolucaoNaDataLimite() {
        LocalDate dataDevolucao = LocalDate.of(2024, 9, 15);
        LocalDate dataLimite = LocalDate.of(2024, 9, 15);
        double multa = EmprestimoUtils.calcularMulta(dataDevolucao, dataLimite);
        assertEquals(0.0, multa); 
    }

    @Test
    void testCalcularMulta_DevolucaoNulaAtraso() {
        LocalDate dataDevolucao = null;
        LocalDate dataLimite = LocalDate.now().minusDays(5);
        double multa = EmprestimoUtils.calcularMulta(dataDevolucao, dataLimite);
        assertEquals(5.0*EmprestimoUtils.TAXA_MULTA_POR_DIA, multa); 
    }

    @Test
    void testCalcularMulta_DevolucaoNulaSemAtraso() {
        LocalDate dataDevolucao = null;
        LocalDate dataLimite = LocalDate.now().plusDays(5);
        double multa = EmprestimoUtils.calcularMulta(dataDevolucao, dataLimite);
        assertEquals(0.0, multa);
    }

    @Test
    void testCalcularMulta_DevolucaoNulaDataLimiteHoje() {
        LocalDate dataDevolucao = null;
        LocalDate dataLimite = LocalDate.now();
        double multa = EmprestimoUtils.calcularMulta(dataDevolucao, dataLimite);
        assertEquals(0.0, multa);
    }


    @Test
    void testCalcularValorBase_DataEmprestimoNula() {
        LocalDate dataEmprestimo = null;
        LocalDate dataDevolucao = LocalDate.of(2024, 9, 5);
        LocalDate dataLimite = LocalDate.of(2024, 9, 10);
        assertThrows(IllegalArgumentException.class, () -> EmprestimoUtils.calcularValorBase(dataEmprestimo, dataDevolucao, dataLimite));
    }

    @Test
    void testCalcularValorBase_DataLimiteNula() {
        LocalDate dataEmprestimo = LocalDate.of(2024, 9, 1);
        LocalDate dataDevolucao = LocalDate.of(2024, 9, 5);
        LocalDate dataLimite = null;
        assertThrows(IllegalArgumentException.class, () -> EmprestimoUtils.calcularValorBase(dataEmprestimo, dataDevolucao, dataLimite));
    }

    @Test
    void testCalcularValorBase_EmprestimoComDevolucaoNulaAnteriorADataLimite() {
        LocalDate dataEmprestimo = LocalDate.now().minusDays(7);
        LocalDate dataDevolucao = null;
        LocalDate dataLimite = LocalDate.now().plusDays(8);
        double valorBase = EmprestimoUtils.calcularValorBase(dataEmprestimo, dataDevolucao, dataLimite);
        assertEquals(7.0, valorBase); 
    }

    @Test
    void testCalcularValorBase_EmprestimoComDevolucaoNulaPosteriorADataLimite() {
        LocalDate dataEmprestimo = LocalDate.now().minusDays(17);
        LocalDate dataDevolucao = null;
        LocalDate dataLimite = LocalDate.now().minusDays(2);
        double valorBase = EmprestimoUtils.calcularValorBase(dataEmprestimo, dataDevolucao, dataLimite);
        assertEquals(15.0, valorBase); 
    }

    @Test
    void testCalcularValorBase_EmprestimoDentroDoPrazo() {
        LocalDate dataEmprestimo = LocalDate.of(2024, 9, 1);
        LocalDate dataDevolucao = LocalDate.of(2024, 9, 5);
        LocalDate dataLimite = LocalDate.of(2024, 9, 16);
        double valorBase = EmprestimoUtils.calcularValorBase(dataEmprestimo, dataDevolucao, dataLimite);
        assertEquals(4.0, valorBase); 
    }

    @Test
    void testCalcularValorBase_EmprestimoComDevolucaoAtrasada() {
        LocalDate dataEmprestimo = LocalDate.of(2024, 9, 1);
        LocalDate dataDevolucao = LocalDate.of(2024, 9, 22);
        LocalDate dataLimite = LocalDate.of(2024, 9, 16);
        double valorBase = EmprestimoUtils.calcularValorBase(dataEmprestimo, dataDevolucao, dataLimite);
        assertEquals(15.0, valorBase); 
    }

    @Test
    void testCalcularValorBase_DataEmprestimoPosteriorDataDevolucao() {
        LocalDate dataEmprestimo = LocalDate.of(2024, 9, 10);
        LocalDate dataDevolucao = LocalDate.of(2024, 9, 5);
        LocalDate dataLimite = LocalDate.of(2024, 9, 25);
        assertThrows(IllegalArgumentException.class, () -> EmprestimoUtils.calcularValorBase(dataEmprestimo, dataDevolucao, dataLimite));
    }

    @Test
    void testCalcularValorBase_DataEmprestimoPosteriorDataLimite() {
        LocalDate dataEmprestimo = LocalDate.of(2024, 9, 10);
        LocalDate dataDevolucao = LocalDate.of(2024, 9, 12);
        LocalDate dataLimite = LocalDate.of(2024, 9, 8);
        assertThrows(IllegalArgumentException.class, () -> EmprestimoUtils.calcularValorBase(dataEmprestimo, dataDevolucao, dataLimite));
    }

    @Test
    void testCalcularValorBase_DataEmprestimoPosteriorDataAtual() {
        LocalDate dataEmprestimo = LocalDate.now().plusDays(2);
        LocalDate dataDevolucao = null;
        LocalDate dataLimite = LocalDate.now().plusDays(17);
        assertThrows(IllegalArgumentException.class, () -> EmprestimoUtils.calcularValorBase(dataEmprestimo, dataDevolucao, dataLimite));
    }

    @Test
    void testCalcularValorTotal_ValoresValidos() {
        double valorBase = 10.0;
        double multa = 5.0;
        double valorTotal = EmprestimoUtils.calcularValorTotal(valorBase, multa);
        assertEquals(15.0, valorTotal);
    }

    @Test
    void testCalcularValorTotal_PrimeiroValorNegativo() {
        double valorBase = -10.0;
        double multa = 5.0;
        assertThrows(IllegalArgumentException.class, () -> EmprestimoUtils.calcularValorTotal(valorBase, multa));
    }

    @Test
    void testCalcularValorTotal_SegundoValorNegativo() {
        double valorBase = 10.0;
        double multa = -5.0;
        assertThrows(IllegalArgumentException.class, () -> EmprestimoUtils.calcularValorTotal(valorBase, multa));
    }
}
