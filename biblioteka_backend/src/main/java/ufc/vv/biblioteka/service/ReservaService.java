package ufc.vv.biblioteka.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import ufc.vv.biblioteka.exception.EmprestimoEmAndamentoExistenteException;
import ufc.vv.biblioteka.exception.LimiteExcedidoException;
import ufc.vv.biblioteka.exception.ReservaEmAndamentoExistenteException;
import ufc.vv.biblioteka.exception.ReservaNaoPodeMaisSerCancelaException;
import ufc.vv.biblioteka.model.Leitor;
import ufc.vv.biblioteka.model.Livro;
import ufc.vv.biblioteka.model.Reserva;
import ufc.vv.biblioteka.model.StatusReserva;
import ufc.vv.biblioteka.repository.LeitorRepository;
import ufc.vv.biblioteka.repository.LivroRepository;
import ufc.vv.biblioteka.repository.ReservaRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaService {

    private final LivroRepository livroRepository;

    private final LeitorRepository leitorRepository;

    private final ReservaRepository reservaRepository;

    private final UsuarioService usuarioService;

    @Autowired
    public ReservaService(LivroRepository livroRepository,
            LeitorRepository leitorRepository, UsuarioService usuarioService,
            ReservaRepository reservaRepository) {
        this.livroRepository = livroRepository;
        this.leitorRepository = leitorRepository;
        this.reservaRepository = reservaRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional
    public Reserva reservarLivro(int livroId, int leitorId, String senha) {
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new EntityNotFoundException("Livro não encontrado"));

        Leitor leitor = leitorRepository.findById(leitorId)
                .orElseThrow(() -> new EntityNotFoundException("Leitor não encontrado"));

        if (!usuarioService.verificarSenha(leitor.getUsuario().getId(), senha)) {
            throw new AccessDeniedException("Senha incorreta");
        }

        // Verificar se o leitor já possui uma reserva em andamento para este livro
        boolean reservaExistente = livro.getReservas().stream()
                .anyMatch(reserva -> reserva.getLeitor().getId() == leitorId &&
                        reserva.getStatus() == StatusReserva.EM_ANDAMENTO);

        if (reservaExistente) {
            throw new ReservaEmAndamentoExistenteException("Leitor já possui uma reserva em andamento para este livro");
        }
        
        boolean emprestimoNaoDevolvidoExistente = leitor.getEmprestimos().stream().anyMatch(emprestimo -> emprestimo.getLivro().getId() == livroId && !emprestimo.isDevolvido());

        if (emprestimoNaoDevolvidoExistente) {
            throw new EmprestimoEmAndamentoExistenteException("Leitor já possui um empréstimo em andamento para este livro");
        }
        
        // Verificar limite de empréstimos do leitor
        if (leitor.getQuantidadeReservasRestantes() == 0) {
            throw new LimiteExcedidoException("Limite de reservas excedido para o leitor");
        }

        // Verificar a quantidade de reservas em andamento
        long reservasEmAndamento = livro.getReservas().stream()
                .filter(reserva -> reserva.getStatus() == StatusReserva.EM_ANDAMENTO)
                .count();

        Reserva reserva = new Reserva();
        reserva.setLivro(livro);
        reserva.setLeitor(leitor);
        reserva.setDataCadastro(LocalDate.now());

        if (livro.getNumeroCopiasDisponiveis() > 0 && reservasEmAndamento < livro.getNumeroCopiasDisponiveis()) {
            reserva.marcarComoEmAndamento();
        } else {
            reserva.marcarComoEmEspera();
        }

        return reservaRepository.save(reserva);
    }

    @Transactional
    public Reserva cancelarReserva(int reservaId, String senha) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new EntityNotFoundException("Reserva não encontrada"));

        if (!usuarioService.verificarSenha(reserva.getLeitor().getUsuario().getId(), senha)) {
            throw new AccessDeniedException("Senha incorreta");
        }

        if (reserva.getStatus() != StatusReserva.EM_ESPERA && reserva.getStatus() != StatusReserva.EM_ANDAMENTO) {
            throw new ReservaNaoPodeMaisSerCancelaException("Esta reserva não pode mais ser cancelada");
        }

        reserva.setStatus(StatusReserva.CANCELADA);

        Livro livro = reserva.getLivro();
        Optional<Reserva> reservaEmEsperaMaisAntiga = livro.getReservas().stream()
                .filter(reservaLivro -> reservaLivro.getStatus() == StatusReserva.EM_ESPERA)
                .min(Comparator.comparing(Reserva::getDataCadastro));

        // Se houver uma reserva em espera, atualizar o status e a dataLimite
        if (reservaEmEsperaMaisAntiga.isPresent()) {
            Reserva reservaEmEspera = reservaEmEsperaMaisAntiga.get();
            reservaEmEspera.marcarComoEmAndamento();
            reservaRepository.save(reservaEmEspera);
        }

        reservaRepository.save(reserva);

        return reserva;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void atualizarReservasExpiradas() {
        LocalDate ontem = LocalDate.now().minusDays(1);

        // Buscar todas as reservas em andamento cuja data limite foi ontem
        List<Reserva> reservasExpiradas = reservaRepository.findByStatusAndDataLimiteBefore(
                StatusReserva.EM_ANDAMENTO, ontem);

        // Expirar as reservas em andamento que passaram da data limite
        reservasExpiradas.forEach(reserva -> reserva.setStatus(StatusReserva.EXPIRADA));
        reservaRepository.saveAll(reservasExpiradas);

        // Para cada reserva expirada, tentar ativar uma reserva em espera
        reservasExpiradas.forEach(reservaExpirada -> {
            Livro livro = reservaExpirada.getLivro();
            Optional<Reserva> reservaEmEsperaMaisAntiga = livro.getReservas().stream()
                    .filter(reserva -> reserva.getStatus() == StatusReserva.EM_ESPERA)
                    .min(Comparator.comparing(Reserva::getDataCadastro));

            reservaEmEsperaMaisAntiga.ifPresent(reserva -> {
                reserva.marcarComoEmAndamento();
                reservaRepository.save(reserva);
            });
        });
    }

}