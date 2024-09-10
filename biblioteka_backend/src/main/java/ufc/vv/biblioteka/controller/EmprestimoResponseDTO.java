package ufc.vv.biblioteka.controller;

import ufc.vv.biblioteka.model.Emprestimo;
import ufc.vv.biblioteka.model.Leitor;
import ufc.vv.biblioteka.model.Livro;
import ufc.vv.biblioteka.model.Reserva;

import java.time.LocalDate;

import lombok.Data;

@Data
public class EmprestimoResponseDTO {
    private int id;
    private Leitor leitor;
    private Livro livro;
    private Reserva reserva;
    private LocalDate dataEmprestimo;
    private LocalDate dataLimite;
    private LocalDate dataDevolucao;
    private int quantidadeRenovacoes;
    private boolean devolvido;
    private double multa;
    private double valorBase;
    private double valorTotal;

    public EmprestimoResponseDTO(Emprestimo emprestimo, double multa,
            double valorBase, double valorTotal) {
        this.id = emprestimo.getId();
        this.leitor = emprestimo.getLeitor();
        this.livro = emprestimo.getLivro();
        this.reserva = emprestimo.getReserva();
        this.dataEmprestimo = emprestimo.getDataEmprestimo();
        this.dataLimite = emprestimo.getDataLimite();
        this.dataDevolucao = emprestimo.getDataDevolucao();;
        this.quantidadeRenovacoes = emprestimo.getQuantidadeRenovacoes();
        this.devolvido = emprestimo.isDevolvido();
        this.multa = multa;
        this.valorBase = valorBase;
        this.valorTotal = valorTotal;
    }

}
