package ru.artdy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.artdy.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    AppUser findAppUserByTelegramUserId(Long id);
}
