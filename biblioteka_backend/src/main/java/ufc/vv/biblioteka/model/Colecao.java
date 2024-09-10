package ufc.vv.biblioteka.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

@Entity
@Data
@JsonIdentityInfo(generator = JSOGGenerator.class)
@NoArgsConstructor
public class Colecao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Nome não pode ser nulo ou vazio")
    @Column(unique = true)
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres.")
    @Pattern(regexp = "^[\\p{L}\\p{N}'\\-\\s.,()/:;]+$", message = "O nome pode conter apenas letras, números, espaços, apóstrofos, hífens, pontos, vírgulas, parênteses, barras, dois pontos e ponto e vírgula.")
    private String nome;

    @NotBlank(message = "Descrição não pode ser nula ou vazia")
    @Size(min = 10, message = "A descrição deve ter no mínimo 10 caracteres.")
    private String descricao;

    @ManyToMany(mappedBy = "colecoes")
    private List<Livro> livros;

    @PrePersist
    @PreUpdate
    private void trimNome() {
        if (nome != null) {
            nome = nome.trim();
        }
        if (descricao != null) {
            descricao = descricao.trim();
        }
    }

    public Colecao(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public Colecao(String nome) {
        this.nome = nome;
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
