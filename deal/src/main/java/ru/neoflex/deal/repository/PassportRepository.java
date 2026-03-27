package ru.neoflex.deal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.neoflex.deal.entity.Passport;

import java.util.UUID;

@Repository
public interface PassportRepository extends JpaRepository<Passport, UUID> {
}
