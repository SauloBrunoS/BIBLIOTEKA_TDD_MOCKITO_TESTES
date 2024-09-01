package ufc.vv.biblioteka.model;

import java.time.LocalDate;
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
import lombok.EqualsAndHashCode;

@Entity
@Data
@JsonIdentityInfo(generator = JSOGGenerator.class)
@EqualsAndHashCode(of = "id")
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    @NotBlank
    @Column(unique = true)
    @Size(min = 3, max = 100, message = "O nome completo deve ter entre 3 e 100 caracteres.")
    @Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ'\\-\\s]+$", message = "O nome completo deve conter apenas letras, espaços, apóstrofos e hífens.")
    private String nomeCompleto;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    @NotNull
    @Past(message = "A data de nascimento deve ser no passado.")
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Nacionalidade nacionalidade;

    @ManyToMany(mappedBy = "autores")
    private List<Livro> livros;

    @PrePersist
    @PreUpdate
    private void trimNome() {
        if (nomeCompleto != null) {
            nomeCompleto = nomeCompleto.toLowerCase().trim();
        }
    }
}
