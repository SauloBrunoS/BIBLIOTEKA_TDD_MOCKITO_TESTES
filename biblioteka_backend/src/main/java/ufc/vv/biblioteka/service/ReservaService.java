package ufc.vv.biblioteka.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import ufc.vv.biblioteka.exception.EmprestimoEmAndamentoExistenteException;
import ufc.vv.biblioteka.exception.LeitorNaoEncontradoException;
import ufc.vv.biblioteka.exception.LimiteExcedidoException;
import ufc.vv.biblioteka.exception.LivroNaoEncontradoException;
import ufc.vv.biblioteka.exception.ReservaEmAndamentoExistenteException;
import ufc.vv.biblioteka.exception.ReservaNaoPodeMaisSerCancelaException;
import ufc.vv.biblioteka.model.GerenciadorEmprestimoReserva;
import ufc.vv.biblioteka.model.Leitor;
import ufc.vv.biblioteka.model.Livro;
import ufc.vv.biblioteka.model.Reserva;
import ufc.vv.biblioteka.model.StatusReserva;
import ufc.vv.biblioteka.repository.LeitorRepository;
import ufc.vv.biblioteka.repository.LivroRepository;
import ufc.vv.biblioteka.repository.ReservaRepository;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReservaService {

    private final LivroRepository livroRepository;

    private final LeitorRepository leitorRepository;

    private final ReservaRepository reservaRepository;

    private final UsuarioService usuarioService;

    private final GerenciadorReserva gerenciadorReserva;

    private final GerenciadorEmprestimoReserva gerenciadorEmprestimoReserva;

    @Autowired
    public ReservaService(LivroRepository livroRepository,
            LeitorRepository leitorRepository, GerenciadorEmprestimoReserva gerenciadorEmprestimoReserva,
            ReservaRepository reservaRepository, UsuarioService usuarioService, GerenciadorReserva gerenciadorReserva) {
        this.livroRepository = livroRepository;
        this.leitorRepository = leitorRepository;
        this.reservaRepository = reservaRepository;
        this.usuarioService = usuarioService;
        this.gerenciadorReserva = gerenciadorReserva;
        this.gerenciadorEmprestimoReserva = gerenciadorEmprestimoReserva;
    }

    @Transactional
    public Reserva reservarLivro(int livroId, int leitorId, String senha) {
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new LivroNaoEncontradoException("Livro não encontrado"));
        Leitor leitor = leitorRepository.findById(leitorId)
                .orElseThrow(() -> new LeitorNaoEncontradoException("Leitor não encontrado"));

        validarSenha(leitor.getUsuario().getId(), senha);
        validarReservaExistente(livro, leitor.getId());
        validarEmprestimoNaoDevolvido(leitor, livroId);
        validarLimiteReservas(leitor);

        Reserva reserva = new Reserva(livro, leitor);
        gerenciadorReserva.atualizarStatusReserva(livro, reserva);

        return reservaRepository.save(reserva);
    }

    @Transactional
    public Reserva cancelarReserva(int reservaId, String senha) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new EntityNotFoundException("Reserva não encontrada"));

        validarSenha(reserva.getLeitor().getUsuario().getId(), senha);
        validarCancelamento(reserva);

        reserva.marcarComoCancelada();
        gerenciadorReserva.ativarProximaReservaEmEspera(reserva.getLivro());

        return reservaRepository.save(reserva);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void atualizarReservasExpiradas() {
        List<Reserva> reservasExpiradas = reservaRepository
                .findByStatusAndDataLimiteBefore(StatusReserva.EM_ANDAMENTO, LocalDate.now().minusDays(1));

        reservasExpiradas.forEach(reserva -> reserva.marcarComoExpirada());
        reservaRepository.saveAll(reservasExpiradas);

        reservasExpiradas.forEach(reserva -> gerenciadorReserva.ativarProximaReservaEmEspera(reserva.getLivro()));
    }

    private void validarLimiteReservas(Leitor leitor) {
        if (gerenciadorEmprestimoReserva.getQuantidadeReservasRestantes(leitor.getReservas()) == 0) {
            throw new LimiteExcedidoException("Limite de reservas excedido para o leitor");
        }
    }

    private void validarCancelamento(Reserva reserva) {
        if (reserva.getStatus() != StatusReserva.EM_ESPERA && reserva.getStatus() != StatusReserva.EM_ANDAMENTO) {
            throw new ReservaNaoPodeMaisSerCancelaException(
                    "Esta reserva não pode mais ser cancelada, pois não está em espera ou andamento");
        }
    }

    private void validarEmprestimoNaoDevolvido(Leitor leitor, int livroId) {
        boolean emprestimoNaoDevolvidoExistente = leitor.getEmprestimos().stream()
                .anyMatch(emprestimo -> emprestimo.getLivro().getId() == livroId && !emprestimo.isDevolvido());
        if (emprestimoNaoDevolvidoExistente) {
            throw new EmprestimoEmAndamentoExistenteException(
                    "Leitor já possui um empréstimo não devolvido para este livro");
        }
    }

    private void validarSenha(int idUsuario, String senha) {
        if (!usuarioService.verificarSenha(idUsuario, senha)) {
            throw new AccessDeniedException("Senha não reconhecida");
        }
    }

    private void validarReservaExistente(Livro livro, int leitorId) {
        boolean reservaExistente = livro.getReservas().stream()
                .anyMatch(reserva -> reserva.getLeitor().getId() == leitorId &&
                        reserva.getStatus() == StatusReserva.EM_ANDAMENTO);

        if (reservaExistente) {
            throw new ReservaEmAndamentoExistenteException("Leitor já possui uma reserva em andamento para este livro");
        }
    }

}