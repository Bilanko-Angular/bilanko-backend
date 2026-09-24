package com.backend.bilanko.repository.document;

import com.backend.bilanko.models.object.document.Document;
import com.backend.bilanko.models.object.document.TypeDocument;
import com.backend.bilanko.models.person.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByUserOrderByDateDeGenerationDesc(User user);

    List<Document> findByUserAndTypeOrderByDateDeGenerationDesc(User user, TypeDocument type);

    Optional<Document> findByIdAndUser(long id, User user);
}
