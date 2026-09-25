package com.backend.bilanko.repository.object.document;

import com.backend.bilanko.models.object.document.Document;
import com.backend.bilanko.models.object.document.TypeDocument;
import com.backend.bilanko.models.person.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByUserOrderByDateDeGenerationDesc(User user);

    List<Document> findByUserAndTypeOrderByDateDeGenerationDesc(User user, TypeDocument type);

    Optional<Document> findByIdAndUser(long id, User user);

    long countByType(TypeDocument type);

    @Query("SELECT COUNT(DISTINCT d.user.id) FROM Document d")
    long countDistinctUsers();

    @Query("SELECT d FROM Document d WHERE " +
            "(:keyword IS NULL OR LOWER(d.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.objet) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.user.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(d.user.subname) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:type IS NULL OR d.type = :type)")
    Page<Document> adminSearchDocuments(
            @Param("keyword") String keyword,
            @Param("type") TypeDocument type,
            Pageable pageable);
}
