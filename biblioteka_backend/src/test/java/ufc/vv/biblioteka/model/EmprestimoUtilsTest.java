package ufc.vv.biblioteka.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class EmprestimoUtilsTest {

    @Test
    public void calcularMulta_shouldReturnZeroWhenDataDevolucaoIsBeforeOrEqualToDataLimite() {
        // Arrange
        LocalDate dataLimite = LocalDate.now();
        LocalDate dataDevolucao = dataLimite;

        // Act
        double multa = EmprestimoUtils.calcularMulta(dataDevolucao, dataLimite);

        // Assert
        assertThat(multa).isEqualTo(0.0);
    }

    @Test
    public void calcularMulta_shouldReturnZeroWhenDataDevolucaoIsNullAndTodayIsBeforeOrEqualToDataLimite() {
        // Arrange
        LocalDate dataLimite = LocalDate.now().plusDays(1);
        LocalDate dataDevolucao = null;

        // Act
        double multa = EmprestimoUtils.calcularMulta(dataDevolucao, dataLimite);

        // Assert
        assertThat(multa).isEqualTo(0.0);
    }

    @Test
    public void calcularMulta_shouldCalculateMultaWhenDataDevolucaoIsAfterDataLimite() {
        // Arrange
        LocalDate dataLimite = LocalDate.now();
        LocalDate dataDevolucao = dataLimite.plusDays(3);

        // Act
        double multa = EmprestimoUtils.calcularMulta(dataDevolucao, dataLimite);

        // Assert
        assertThat(multa).isEqualTo(6.0); // 3 days late * 2.0 per day
    }

    @Test
    public void calcularMulta_shouldCalculateMultaWhenDataDevolucaoIsNullAndTodayIsAfterDataLimite() {
        // Arrange
        LocalDate dataLimite = LocalDate.now().minusDays(2);
        LocalDate dataDevolucao = null;

        // Act
        double multa = EmprestimoUtils.calcularMulta(dataDevolucao, dataLimite);

        // Assert
        assertThat(multa).isEqualTo(4.0); // 2 days late * 2.0 per day
    }

    @Test
    public void calcularMulta_shouldThrowExceptionWhenDataLimiteIsNull() {
        // Act & Assert
        assertThatThrownBy(() -> EmprestimoUtils.calcularMulta(LocalDate.now(), null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Data limite não podem ser nula.");
    }

    @Test
    public void calcularValorBase_shouldCalculateCorrectlyWithDataDevolucao() {
        // Arrange
        LocalDate dataEmprestimo = LocalDate.now().minusDays(5);
        LocalDate dataDevolucao = LocalDate.now();
        LocalDate dataLimite = LocalDate.now().plusDays(5);

        // Act
        double valorBase = EmprestimoUtils.calcularValorBase(dataEmprestimo, dataDevolucao, dataLimite);

        // Assert
        assertThat(valorBase).isEqualTo(5.0); // 5 days * 1.0 per day
    }

    @Test
    public void calcularValorBase_shouldCalculateCorrectlyWhenDataDevolucaoIsNullAndBeforeOrEqualToDataLimite() {
        // Arrange
        LocalDate dataEmprestimo = LocalDate.now().minusDays(5);
        LocalDate dataDevolucao = null;
        LocalDate dataLimite = LocalDate.now().plusDays(5);

        // Act
        double valorBase = EmprestimoUtils.calcularValorBase(dataEmprestimo, dataDevolucao, dataLimite);

        // Assert
        assertThat(valorBase).isEqualTo(5.0); // 5 days * 1.0 per day
    }

    @Test
    public void calcularValorBase_shouldThrowExceptionWhenDataEmprestimoOrDataLimiteAreNull() {
        // Act & Assert
        assertThatThrownBy(() -> EmprestimoUtils.calcularValorBase(null, LocalDate.now(), LocalDate.now()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Data de empréstimo e data de limite não podem ser nulas.");

        assertThatThrownBy(() -> EmprestimoUtils.calcularValorBase(LocalDate.now(), LocalDate.now(), null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Data de empréstimo e data de limite não podem ser nulas.");
    }

    @Test
    public void calcularValorBase_shouldThrowExceptionWhenDataEmprestimoIsAfterData() {
        // Arrange
        LocalDate dataEmprestimo = LocalDate.now().plusDays(5);
        LocalDate dataDevolucao = LocalDate.now();

        // Act & Assert
        assertThatThrownBy(() -> EmprestimoUtils.calcularValorBase(dataEmprestimo, dataDevolucao, dataDevolucao))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("A data de empréstimo não pode ser posterior à data de devolução ou data atual.");
    }

    @Test
    public void calcularValorTotal_shouldCalculateSumOfValorBaseAndMulta() {
        // Act
        double valorTotal = EmprestimoUtils.calcularValorTotal(10.0, 5.0);

        // Assert
        assertThat(valorTotal).isEqualTo(15.0);
    }

    @Test
    public void calcularValorTotal_shouldThrowExceptionWhenValoresAreNegative() {
        // Act & Assert
        assertThatThrownBy(() -> EmprestimoUtils.calcularValorTotal(-10.0, 5.0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Valores não podem ser negativos.");

        assertThatThrownBy(() -> EmprestimoUtils.calcularValorTotal(10.0, -5.0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Valores não podem ser negativos.");
    }
}
