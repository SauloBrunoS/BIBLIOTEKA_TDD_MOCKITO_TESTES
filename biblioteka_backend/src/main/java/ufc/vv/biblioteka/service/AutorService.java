package ufc.vv.biblioteka.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import ufc.vv.biblioteka.model.Autor;
import ufc.vv.biblioteka.repository.AutorRepository;

@Service
public class AutorService {

    private final AutorRepository autorRepository;

    @Autowired
    public AutorService(AutorRepository autorRepository) {
        this.autorRepository = autorRepository;
    }

    public Autor createAutor(Autor autor) {
        validarNovoAutor(autor);
        return autorRepository.save(autor);
    }

    public Autor updateAutor(int id, Autor updatedAutor) {
        Autor existingAutor = buscarAutorPorId(id);
        validarAutorEditado(existingAutor, updatedAutor);

        existingAutor.setDataNascimento(updatedAutor.getDataNascimento());
        existingAutor.setNacionalidade(updatedAutor.getNacionalidade());
        existingAutor.setNomeCompleto(updatedAutor.getNomeCompleto());

        return autorRepository.save(existingAutor);
    }

    public void deleteAutorById(int id) {
        Autor autor = buscarAutorPorId(id);
        validarAssociacaoComLivros(autor);
        autorRepository.delete(autor);
    }

    private Autor buscarAutorPorId(int id) {
        return autorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Autor não encontrado"));
    }

    private void validarNovoAutor(Autor autor) {
        if (autorRepository.existsByNomeCompletoIgnoreCase(autor.getNomeCompleto())) {
            throw new DuplicateKeyException("Um autor com este nome já está cadastrado.");
        }
    }

    private void validarAutorEditado(Autor existingAutor, Autor updatedAutor) {

        if (!existingAutor.getNomeCompleto().equals(updatedAutor.getNomeCompleto()) &&
                autorRepository.existsByNomeCompletoIgnoreCase(updatedAutor.getNomeCompleto())) {
            throw new DuplicateKeyException("Um autor com este nome já está cadastrado.");
        }
    }

    private void validarAssociacaoComLivros(Autor autor) {
        if (autor.getLivros() != null && !autor.getLivros().isEmpty()) {
            throw new DataIntegrityViolationException(
                    "Não é possível excluir o autor porque ele tem livros associados.");
        }
    }
}