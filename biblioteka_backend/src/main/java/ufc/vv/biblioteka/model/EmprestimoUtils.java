package ufc.vv.biblioteka.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class EmprestimoUtils {
    public static final double TAXA_MULTA_POR_DIA = 2.0;
    public static final double TAXA_EMPRESTIMO_POR_DIA = 1.0;

    private EmprestimoUtils() {

    }

    public static double calcularMulta(LocalDate dataDevolucao, LocalDate dataLimite) {
        if (dataLimite == null) {
            throw new IllegalArgumentException("Data limite não pode ser nula.");
        }
        if (dataDevolucao != null && dataDevolucao.isAfter(dataLimite)) {
            long diasAtraso = ChronoUnit.DAYS.between(dataLimite, dataDevolucao);
            return diasAtraso * TAXA_MULTA_POR_DIA;
        } else if (dataDevolucao == null && LocalDate.now().isAfter(dataLimite)) {
            long diasAtraso = ChronoUnit.DAYS.between(dataLimite, LocalDate.now());
            return diasAtraso * TAXA_MULTA_POR_DIA;
        } else
            return 0.0;
    }

    public static double calcularValorBase(LocalDate dataEmprestimo, LocalDate dataDevolucao, LocalDate dataLimite) {
        if (dataEmprestimo == null || dataLimite == null) {
            throw new IllegalArgumentException("Data de empréstimo e data limite não podem ser nulas.");
        }
        LocalDate data;
        if (dataDevolucao != null) {
            if (dataDevolucao.isAfter(dataLimite)) {
                data = dataLimite;
            } else
                data = dataDevolucao;
        } else {
            LocalDate dataAtual = LocalDate.now();
            if (dataAtual.isAfter(dataLimite)) {
                data = dataLimite;
            } else {
                data = dataAtual;
            }
        }
        if (dataEmprestimo.isAfter(data)) {
            throw new IllegalArgumentException(
                    "A data de empréstimo não pode ser posterior à data de devolução, data limite ou data atual.");
        }
        long diasEmprestimo = ChronoUnit.DAYS.between(dataEmprestimo, data);
        return diasEmprestimo * TAXA_EMPRESTIMO_POR_DIA;
    }

    public static double calcularValorTotal(double valorBase, double multa) {
        if (valorBase < 0 || multa < 0) {
            throw new IllegalArgumentException("Valores não podem ser negativos.");
        }
        return valorBase + multa;
    }
}