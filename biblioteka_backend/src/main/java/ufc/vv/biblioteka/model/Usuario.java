package ufc.vv.biblioteka.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
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
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Email não pode ser nulo ou vazio", groups = { OnCreate.class, OnUpdate.class })
    @Email(message = "E-mail deve ser válido", groups = { OnCreate.class, OnUpdate.class })
    @Column(unique = true)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotNull(message = "Senha não pode ser nula ou vazia", groups = { OnCreate.class })
    @Size(min = 8, max = 64, message = "A senha deve ter entre 8 e 64 caracteres", groups = { OnCreate.class })
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])\\S+$", message = "A senha deve conter pelo menos um número, uma letra maiúscula, uma letra minúscula, e um caractere especial. Também não pode conter espaços!", groups = {
            OnCreate.class })
    private String senha;

    @NotNull(message = "Tipo de usuário não pode ser nulo", groups = OnSave.class)
    @Enumerated(EnumType.STRING)
    private TipoUsuario tipoUsuario;

    public Usuario(String email, String senha, TipoUsuario tipoUsuario) {
        this.email = email;
        this.senha = senha;
        this.tipoUsuario = tipoUsuario;
    }

}