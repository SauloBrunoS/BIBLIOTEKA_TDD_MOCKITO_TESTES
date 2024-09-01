package ufc.vv.biblioteka.repository;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import ufc.vv.biblioteka.model.Autor;
import ufc.vv.biblioteka.model.Nacionalidade;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AutorRepositoryTest {

    @Autowired
    private AutorRepository autorRepository;

    @BeforeEach
    void setUp() {
        Autor autor = new Autor();
        autor.setNomeCompleto("João Silva");
        autor.setDataNascimento(LocalDate.of(1980, 1, 1));
        autor.setNacionalidade(Nacionalidade.BRASIL);
        autorRepository.save(autor);
    }

    @Test
    void shouldFindByAllFields() {
        Page<Autor> result = autorRepository.findByAllFields("João", Nacionalidade.BRASIL, PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getNomeCompleto()).isEqualTo("João Silva");
    }

    @Test
    void shouldFindByNomeCompletoIgnoreCase() {
        boolean exists = autorRepository.existsByNomeCompleto("João Silva");
        assertThat(exists).isTrue();
    }

    @Test
    void shouldFindAllWithFilter() {
        var autores = autorRepository.findAllWithFilter("João");
        assertThat(autores).hasSize(1);
        assertThat(autores.get(0).getNomeCompleto()).isEqualTo("João Silva");
    }
}