package com.example.jobs.temps;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.jobs.temps.entities.Temp;

public interface TempRepository extends JpaRepository<Temp, Long> {
    Optional<Temp> findByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}