package ufc.vv.biblioteka.repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import ufc.vv.biblioteka.model.Colecao;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ColecaoRepositoryTest {

    @Autowired
    private ColecaoRepository colecaoRepository;

    @BeforeEach
    void setUp() {
        Colecao colecao = new Colecao();
        colecao.setNome("Coleção de Teste");
        colecao.setDescricao("Uma descrição qualquer");
        colecaoRepository.save(colecao);
    }

    @Test
    void shouldFindByNomeAndDescricao() {
        Page<Colecao> result = colecaoRepository.findByNomeAndDescricao("Teste", PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getNome()).isEqualTo("Coleção de Teste");
    }

    @Test
    void shouldCheckIfColecaoExistsByNome() {
        boolean exists = colecaoRepository.existsByNome("Coleção de Teste");
        assertThat(exists).isTrue();
    }

    @Test
    void shouldFindAllWithFilter() {
        var colecoes = colecaoRepository.findAllWithFilter("Teste");
        assertThat(colecoes).hasSize(1);
        assertThat(colecoes.get(0).getNome()).isEqualTo("Coleção de Teste");
    }
}