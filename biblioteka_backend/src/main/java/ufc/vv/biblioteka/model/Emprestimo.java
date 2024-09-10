package ufc.vv.biblioteka.model;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ufc.vv.biblioteka.exception.LimiteExcedidoException;

@Entity
@Data
@NoArgsConstructor
@JsonIdentityInfo(generator = JSOGGenerator.class)
public class Emprestimo {

    public static final int DATA_LIMITE_PRAZO_DEVOLUCAO_EM_DIAS = 15;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "leitor_id")
    @NotNull(message = "Leitor não pode ser nulo")
    private Leitor leitor;

    @ManyToOne
    @JoinColumn(name = "livro_id")
    @NotNull(message = "Livro não pode ser nulo")
    private Livro livro;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @Setter(AccessLevel.NONE)
    private LocalDate dataEmprestimo;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate dataLimite;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @Setter(AccessLevel.NONE)
    private LocalDate dataDevolucao;

    @Setter(AccessLevel.NONE)
    private boolean devolvido;

    @Setter(AccessLevel.NONE)
    private int quantidadeRenovacoes;

    @OneToOne(mappedBy = "emprestimo", cascade = CascadeType.PERSIST)
    private Reserva reserva;

    public void devolverLivro() {
        this.dataDevolucao = LocalDate.now();
        this.devolvido = true;
    }

    public void renovarLivro() {
        if (quantidadeRenovacoes == GerenciadorRenovacao.LIMITE_RENOVACOES_SEGUIDAS_DE_UM_MESMO_LIVRO)
            throw new LimiteExcedidoException("Limite de renovações atingido.");
        this.quantidadeRenovacoes++;
    }

    public void marcarDataLimite() {
        this.dataLimite = LocalDate.now().plusDays(DATA_LIMITE_PRAZO_DEVOLUCAO_EM_DIAS);
    }

    public Emprestimo(Leitor leitor, Livro livro) {
        this.leitor = leitor;
        this.livro = livro;
        this.dataEmprestimo = LocalDate.now();
        this.devolvido = false;
        this.quantidadeRenovacoes = 0;
        marcarDataLimite();
    }

    public Emprestimo(Leitor leitor, Livro livro, Reserva reserva) {
        this.leitor = leitor;
        this.livro = livro;
        this.dataEmprestimo = LocalDate.now();
        this.devolvido = false;
        this.quantidadeRenovacoes = 0;
        this.reserva = reserva;
        marcarDataLimite();
    }

}
