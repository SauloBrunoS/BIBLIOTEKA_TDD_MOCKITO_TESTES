package ufc.vv.biblioteka.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DuplicateKeyException;

import ufc.vv.biblioteka.model.Usuario;
import ufc.vv.biblioteka.repository.UsuarioRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void deveCriarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setEmail("email@teste.com");

        when(usuarioRepository.existsByEmail(any())).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario result = usuarioService.save(usuario);

        assertNotNull(result);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deveLancarExcecaoSeUsuarioJaExistir() {
        Usuario usuario = new Usuario();
        usuario.setEmail("email@teste.com");

        when(usuarioRepository.existsByEmail(any())).thenReturn(true);

        assertThrows(DuplicateKeyException.class, () -> usuarioService.save(usuario));
    }

}
