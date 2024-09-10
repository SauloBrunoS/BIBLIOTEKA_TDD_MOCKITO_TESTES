package ufc.vv.biblioteka.services;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import jakarta.persistence.EntityNotFoundException;
import ufc.vv.biblioteka.model.TipoUsuario;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import ufc.vv.biblioteka.model.Usuario;
import ufc.vv.biblioteka.repository.UsuarioRepository;
import ufc.vv.biblioteka.service.UsuarioService;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(); 
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);
    }

    @Test
    void testSaveUsuario_Success() {
        Usuario usuario = new Usuario("email@teste.com", "Senha@123", TipoUsuario.LEITOR);
       
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Usuario savedUsuario = usuarioService.save(usuario);

        assertNotNull(savedUsuario);
        assertEquals("email@teste.com", savedUsuario.getEmail());
        assertTrue(passwordEncoder.matches("Senha@123", savedUsuario.getSenha()));

        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    void testVerificarSenha_Success() {
        Usuario usuario = new Usuario("email@teste.com", passwordEncoder.encode("Senha@123"), TipoUsuario.LEITOR);
        usuario.setId(1);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));

        boolean senhaValida = usuarioService.verificarSenha(1, "Senha@123");

        assertTrue(senhaValida);
        verify(usuarioRepository, times(1)).findById(1);
    }

    @Test
    void testVerificarSenha_UsuarioNotFound() {
        when(usuarioRepository.findById(2)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> usuarioService.verificarSenha(2, "Senha@123"));
        verify(usuarioRepository, times(1)).findById(2);
    }

    @Test
    void testVerificarSenha_InvalidSenha() {
        Usuario usuario = new Usuario("email@teste.com", passwordEncoder.encode("Senha@123"), TipoUsuario.LEITOR);
        usuario.setId(1);

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));

        boolean senhaValida = usuarioService.verificarSenha(1, "Senha@1234");

        assertFalse(senhaValida);
        verify(usuarioRepository, times(1)).findById(1);
    }
}
