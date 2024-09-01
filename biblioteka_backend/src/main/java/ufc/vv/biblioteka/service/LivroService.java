package ufc.vv.biblioteka.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.validator.routines.ISBNValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import ufc.vv.biblioteka.exception.LimiteExcedidoException;
import ufc.vv.biblioteka.model.Autor;
import ufc.vv.biblioteka.model.Colecao;
import ufc.vv.biblioteka.model.Livro;
import ufc.vv.biblioteka.model.Reserva;
import ufc.vv.biblioteka.model.StatusReserva;
import ufc.vv.biblioteka.repository.AutorRepository;
import ufc.vv.biblioteka.repository.ColecaoRepository;
import ufc.vv.biblioteka.repository.EmprestimoRepository;
import ufc.vv.biblioteka.repository.LivroRepository;
import ufc.vv.biblioteka.repository.ReservaRepository;

@Service
public class LivroService {

    private final LivroRepository livroRepository;

    private final AutorRepository autorRepository;

    private final EmprestimoRepository emprestimorRepository;

    private final ReservaRepository reservaRepository;

    private final ColecaoRepository colecaoRepository;

    ISBNValidator isbnValidator = new ISBNValidator();

    @Autowired
    public LivroService(LivroRepository livroRepository, AutorRepository autorRepository,
            ColecaoRepository colecaoRepository, EmprestimoRepository emprestimorRepository,
            ReservaRepository reservaRepository) {
        this.livroRepository = livroRepository;
        this.autorRepository = autorRepository;
        this.colecaoRepository = colecaoRepository;
        this.emprestimorRepository = emprestimorRepository;
        this.reservaRepository = reservaRepository;
    }

    public Livro adicionarLivro(Livro livro) {
        validarLivro(livro);
        List<Autor> autores = autorRepository.findAllById(livro.getAutores().stream()
                .map(Autor::getId)
                .collect(Collectors.toList()));
        livro.setAutores(autores);

        List<Colecao> colecoes = colecaoRepository.findAllById(livro.getColecoes().stream()
                .map(Colecao::getId)
                .collect(Collectors.toList()));
        livro.setColecoes(colecoes);
        return livroRepository.save(livro);
    }

    public Livro atualizarLivro(int id, Livro livro) {
        Livro livroAntigo = livroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Livro não encontrado."));

        validarLivro(livro);
        livro.setId(id);
        int qtdLivrosNaoDevolvidos = emprestimorRepository.countByDevolvidoFalse();
        int qtdReservasEmAndamento = reservaRepository.countByStatus(StatusReserva.EM_ANDAMENTO);
        if (livro.getNumeroCopiasTotais() < (qtdLivrosNaoDevolvidos + qtdReservasEmAndamento)) {
            throw new LimiteExcedidoException(
                    "A quantidade de cópias do livro não pode ser reduzida a essa quantidade por ser menor que a quantidade de cópias desse livro que estão relacionadas a empréstimos e reservas em andamento");
        }
        int diferenca = livro.getNumeroCopiasTotais() - livroAntigo.getNumeroCopiasTotais();
        livro.setNumeroCopiasDisponiveis(livroAntigo.getNumeroCopiasDisponiveis() + diferenca);

        if (diferenca > 0) {
            for (int i = 0; i < diferenca; i++) {
                Optional<Reserva> reservaEmEsperaMaisAntiga = livroAntigo.getReservas().stream()
                        .filter(reserva -> reserva.getStatus() == StatusReserva.EM_ESPERA)
                        .min(Comparator.comparing(Reserva::getDataCadastro));

                if (reservaEmEsperaMaisAntiga.isEmpty()) {
                    break;
                }
                reservaEmEsperaMaisAntiga.ifPresent(reserva -> {
                    reserva.marcarComoEmAndamento();
                    reservaRepository.save(reserva);
                });
            }
        }

        List<Autor> autores = autorRepository.findAllById(livro.getAutores().stream()
                .map(Autor::getId)
                .collect(Collectors.toList()));
        livro.setAutores(autores);

        List<Colecao> colecoes = colecaoRepository.findAllById(livro.getColecoes().stream()
                .map(Colecao::getId)
                .collect(Collectors.toList()));
        livro.setColecoes(colecoes);
        return livroRepository.save(livro);
    }

    public void excluirLivro(int id) {
        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Livro não encontrado."));
        if (!livro.getEmprestimos().isEmpty() || !livro.getReservas().isEmpty()) {
            throw new DataIntegrityViolationException(
                    "O livro não pode ser excluído, pois está associado a empréstimos ou reservas.");
        }
        livroRepository.delete(livro);
    }

    private void validarLivro(Livro livro) {

        if (livro == null) {
            throw new IllegalArgumentException("Livro não pode ser nulo.");
        }

        if (livro.getTitulo() == null || livro.getTitulo().isEmpty()) {
            throw new IllegalArgumentException("O título do livro não pode ser nulo ou vazio.");
        }

        if (livro.getAutores() == null || livro.getAutores().isEmpty()) {
            throw new IllegalArgumentException("A lista de autores não pode ser nula ou vazia.");
        }

        if (livro.getColecoes() == null || livro.getColecoes().isEmpty()) {
            throw new IllegalArgumentException("A lista de autores não pode ser nula ou vazia.");
        }

        if (livro.getIsbn() == null || livro.getIsbn().isEmpty()) {
            throw new IllegalArgumentException("O ISBN do livro não pode ser nulo ou vazio.");
        }

        if (!isbnValidator.isValidISBN13(livro.getIsbn()) && !isbnValidator.isValidISBN10(livro.getIsbn())) {
            throw new IllegalArgumentException("O ISBN possui formato inválido.");
        }

        if (livro.getDataPublicacao() == null) {
            throw new IllegalArgumentException("A data de publicação do livro não pode ser nula.");
        }

        if (livro.getDataPublicacao().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data de publicação não pode ser uma data futura.");
        }

        if (livro.getNumeroCopiasDisponiveis() < 0) {
            throw new IllegalArgumentException("Número de cópias disponíves não pode ser negativo.");
        }

        if (livro.getNumeroCopiasTotais() < 0) {
            throw new IllegalArgumentException("Número de cópias totais não pode ser negativo.");
        }

        if (livro.getNumeroCopiasDisponiveis() > livro.getNumeroCopiasTotais()) {
            throw new IllegalArgumentException(
                    "Número de cópias totais não pode ser menor que número de cópias disponíveis.");
        }

        if (livro.getQtdPaginas() < 0) {
            throw new IllegalArgumentException("Número de páginas não pode ser menor que 1.");
        }
    }

}