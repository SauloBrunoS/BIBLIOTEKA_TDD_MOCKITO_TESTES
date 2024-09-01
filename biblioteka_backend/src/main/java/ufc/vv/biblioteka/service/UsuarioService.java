package ufc.vv.biblioteka.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import ufc.vv.biblioteka.model.Usuario;
import ufc.vv.biblioteka.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario save(Usuario usuario) {
        String hashedPassword = passwordEncoder.encode(usuario.getSenha());
        usuario.setSenha(hashedPassword);
        return usuarioRepository.save(usuario);
    }

    public boolean verificarSenha(int idUsuario, String rawPassword) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        return passwordEncoder.matches(rawPassword, usuario.getSenha());
    }
}