package ufc.vv.biblioteka.model;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.validator.constraints.br.CPF;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@JsonIdentityInfo(generator = JSOGGenerator.class)
public class Leitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Nome completo não pode ser nulo ou vazio", groups = { OnCreate.class, OnUpdate.class })
    @Size(min = 3, max = 100, message = "O nome completo deve ter entre 3 e 100 caracteres.", groups = { OnCreate.class,
            OnUpdate.class })
    @Pattern(regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ'\\-\\s]+$", message = "O nome completo deve conter apenas letras, espaços, apóstrofos e hífens.", groups = {
            OnCreate.class, OnUpdate.class })
    private String nomeCompleto;

    @Pattern(regexp = "\\d{10,11}", message = "Telefone deve ter exatamente 10 ou 11 dígitos", groups = {
            OnCreate.class, OnUpdate.class })
    @NotNull(message = "Telefone não pode ser nulo", groups = { OnCreate.class, OnUpdate.class })
    private String telefone;

    @NotNull(message = "CPF não pode ser nulo ou vazio", groups = { OnCreate.class, OnUpdate.class })
    @CPF(message = "CPF deve ser válido", groups = { OnCreate.class, OnUpdate.class })
    @Column(unique = true)
    private String cpf;

    @OneToOne(cascade = CascadeType.ALL)
    @NotNull(message = "Usuário não pode ser nulo", groups = { OnCreate.class, OnUpdate.class })
    @Valid
    @JoinColumn(name = "usuario_id", referencedColumnName = "id")
    private Usuario usuario;

    @OneToMany(mappedBy = "leitor", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    private List<Emprestimo> emprestimos;

    @OneToMany(mappedBy = "leitor", cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
    private List<Reserva> reservas;

    public Leitor(String nomeCompleto, String telefone, String cpf, Usuario usuario) {
        this.nomeCompleto = nomeCompleto;
        this.telefone = telefone;
        this.cpf = cpf;
        this.usuario = usuario;
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
