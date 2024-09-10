package ufc.vv.biblioteka.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.voodoodyne.jackson.jsog.JSOGGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@JsonIdentityInfo(generator = JSOGGenerator.class)
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Nome completo não pode ser nulo ou vazio")
    @Column(unique = true)
    @Size(min = 3, max = 100, message = "O nome completo deve ter entre 3 e 100 caracteres.")
    @Pattern(regexp = "^[\\p{L}'\\-\\sIVXLCDM]+$", message = "O nome completo deve conter apenas letras, espaços, apóstrofos, hífens e números romanos.")
    private String nomeCompleto;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @NotNull(message = "Data de nascimento não pode ser nulo")
    @Past(message = "A data de nascimento deve estar no passado.")
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Nacionalidade não pode ser nula")
    private Nacionalidade nacionalidade;

    @ManyToMany(mappedBy = "autores")
    private List<Livro> livros;

    @PrePersist
    @PreUpdate
    private void trimNome() {
        if (nomeCompleto != null) {
            nomeCompleto = nomeCompleto.trim();
        }
    }

    public Autor(String nomeCompleto, LocalDate dataNascimento, Nacionalidade nacionalidade) {
        this.nomeCompleto = nomeCompleto;
        this.dataNascimento = dataNascimento;
        this.nacionalidade = nacionalidade;
    }

    public void adicionarLivro(Livro livro) {
        if (this.livros != null)
            livros.add(livro);
        else {
            livros = new ArrayList<>();
            livros.add(livro);
        }
    }
}
