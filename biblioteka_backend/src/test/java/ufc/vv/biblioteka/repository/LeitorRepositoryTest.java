package ufc.vv.biblioteka.repository;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;

import jakarta.transaction.Transactional;
import ufc.vv.biblioteka.model.Leitor;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
public class LeitorRepositoryTest {

    @Autowired
    private LeitorRepository leitorRepository;

    @Test
    public void deveEncontrarLeitorPorTodosOsCampos() {
        // Dado
        Leitor leitor = new Leitor();
        leitor.setNomeCompleto("José da Silva");
        leitor.setCpf("12345678900");
        leitorRepository.save(leitor);

        // Quando
        Page<Leitor> leitores = leitorRepository.findByAllFields("José", PageRequest.of(0, 10));

        // Então
        assertFalse(leitores.isEmpty());
    }

    @Test
    public void deveVerificarSeExisteLeitorPorCpf() {
        // Dado
        Leitor leitor = new Leitor();
        leitor.setCpf("98765432100");
        leitorRepository.save(leitor);

        // Quando
        boolean existe = leitorRepository.existsByCpf("98765432100");

        // Então
        assertTrue(existe);
    }

    @Test
    public void deveFiltrarLeitoresPorCpf() {
        // Dado
        Leitor leitor = new Leitor();
        leitor.setCpf("12345678900");
        leitorRepository.save(leitor);

        // Quando
        List<Leitor> leitores = leitorRepository.findAllWithCPFFilter("123456");

        // Então
        assertFalse(leitores.isEmpty());
    }
}
