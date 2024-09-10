package ufc.vv.biblioteka.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import ufc.vv.biblioteka.exception.DataRenovacaoException;
import ufc.vv.biblioteka.exception.EmprestimoEmAndamentoExistenteException;
import ufc.vv.biblioteka.exception.EmprestimoJaDevolvidoException;
import ufc.vv.biblioteka.exception.LeitorNaoEncontradoException;
import ufc.vv.biblioteka.exception.LimiteExcedidoException;
import ufc.vv.biblioteka.exception.LivroIndisponivelException;
import ufc.vv.biblioteka.exception.LivroNaoEncontradoException;
import ufc.vv.biblioteka.exception.ReservaEmEsperaException;
import ufc.vv.biblioteka.model.Emprestimo;
import ufc.vv.biblioteka.model.GerenciadorEmprestimoReserva;
import ufc.vv.biblioteka.model.GerenciadorRenovacao;
import ufc.vv.biblioteka.model.Leitor;
import ufc.vv.biblioteka.model.Livro;
import ufc.vv.biblioteka.model.Reserva;
import ufc.vv.biblioteka.model.StatusReserva;
import ufc.vv.biblioteka.repository.EmprestimoRepository;
import ufc.vv.biblioteka.repository.LeitorRepository;
import ufc.vv.biblioteka.repository.LivroRepository;

@Service
public class EmprestimoService {
    private final EmprestimoRepository emprestimoRepository;
    private final UsuarioService usuarioService;
    private final GerenciadorReserva gerenciadorReserva;
    private final LivroRepository livroRepository;
    private final LeitorRepository leitorRepository;
    private final GerenciadorEmprestimoReserva gerenciadorEmprestimoReserva;
    private final GerenciadorRenovacao gerenciadorRenovacao;

    @Autowired
    public EmprestimoService(EmprestimoRepository emprestimoRepository,
            UsuarioService usuarioService,
            GerenciadorReserva gerenciadorReserva,
            LivroRepository livroRepository,
            LeitorRepository leitorRepository,
            GerenciadorEmprestimoReserva gerenciadorEmprestimoReserva, GerenciadorRenovacao gerenciadorRenovacao) {
        this.emprestimoRepository = emprestimoRepository;
        this.usuarioService = usuarioService;
        this.gerenciadorReserva = gerenciadorReserva;
        this.livroRepository = livroRepository;
        this.leitorRepository = leitorRepository;
        this.gerenciadorEmprestimoReserva = gerenciadorEmprestimoReserva;
        this.gerenciadorRenovacao = gerenciadorRenovacao;
    }

    @Transactional
    public Emprestimo emprestarLivro(int livroId, int leitorId, String senha) {
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new LivroNaoEncontradoException("Livro não encontrado"));
        Leitor leitor = leitorRepository.findById(leitorId)
                .orElseThrow(() -> new LeitorNaoEncontradoException("Leitor não encontrado"));

        validarEmprestimo(leitor, livro, senha);

        Reserva reserva = gerenciadorReserva.atualizarReservaSeExistente(leitor.getId(), livro);

        if (reserva != null) {
            livro.emprestarLivro();
            livroRepository.save(livro);
            Emprestimo emprestimo = new Emprestimo(leitor, livro, reserva);
            reserva.setEmprestimo(emprestimo);
            return emprestimoRepository.save(emprestimo);
        } else {
            validarDisponibilidadeParaEmprestimo(livro);
            validarLivroNaoReservado(livro);
            livro.emprestarLivro();
            livroRepository.save(livro);
            Emprestimo emprestimo = new Emprestimo(leitor, livro);
            return emprestimoRepository.save(emprestimo);
        }
    }

    @Transactional
    public Emprestimo renovarEmprestimo(int emprestimoId, String senha) {
        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new EntityNotFoundException("Empréstimo não encontrado"));

        validarRenovacao(emprestimo, senha);

        gerenciadorRenovacao.renovar(emprestimo);
        return emprestimoRepository.save(emprestimo);
    }

    @Transactional
    public Emprestimo devolverLivro(int emprestimoId, String senha) {
        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new EntityNotFoundException("Empréstimo não encontrado"));

        validarSenha(emprestimo.getLeitor().getUsuario().getId(), senha);

        if (emprestimo.isDevolvido())
            throw new EmprestimoJaDevolvidoException("Empréstimo já foi devolvido");
        emprestimo.devolverLivro();
        emprestimoRepository.save(emprestimo);

        Livro livro = emprestimo.getLivro();
        livro.devolverLivro();
        livroRepository.save(livro);
        gerenciadorReserva.ativarProximaReservaEmEspera(livro);

        return emprestimo;
    }

    private void validarEmprestimo(Leitor leitor, Livro livro, String senha) {
        validarSenha(leitor.getUsuario().getId(), senha);
        validarEmprestimoNaoDevolvido(leitor, livro.getId());
        validarLimiteEmprestimos(leitor);
    }

    private void validarRenovacao(Emprestimo emprestimo, String senha) {
        validarSenha(emprestimo.getLeitor().getUsuario().getId(), senha);
        validarEmprestimoDevolvido(emprestimo);
        validarDataLimiteExcedida(emprestimo);
        validarDataRenovacao(emprestimo);
        validarReservasEmEspera(emprestimo.getLivro());
    }

    private void validarDataLimiteExcedida(Emprestimo emprestimo) {
        if (emprestimo.getDataLimite().isAfter(LocalDate.now())) {
            throw new DataRenovacaoException(
                    "A renovação não pode mais ser realizada! Data limite do empréstimo foi ultrapassada");
        }
    }

    private void validarEmprestimoDevolvido(Emprestimo emprestimo) {
        if (emprestimo.isDevolvido()) {
            throw new EmprestimoJaDevolvidoException(
                    "A renovação não pode mais ser realizada! O empreśtimo já foi devolvido");
        }
    }

    private void validarLimiteEmprestimos(Leitor leitor) {
        if (gerenciadorEmprestimoReserva.getQuantidadeEmprestimosRestantes(leitor.getEmprestimos()) == 0) {
            throw new LimiteExcedidoException("Limite de empréstimos excedido para o leitor");
        }
    }

    private void validarDataRenovacao(Emprestimo emprestimo) {
        if (!emprestimo.getDataLimite().equals(LocalDate.now())) {
            throw new DataRenovacaoException("A renovação só pode ser feita na data limite do empréstimo");
        }
    }

    private void validarDisponibilidadeParaEmprestimo(Livro livro) {
        if (livro.getNumeroCopiasDisponiveis() == 0) {
            throw new LivroIndisponivelException("Livro não possui cópias para empréstimo");
        }
    }

    private void validarLivroNaoReservado(Livro livro) {
        long reservasEmAndamento = livro.getReservas().stream()
                .filter(reserva -> reserva.getStatus() == StatusReserva.EM_ANDAMENTO)
                .count();

        if (reservasEmAndamento >= livro.getNumeroCopiasDisponiveis()) {
            throw new LivroIndisponivelException(
                    "Todas as cópias do livro estão reservadas e não estão disponíveis para empréstimo.");
        }

    }

    private void validarReservasEmEspera(Livro livro) {
        boolean reservasEmEsperaExistem = livro.getReservas().stream()
                .anyMatch(reserva -> reserva.getStatus() == StatusReserva.EM_ESPERA);

        if (reservasEmEsperaExistem) {
            throw new ReservaEmEsperaException(
                    "Não é possível renovar o empréstimo, pois há reservas em espera para este livro");
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

}
