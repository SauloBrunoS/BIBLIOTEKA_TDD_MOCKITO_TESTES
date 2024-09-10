package ufc.vv.biblioteka.repository;

import org.springframework.data.domain.Pageable;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ufc.vv.biblioteka.model.Colecao;
import java.util.Optional;

@RepositoryRestResource(collectionResourceRel = "colecoes", path = "colecoes")
public interface ColecaoRepository extends JpaRepository<Colecao, Integer> {
        @Query("SELECT c FROM Colecao c WHERE " +
                        "(:search IS NULL OR " +
                        "c.nome iLIKE '%'||:search||'%' OR " +
                        "c.descricao iLIKE '%'||:search||'%')")
        Page<Colecao> findByNomeAndDescricao(
                        @Param("search") String search,
                        Pageable pageable);

        @Query("SELECT COUNT(c) > 0 FROM Colecao c WHERE LOWER(c.nome) = LOWER(:nome)")
        boolean existsByNomeIgnoreCase(String nome);

        @Query("SELECT c FROM Colecao c WHERE c.nome ILIKE '%'||:search||'%'")
        List<Colecao> findAllWithFilter(@Param("search") String search);

        @Query("SELECT c FROM Colecao c LEFT JOIN FETCH c.livros WHERE c.id = :id")
        Optional<Colecao> findByIdWithLivros(@Param("id") Integer id);
}