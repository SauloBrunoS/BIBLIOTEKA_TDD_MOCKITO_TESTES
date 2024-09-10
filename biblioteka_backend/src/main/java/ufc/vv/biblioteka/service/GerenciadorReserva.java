package ufc.vv.biblioteka.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ufc.vv.biblioteka.exception.LimiteExcedidoException;
import ufc.vv.biblioteka.model.Livro;
import ufc.vv.biblioteka.model.Reserva;
import ufc.vv.biblioteka.model.StatusReserva;
import ufc.vv.biblioteka.repository.EmprestimoRepository;
import ufc.vv.biblioteka.repository.ReservaRepository;

import java.util.Comparator;
import java.util.Optional;

@Service
public class GerenciadorReserva {
    private final ReservaRepository reservaRepository;
    private final EmprestimoRepository emprestimoRepository;

    @Autowired
    public GerenciadorReserva(ReservaRepository reservaRepository, EmprestimoRepository emprestimoRepository) {
        this.reservaRepository = reservaRepository;
        this.emprestimoRepository = emprestimoRepository;
    }

    public Reserva atualizarReservaSeExistente(int leitorId, Livro livro) {
        if (livro.getReservas() == null)
            throw new IllegalStateException("Lista de reservas não pode ser nula");
        Optional<Reserva> reservaOptional = livro.getReservas().stream()
                .filter(reserva -> reserva.getLeitor().getId() == leitorId
                        && reserva.getStatus() == StatusReserva.EM_ANDAMENTO)
                .findFirst();

        if (reservaOptional.isPresent()) {
            Reserva reserva = reservaOptional.get();
            reserva.marcarComoAtendida();
            return reserva;
        }
        return null;
    }

    public void ajustarReservasParaNovoNumeroDeCopias(Livro livroAntigo, Livro livroNovo) {
        int qtdLivrosNaoDevolvidos = emprestimoRepository.countByDevolvidoFalseAndLivroId(livroAntigo.getId());
        int qtdReservasEmAndamento = reservaRepository.countByStatusAndLivroId(StatusReserva.EM_ANDAMENTO,
                livroAntigo.getId());
        if (livroNovo.getNumeroCopiasTotais() < (qtdLivrosNaoDevolvidos + qtdReservasEmAndamento)) {
            throw new LimiteExcedidoException(
                    "A quantidade de cópias do livro não pode ser reduzida a essa quantidade por ser menor que a quantidade de cópias desse livro que estão relacionadas a empréstimos e reservas em andamento");
        }
        int diferenca = livroNovo.getNumeroCopiasTotais() - livroAntigo.getNumeroCopiasTotais();
        livroNovo.setNumeroCopiasDisponiveis(livroAntigo.getNumeroCopiasDisponiveis() + diferenca);

        if (diferenca > 0) {
            for (int i = 0; i < diferenca; i++) {
                ativarProximaReservaEmEspera(livroAntigo);
            }
        }
    }

    public void ativarProximaReservaEmEspera(Livro livro) {
        if (livro.getReservas() == null)
            throw new IllegalStateException("Lista de reservas não pode ser nula");
        Optional<Reserva> reservaEmEsperaMaisAntiga = livro.getReservas().stream()
                .filter(reserva -> reserva.getStatus() == StatusReserva.EM_ESPERA)
                .min(Comparator.comparing(Reserva::getDataCadastro));

        reservaEmEsperaMaisAntiga.ifPresent(reserva -> {
            reserva.marcarComoEmAndamento();
            reservaRepository.save(reserva);
        });
    }

    public void atualizarStatusReserva(Livro livro, Reserva reserva) {
        if (livro.getReservas() == null)
            throw new IllegalStateException("Lista de reservas não pode ser nula");
        long reservasEmAndamento = livro.getReservas().stream()
                .filter(r -> r.getStatus() == StatusReserva.EM_ANDAMENTO)
                .count();

        if (livro.getNumeroCopiasDisponiveis() > reservasEmAndamento) {
            reserva.marcarComoEmAndamento();
        } else {
            reserva.marcarComoEmEspera();
        }
    }
}
