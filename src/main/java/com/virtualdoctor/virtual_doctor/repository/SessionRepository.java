package com.virtualdoctor.virtual_doctor.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.virtualdoctor.virtual_doctor.model.Session;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Session> findByEndedFalseAndUpdatedAtBefore(LocalDateTime cutoff);
}