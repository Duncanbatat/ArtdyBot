package ru.artdy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.artdy.entity.RawData;

@Repository
public interface RawDataRepository extends JpaRepository<RawData, Long> {
}
