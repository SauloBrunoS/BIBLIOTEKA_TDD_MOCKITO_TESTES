package ufc.vv.biblioteka.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import ufc.vv.biblioteka.exception.LivroIndisponivelException;

import java.util.ArrayList;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import java.time.LocalDate;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

@Entity
@Data
@NoArgsConstructor
@JsonIdentityInfo(generator = JSOGGenerator.class)
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Título não pode ser nulo ou vazio")
    @Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ0-9'\\-\\s]+$", message = "O título deve conter apenas letras, números, espaços, apóstrofos e hífens.")
    @Size(min = 3, max = 100, message = "O título deve ter entre 3 e 100 caracteres.")
    private String titulo;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @JoinTable(name = "livro_autor", joinColumns = @JoinColumn(name = "livro_id"), inverseJoinColumns = @JoinColumn(name = "autor_id"))
    @NotNull(message = "A lista de autores não pode ser nula")
    @Size(min = 1, message = "O livro deve ter pelo menos um autor")
    private List<Autor> autores;
    
    @Column(unique = true)
    @NotNull(message = "ISBN não pode ser nulo")
    @Pattern(regexp = "^(\\d{9}[\\dX]|\\d{13})$", message = "ISBN deve ter exatamente 10 dígitos, podendo opcionalmente ter um X no final, ou 13 dígitos")
    private String isbn;

    @NotNull(message = "A data de publicação não pode ser nula")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @Past(message = "Data de publicação deve ser passada")
    private LocalDate dataPublicacao;

    @Min(value = 0, message = "O número de cópias totais deve ser maior ou igual a zero")
    private int numeroCopiasDisponiveis;

    @Min(value = 0, message = "O número de cópias disponíveis deve ser maior ou igual a zero")
    private int numeroCopiasTotais;

    @Min(value = 1, message = "A quantidade de páginas deve ser maior que zero")
    private int qtdPaginas;

    @OneToMany(mappedBy = "livro", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    private List<Emprestimo> emprestimos;

    @OneToMany(mappedBy = "livro", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    private List<Reserva> reservas;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    @JoinTable(name = "livro_colecao", joinColumns = @JoinColumn(name = "livro_id"), inverseJoinColumns = @JoinColumn(name = "colecao_id"))
    @NotNull(message = "A lista de coleções não pode ser nula")
    @Size(min = 1, message = "O livro deve pertencer a pelo menos uma coleção")
    private List<Colecao> colecoes;

    public void emprestarLivro() {
        if (numeroCopiasDisponiveis > 0) {
            numeroCopiasDisponiveis--;
        } else {
            throw new LivroIndisponivelException("O livro não está disponível para empréstimo.");
        }
    }

    public void devolverLivro() {
        if(numeroCopiasDisponiveis == numeroCopiasTotais) throw new IllegalStateException("Nenhuma cópia do livro está em empréstimo atualmente");
        numeroCopiasDisponiveis++;
    }

    public Livro(String titulo, List<Autor> autores, String isbn, LocalDate dataPublicacao, int numeroCopias,
            int qtdPaginas, List<Colecao> colecoes) {
        this.titulo = titulo;
        this.autores = autores;
        this.isbn = isbn;
        this.dataPublicacao = dataPublicacao;
        this.numeroCopiasDisponiveis = numeroCopias;
        this.numeroCopiasTotais = numeroCopias;
        this.qtdPaginas = qtdPaginas;
        this.colecoes = colecoes;
        this.reservas = new ArrayList<>();
        this.emprestimos = new ArrayList<>();
    }

    public void adicionarReserva(Reserva reserva) {
        if (this.reservas != null)
            reservas.add(reserva);
        else {
            reservas = new ArrayList<>();
            reservas.add(reserva);
        }
    }

    public void adicionarEmprestimo(Emprestimo emprestimo) {
        if (this.emprestimos != null)
            emprestimos.add(emprestimo);
        else {
            emprestimos = new ArrayList<>();
            emprestimos.add(emprestimo);
        }
    }
}
