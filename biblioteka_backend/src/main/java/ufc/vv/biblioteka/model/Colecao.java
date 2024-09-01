package ufc.vv.biblioteka.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.persistence.JoinColumn;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

@Entity
@Data
@JsonIdentityInfo(generator = JSOGGenerator.class)
@EqualsAndHashCode(of = "id")
public class Colecao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    @NotBlank
    @Column(unique = true)
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres.")
    @Pattern(regexp = "^[\\p{L}\\p{N}'\\-\\s.,()/:;]+$", message = "O nome pode conter apenas letras, números, espaços, apóstrofos, hífens, pontos, vírgulas, parênteses, barras, dois pontos e ponto e vírgula.")
    private String nome;

    @NotBlank
    private String descricao;

    @ManyToMany
    @JoinTable(name = "colecao_livro", joinColumns = @JoinColumn(name = "colecao_id"), inverseJoinColumns = @JoinColumn(name = "livro_id"))
    private List<Livro> livros;

    @PrePersist
    @PreUpdate
    private void trimNome() {
        if (nome != null) {
            nome = nome.toLowerCase().trim();
        }
        if(descricao != null){
            descricao = descricao.trim();
        }
    }

}
