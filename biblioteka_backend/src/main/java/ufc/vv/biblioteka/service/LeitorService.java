package ufc.vv.biblioteka.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import ufc.vv.biblioteka.model.Leitor;
import ufc.vv.biblioteka.model.TipoUsuario;
import ufc.vv.biblioteka.model.Usuario;
import ufc.vv.biblioteka.repository.LeitorRepository;
import ufc.vv.biblioteka.repository.UsuarioRepository;

@Service
public class LeitorService {

    private final LeitorRepository leitorRepository;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public LeitorService(LeitorRepository leitorRepository,
            UsuarioService usuarioService, UsuarioRepository usuarioRepository) {
        this.leitorRepository = leitorRepository;
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
    }

    private Leitor buscarLeitorPorId(int leitorId) {
        return leitorRepository.findById(leitorId)
                .orElseThrow(() -> new EntityNotFoundException("Leitor não encontrado"));
    }

    @Transactional
    public Leitor criarLeitor(Leitor leitor) {
        validarLeitorAoCadastrar(leitor);

        Usuario usuario = leitor.getUsuario();
        usuario.setTipoUsuario(TipoUsuario.LEITOR);
        usuarioService.save(usuario);
        return leitorRepository.save(leitor);
    }

    public Leitor atualizarLeitor(int id, Leitor leitorAtualizado) {
     
        Leitor leitorExistente = buscarLeitorPorId(id);

        validarLeitorEditado(leitorExistente, leitorAtualizado);

        leitorExistente.setNomeCompleto(leitorAtualizado.getNomeCompleto());
        leitorExistente.setTelefone(leitorAtualizado.getTelefone());
        leitorExistente.getUsuario().setEmail(leitorAtualizado.getUsuario().getEmail());

        return leitorRepository.save(leitorExistente);
    }

    public void excluirLeitor(int id) {
        Leitor leitor = buscarLeitorPorId(id);
        validarExclusao(leitor);
        leitorRepository.delete(leitor);
    }

    private void validarLeitorAoCadastrar(Leitor leitor) {

        if (usuarioRepository.existsByEmailIgnoresCase(leitor.getUsuario().getEmail())) {
            throw new DuplicateKeyException("Um usuário com este e-mail já está cadastrado.");
        }

        if (leitorRepository.existsByCpf(leitor.getCpf())) {
            throw new DuplicateKeyException("Um usuário com este CPF já está cadastrado.");
        }
    }

    private void validarExclusao(Leitor leitor) {
        if ((leitor.getEmprestimos() != null && !leitor.getEmprestimos().isEmpty()) ||
                (leitor.getReservas() != null && !leitor.getReservas().isEmpty())) {
            throw new DataIntegrityViolationException(
                    "Não é possível excluir o leitor porque ele tem empréstimos ou reservas.");
        }
    }

    private void validarLeitorEditado(Leitor existingLeitor, Leitor updatedLeitor) {
        if (!existingLeitor.getUsuario().getEmail().equals(updatedLeitor.getUsuario().getEmail()) &&
                usuarioRepository.existsByEmailIgnoresCase(updatedLeitor.getUsuario().getEmail())) {
            throw new DuplicateKeyException("Um leitor com este e-mail já está cadastrado.");
        }
    }
}
