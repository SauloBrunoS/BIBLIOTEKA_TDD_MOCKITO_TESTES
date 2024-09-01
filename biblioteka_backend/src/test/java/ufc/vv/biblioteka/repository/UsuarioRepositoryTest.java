package ufc.vv.biblioteka.repository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import jakarta.transaction.Transactional;

import ufc.vv.biblioteka.model.Usuario;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
public class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;


    @Test
    public void deveVerificarSeExisteUsuarioPorEmail() {
        // Dado
        Usuario usuario = new Usuario();
        usuario.setEmail("teste@exemplo.com");
        usuarioRepository.save(usuario);

        // Quando
        boolean existe = usuarioRepository.existsByEmail("teste@exemplo.com");

        // Então
        assertTrue(existe);
    }

}

