package ufc.vv.biblioteka.model;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
@JsonIdentityInfo(generator = JSOGGenerator.class)
@EqualsAndHashCode(of = "id")
public class Emprestimo {

    private static final int DATA_LIMITE_PRAZO_DEVOLUCAO_EM_DIAS = 15;
    private static final int LIMITE_RENOVACOES_SEGUIDAS_DE_UM_MESMO_LIVRO = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "leitor_id", nullable = false)
    private Leitor leitor;

    @ManyToOne
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @Column(nullable = false)
    private LocalDate dataEmprestimo;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @Column(nullable = false)
    private LocalDate dataLimite;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataDevolucao;

    private boolean devolvido;

    private double valorBase;

    private int quantidadeRenovacoes;

    private double multa;

    private double valorTotal;

    @OneToOne(mappedBy = "emprestimo", cascade = CascadeType.PERSIST)
    private Reserva reserva;

    public void devolverLivro(LocalDate dataDevolucao) {
        this.dataDevolucao = dataDevolucao;
        this.devolvido = true;
    }

    public double calcularValorTotal() {
        this.valorBase = EmprestimoUtils.calcularValorBase(dataEmprestimo, dataDevolucao, dataLimite);
        this.multa = EmprestimoUtils.calcularMulta(dataDevolucao, dataLimite);
        this.valorTotal = EmprestimoUtils.calcularValorTotal(valorBase, multa);
        return valorTotal;
    }

    public void setDataLimite(LocalDate dataAtual) {
        this.dataLimite = dataAtual.plusDays(DATA_LIMITE_PRAZO_DEVOLUCAO_EM_DIAS);
    }

    public int getQuantidadeRenovacoesRestantes() {
        return LIMITE_RENOVACOES_SEGUIDAS_DE_UM_MESMO_LIVRO - quantidadeRenovacoes;
    }

    public void renovar() {
        quantidadeRenovacoes++;
        setDataLimite(LocalDate.now());
    }
}
