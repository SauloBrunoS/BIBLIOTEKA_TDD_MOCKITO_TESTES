package ufc.vv.biblioteka.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ufc.vv.biblioteka.model.Usuario;

@RepositoryRestResource(collectionResourceRel = "usuarios", path = "usuarios")
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    @Query("SELECT COUNT(u) > 0 FROM Usuario u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmailIgnoresCase(String email);
}
