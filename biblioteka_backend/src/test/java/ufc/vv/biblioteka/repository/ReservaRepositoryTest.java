package ufc.vv.biblioteka.repository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import jakarta.transaction.Transactional;
import ufc.vv.biblioteka.model.Leitor;
import ufc.vv.biblioteka.model.Livro;
import ufc.vv.biblioteka.model.Reserva;
import ufc.vv.biblioteka.model.StatusReserva;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;


import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
public class ReservaRepositoryTest {

    @Autowired
    private ReservaRepository reservaRepository;

    @Test
    public void deveEncontrarReservaPorLivroId() {
        // Dado
        Reserva reserva = new Reserva();
        Livro livro = new Livro();
        livro.setId(1); // Assume que o livro com ID 1 existe
        reserva.setLivro(livro);
        reserva.setDataCadastro(LocalDate.now());
        reservaRepository.save(reserva);

        // Quando
        Page<Reserva> reservas = reservaRepository.findByLivroIdAndSearch(1, "now", null, null, PageRequest.of(0, 10));

        // Então
        assertFalse(reservas.isEmpty());
    }

    @Test
    public void deveEncontrarReservaPorLeitorId() {
        // Dado
        Reserva reserva = new Reserva();
        Leitor leitor = new Leitor();
        leitor.setId(1); // Assume que o leitor com ID 1 existe
        reserva.setLeitor(leitor);
        reservaRepository.save(reserva);

        // Quando
        Page<Reserva> reservas = reservaRepository.findByLeitorIdAndSearch(1, "", null, null, PageRequest.of(0, 10));

        // Então
        assertFalse(reservas.isEmpty());
    }

    @Test
    public void deveContarReservasPorStatus() {
        // Dado
        Reserva reserva = new Reserva();
        reserva.setStatus(StatusReserva.EM_ANDAMENTO);
        reservaRepository.save(reserva);

        // Quando
        int quantidade = reservaRepository.countByStatus(StatusReserva.EM_ANDAMENTO);

        // Então
        assertTrue(quantidade > 0);
    }
}