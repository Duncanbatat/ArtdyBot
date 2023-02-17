package ru.artdy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.artdy.entity.AppDocument;

@Repository
public interface AppDocumentRepository extends JpaRepository<AppDocument, Long> {
}
