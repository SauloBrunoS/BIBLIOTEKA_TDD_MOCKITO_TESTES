package ufc.vv.biblioteka.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;

import ufc.vv.biblioteka.model.Colecao;
import ufc.vv.biblioteka.repository.ColecaoRepository;

@Service
public class ColecaoService {

    private final ColecaoRepository colecaoRepository;

    @Autowired
    public ColecaoService(ColecaoRepository colecaoRepository) {
        this.colecaoRepository = colecaoRepository;
    }

    public Colecao createColecao(Colecao colecao) {
        validarNovaColecao(colecao);
        return colecaoRepository.save(colecao);
    }

    public Colecao updateColecao(int id, Colecao updatedColecao) {
        Colecao existingColecao = buscarColecaoPorId(id);
        validarColecaoEditada(existingColecao, updatedColecao);
        existingColecao.setNome(updatedColecao.getNome());
        existingColecao.setDescricao(updatedColecao.getDescricao());
        return colecaoRepository.save(existingColecao);
    }

    public void deleteColecaoById(int id) {
        Colecao existingColecao = buscarColecaoPorId(id);
        validarAssociacaoComLivros(existingColecao);
        colecaoRepository.delete(existingColecao);
    }

    private Colecao buscarColecaoPorId(int id) {
        return colecaoRepository.findByIdWithLivros(id)
                .orElseThrow(() -> new EntityNotFoundException("Coleção não encontrada"));
    }

    private void validarNovaColecao(Colecao colecao) {
        if (colecaoRepository.existsByNomeIgnoreCase(colecao.getNome())) {
            throw new DuplicateKeyException("Uma coleção com este nome já está cadastrada.");
        }
    }

    private void validarAssociacaoComLivros(Colecao colecao) {
        if (colecao.getLivros() != null && !colecao.getLivros().isEmpty()) {
            throw new DataIntegrityViolationException(
                    "Não é possível excluir a coleção porque ela tem livros associados.");
        }
    }

    private void validarColecaoEditada(Colecao existingColecao, Colecao updatedColecao) {

        if (!existingColecao.getNome().equals(updatedColecao.getNome()) &&
                colecaoRepository.existsByNomeIgnoreCase(updatedColecao.getNome())) {
            throw new DuplicateKeyException("Uma coleção com este nome já está cadastrada.");
        }
    }
}