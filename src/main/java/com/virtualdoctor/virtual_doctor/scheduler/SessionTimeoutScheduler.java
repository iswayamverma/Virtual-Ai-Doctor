package com.virtualdoctor.virtual_doctor.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.virtualdoctor.virtual_doctor.model.Session;
import com.virtualdoctor.virtual_doctor.repository.SessionRepository;
import com.virtualdoctor.virtual_doctor.service.ConsultationService;

@Component
public class SessionTimeoutScheduler {

    private static final int INACTIVITY_TIMEOUT_MINUTES = 15;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private ConsultationService consultationService;

    // Runs every 5 minutes
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void closeInactiveSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(INACTIVITY_TIMEOUT_MINUTES);
        List<Session> staleSessions = sessionRepository.findByEndedFalseAndUpdatedAtBefore(cutoff);

        for (Session session : staleSessions) {
            try {
                consultationService.autoEndSession(session.getId());
                System.out.println("Auto-ended inactive session: " + session.getId());
            } catch (Exception e) {
                System.out.println("Failed to auto-end session " + session.getId() + ": " + e.getMessage());
            }
        }
    }
}