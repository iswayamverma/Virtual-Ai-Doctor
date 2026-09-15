package com.virtualdoctor.virtual_doctor.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 500)
    private String diagnosis;

    @Column
    private String severity;

    @Column(columnDefinition = "TEXT")
    private String summary; // ✅ ADDED - AI-generated full-session summary, filled in when session ends

    @Column
    private Boolean ended = false; // ✅ ADDED

    @Column(name = "ended_at")
    private LocalDateTime endedAt; // ✅ ADDED

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now(); // ✅ ADDED - bumped on every new message, used for auto-timeout
}