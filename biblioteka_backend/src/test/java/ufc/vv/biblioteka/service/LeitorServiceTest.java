package ufc.vv.biblioteka.service;

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
import ufc.vv.biblioteka.model.Usuario;
import ufc.vv.biblioteka.repository.LeitorRepository;
import ufc.vv.biblioteka.repository.UsuarioRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.anyInt;

public class LeitorServiceTest {

    @Mock
    private LeitorRepository leitorRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private LeitorService leitorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveCriarLeitor() {
        Leitor leitor = new Leitor();
        Usuario usuario = new Usuario();
        usuario.setEmail("email@teste.com");
        leitor.setUsuario(usuario);
        leitor.setCpf("123.456.789-10");

        when(usuarioRepository.existsByEmail(any())).thenReturn(false);
        when(leitorRepository.existsByCpf(any())).thenReturn(false);
        when(leitorRepository.save(any(Leitor.class))).thenReturn(leitor);

        Leitor result = leitorService.criarLeitor(leitor);

        assertNotNull(result);
        verify(leitorRepository).save(leitor);
    }

    @Test
    void deveLancarExcecaoSeUsuarioJaExistir() {
        Leitor leitor = new Leitor();
        Usuario usuario = new Usuario();
        usuario.setEmail("email@teste.com");
        leitor.setUsuario(usuario);

        when(usuarioRepository.existsByEmail(any())).thenReturn(true);

        assertThrows(DuplicateKeyException.class, () -> leitorService.criarLeitor(leitor));
    }

    @Test
    void deveLancarExcecaoSeCpfJaExistir() {
        Leitor leitor = new Leitor();
        leitor.setCpf("123.456.789-10");

        when(leitorRepository.existsByCpf(any())).thenReturn(true);

        assertThrows(DuplicateKeyException.class, () -> leitorService.criarLeitor(leitor));
    }

    @Test
    void deveAtualizarLeitor() {
        Leitor existingLeitor = new Leitor();
        existingLeitor.setId(1);
        existingLeitor.setNomeCompleto("Nome Antigo");

        when(leitorRepository.findById(anyInt())).thenReturn(java.util.Optional.of(existingLeitor));
        when(leitorRepository.save(any(Leitor.class))).thenReturn(existingLeitor);

        Leitor updatedLeitor = new Leitor();
        updatedLeitor.setNomeCompleto("Nome Atualizado");

        Leitor result = leitorService.atualizarLeitor(1, updatedLeitor);

        assertEquals("Nome Atualizado", result.getNomeCompleto());
        verify(leitorRepository).save(existingLeitor);
    }

    @Test
    void deveExcluirLeitor() {
        Leitor leitor = new Leitor();
        leitor.setId(1);

        when(leitorRepository.findById(anyInt())).thenReturn(java.util.Optional.of(leitor));

        leitorService.excluirLeitor(1);

        verify(leitorRepository).delete(leitor);
    }
}