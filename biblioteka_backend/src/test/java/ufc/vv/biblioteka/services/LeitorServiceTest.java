package ufc.vv.biblioteka.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import jakarta.persistence.EntityNotFoundException;
import ufc.vv.biblioteka.model.Emprestimo;
import ufc.vv.biblioteka.model.Leitor;
import ufc.vv.biblioteka.model.Reserva;
import ufc.vv.biblioteka.model.TipoUsuario;
import ufc.vv.biblioteka.model.Usuario;
import ufc.vv.biblioteka.repository.LeitorRepository;
import ufc.vv.biblioteka.repository.UsuarioRepository;
import ufc.vv.biblioteka.service.LeitorService;
import ufc.vv.biblioteka.service.UsuarioService;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class LeitorServiceTest {

    @Mock
    private LeitorRepository leitorRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private LeitorService leitorService;

    private Leitor leitor;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario("email@teste.com", "Senha@123", TipoUsuario.LEITOR);
        leitor = new Leitor("João da Silva", "88993570006", "94182774094", usuario);
        leitor.setId(1);
    }

    @Test
    void testCriarLeitor_Success() {
        when(usuarioRepository.existsByEmailIgnoresCase(usuario.getEmail())).thenReturn(false);
        when(leitorRepository.existsByCpf(leitor.getCpf())).thenReturn(false);
        
        leitorService.criarLeitor(leitor);
     
        verify(usuarioService, times(1)).save(usuario);
        verify(leitorRepository, times(1)).save(leitor);
    }

    @Test
    void testCriarLeitor_EmailDuplicado() {
        when(usuarioRepository.existsByEmailIgnoresCase(usuario.getEmail())).thenReturn(true);

        assertThrows(DuplicateKeyException.class, () -> leitorService.criarLeitor(leitor));
        verify(usuarioService, never()).save(any(Usuario.class));
        verify(leitorRepository, never()).save(any(Leitor.class));
    }

    @Test
    void testCriarLeitor_CpfDuplicado() {
        when(usuarioRepository.existsByEmailIgnoresCase(usuario.getEmail())).thenReturn(false);
        when(leitorRepository.existsByCpf(leitor.getCpf())).thenReturn(true);

        assertThrows(DuplicateKeyException.class, () -> leitorService.criarLeitor(leitor));
        verify(usuarioService, never()).save(any(Usuario.class));
        verify(leitorRepository, never()).save(any(Leitor.class));
    }

    @Test
    void testAtualizarLeitor_Success() {
        Usuario usuarioAtualizado = new Usuario("novoemail@teste.com", "Senha@123", TipoUsuario.LEITOR);
        Leitor leitorAtualizado = new Leitor("João da Silva Maria", "88991471086", "94182774094", usuarioAtualizado);
        
        when(leitorRepository.findById(leitor.getId())).thenReturn(Optional.of(leitor));
        when(usuarioRepository.existsByEmailIgnoresCase(usuarioAtualizado.getEmail())).thenReturn(false);
        when(leitorRepository.save(leitor)).thenReturn(leitor);

        Leitor result = leitorService.atualizarLeitor(leitor.getId(), leitorAtualizado);

        assertNotNull(result);
        assertEquals(leitorAtualizado.getNomeCompleto(), result.getNomeCompleto());
        assertEquals(leitorAtualizado.getTelefone(), result.getTelefone());
        assertEquals(leitorAtualizado.getUsuario().getEmail(), result.getUsuario().getEmail());

        verify(leitorRepository, times(1)).save(leitor);
    }

    @Test
    void testAtualizarLeitor_EmailDuplicado() {
        Usuario usuarioAtualizado = new Usuario("novoemail@teste.com", "Senha@123", TipoUsuario.LEITOR);
        Leitor leitorAtualizado = new Leitor("João da Silva Maria", "88991471086", "94182774094", usuarioAtualizado);

        when(leitorRepository.findById(leitor.getId())).thenReturn(Optional.of(leitor));
        when(usuarioRepository.existsByEmailIgnoresCase(usuarioAtualizado.getEmail())).thenReturn(true);

        assertThrows(DuplicateKeyException.class,
                () -> leitorService.atualizarLeitor(leitor.getId(), leitorAtualizado));
        verify(leitorRepository, never()).save(any(Leitor.class));
    }

    @Test
    void testExcluirLeitor_Success() {
        when(leitorRepository.findById(leitor.getId())).thenReturn(Optional.of(leitor));

        leitorService.excluirLeitor(leitor.getId());

        verify(leitorRepository, times(1)).delete(leitor);
    }

    @Test
    void testExcluirLeitor_ComEmprestimos() {
        leitor.setEmprestimos(List.of(new Emprestimo()));
        when(leitorRepository.findById(leitor.getId())).thenReturn(Optional.of(leitor));

        assertThrows(DataIntegrityViolationException.class, () -> leitorService.excluirLeitor(leitor.getId()));
        verify(leitorRepository, never()).delete(any(Leitor.class));
    }

    @Test
    void testExcluirLeitor_ComReservas() {
        leitor.setReservas(List.of(new Reserva()));
        when(leitorRepository.findById(leitor.getId())).thenReturn(Optional.of(leitor));

        assertThrows(DataIntegrityViolationException.class, () -> leitorService.excluirLeitor(leitor.getId()));
        verify(leitorRepository, never()).delete(any(Leitor.class));
    }

    @Test
    void testAtualizarLeitorPorId_NotFound() {
        when(leitorRepository.findById(2)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> leitorService.atualizarLeitor(2, leitor));
    }

    @Test
    void testDeletareitorPorId_NotFound() {
        when(leitorRepository.findById(2)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> leitorService.excluirLeitor(2));
    }
}