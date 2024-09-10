package ufc.vv.biblioteka.service;

import org.apache.commons.validator.routines.ISBNValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import ufc.vv.biblioteka.model.Autor;
import ufc.vv.biblioteka.model.Colecao;
import ufc.vv.biblioteka.model.Livro;
import ufc.vv.biblioteka.repository.AutorRepository;
import ufc.vv.biblioteka.repository.ColecaoRepository;
import ufc.vv.biblioteka.repository.LivroRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LivroService {

    private final LivroRepository livroRepository;
    private final GerenciadorReserva gerenciadorReserva;
    private final ISBNValidator isbnValidator;
    private final AutorRepository autorRepository;
    private final ColecaoRepository colecaoRepository;

    @Autowired
    public LivroService(LivroRepository livroRepository,
            GerenciadorReserva gerenciadorReserva, ISBNValidator isbnValidator, AutorRepository autorRepository,
            ColecaoRepository colecaoRepository) {
        this.livroRepository = livroRepository;
        this.gerenciadorReserva = gerenciadorReserva;
        this.isbnValidator = isbnValidator;
        this.autorRepository = autorRepository;
        this.colecaoRepository = colecaoRepository;
    }

    private Livro buscarLivroPorId(int livroId) {
        return livroRepository.findById(livroId)
                .orElseThrow(() -> new EntityNotFoundException("Livro não encontrado"));
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
        Livro livroAntigo = buscarLivroPorId(id);
        validarLivroEditado(livroAntigo, livro);
        gerenciadorReserva.ajustarReservasParaNovoNumeroDeCopias(livroAntigo, livro);
        List<Autor> autores = autorRepository.findAllById(livro.getAutores().stream()
                .map(Autor::getId)
                .collect(Collectors.toList()));
        livro.setAutores(autores);

        List<Colecao> colecoes = colecaoRepository.findAllById(livro.getColecoes().stream()
                .map(Colecao::getId)
                .collect(Collectors.toList()));
        livro.setColecoes(colecoes);
        livro.setId(id);
        return livroRepository.save(livro);
    }

    public void excluirLivro(int id) {
        Livro livro = buscarLivroPorId(id);
        validarExclusaoLivro(livro);
        livroRepository.delete(livro);
    }

    private void validarLivro(Livro livro) {

        if (!isbnValidator.isValidISBN13(livro.getIsbn()) && !isbnValidator.isValidISBN10(livro.getIsbn())) {
            throw new IllegalArgumentException("O ISBN informado é inválido.");
        }

        if (livroRepository.existsByIsbn(livro.getIsbn())) {
            throw new DuplicateKeyException("Um livro com este isbn já está cadastrado.");
        }

    }

    private void validarLivroEditado(Livro existingLivro, Livro updatedLivro) {

        if (!updatedLivro.getIsbn().equals(existingLivro.getIsbn())
                && !isbnValidator.isValidISBN13(updatedLivro.getIsbn())
                && !isbnValidator.isValidISBN10(updatedLivro.getIsbn())) {
            throw new IllegalArgumentException("O ISBN informado é inválido.");
        }

        if (!updatedLivro.getIsbn().equals(existingLivro.getIsbn())
                && livroRepository.existsByIsbn(updatedLivro.getIsbn())) {
            throw new DuplicateKeyException("Um livro com este isbn já está cadastrado.");
        }

    }

    private void validarExclusaoLivro(Livro livro) {
        if ((livro.getEmprestimos() != null && !livro.getEmprestimos().isEmpty())
                || (livro.getReservas() != null && !livro.getReservas().isEmpty())) {
            throw new DataIntegrityViolationException(
                    "O livro não pode ser excluído, pois está associado a empréstimos ou reservas.");
        }
    }

}
