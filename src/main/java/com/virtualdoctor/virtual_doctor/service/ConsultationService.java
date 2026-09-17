package com.virtualdoctor.virtual_doctor.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.virtualdoctor.virtual_doctor.model.Message;
import com.virtualdoctor.virtual_doctor.model.Session;
import com.virtualdoctor.virtual_doctor.model.User;
import com.virtualdoctor.virtual_doctor.repository.MessageRepository;
import com.virtualdoctor.virtual_doctor.repository.SessionRepository;
import com.virtualdoctor.virtual_doctor.repository.UserRepository;

@Service
public class ConsultationService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AiService aiService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private ConsultationContext consultationContext;

    public Session startSession(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Session session = new Session();
        session.setUser(user);
        session.setSeverity("LOW");
        session.setEnded(false);
        return sessionRepository.save(session);
    }

    public String chat(Long sessionId, String userMessage, String email) {
    Session session = sessionRepository.findById(sessionId).orElseThrow();

    if (Boolean.TRUE.equals(session.getEnded())) {
        throw new IllegalStateException("This consultation has already ended. Please start a new session.");
    }

    Message userMsg = new Message();
    userMsg.setSession(session);
    userMsg.setSender("user");
    userMsg.setContent(userMessage);
    messageRepository.save(userMsg);

    consultationContext.setSessionId(sessionId);
    String aiResponse;
    try {
        aiResponse = aiService.chat(sessionId, userMessage);
    } finally {
        consultationContext.clear();
    }

    Message aiMsg = new Message();
    aiMsg.setSession(session);
    aiMsg.setSender("ai");
    aiMsg.setContent(aiResponse);
    messageRepository.save(aiMsg);

    // Re-fetch: a flagHighSeverity tool call may have updated severity mid-call.
    // Saving the pre-call `session` object here would silently clobber that.
    session = sessionRepository.findById(sessionId).orElseThrow();
    session.setDiagnosis(aiResponse.substring(0, Math.min(aiResponse.length(), 500)));
    session.setUpdatedAt(LocalDateTime.now());
    sessionRepository.save(session);

    return aiResponse;
}
    // ✅ ADDED - manual "End Consultation" button calls this
    public void endSession(Long sessionId, String email) {
        Session session = sessionRepository.findById(sessionId).orElseThrow();
        User user = userRepository.findByEmail(email).orElseThrow();

        if (!session.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You are not authorized to end this session.");
        }

        processSessionEnd(session);
    }

    // ✅ ADDED - called internally by the scheduled auto-timeout job, no ownership check needed
    public void autoEndSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId).orElseThrow();
        processSessionEnd(session);
    }

    private void processSessionEnd(Session session) {
        if (Boolean.TRUE.equals(session.getEnded())) {
            return; // already ended, nothing to do
        }

        User user = session.getUser();
        List<Message> history = messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId());

        if (history.isEmpty()) {
            // Nothing was discussed - just mark ended, skip AI summary and email
            session.setEnded(true);
            session.setEndedAt(LocalDateTime.now());
            sessionRepository.save(session);
            return;
        }

        StringBuilder transcript = new StringBuilder();
        for (Message msg : history) {
            transcript.append(msg.getSender()).append(": ").append(msg.getContent()).append("\n");
        }

        StringBuilder userContext = new StringBuilder();
        if (user.getAge() != null)
            userContext.append("Age: ").append(user.getAge()).append("\n");
        if (user.getBloodGroup() != null && !user.getBloodGroup().isEmpty())
            userContext.append("Blood Group: ").append(user.getBloodGroup()).append("\n");
        if (user.getAllergies() != null && !user.getAllergies().isEmpty())
            userContext.append("Known Allergies: ").append(user.getAllergies()).append("\n");
        if (user.getMedicalHistory() != null && !user.getMedicalHistory().isEmpty())
            userContext.append("Medical History: ").append(user.getMedicalHistory()).append("\n");

        String summary = aiService.summarizeConversation(transcript.toString(), userContext.toString());

        session.setSummary(summary);
        session.setEnded(true);
        session.setEndedAt(LocalDateTime.now());
        sessionRepository.save(session);

        // ✅ HIGH severity sessions auto-send both emails; others wait for the button
        if ("HIGH".equals(session.getSeverity())) {
            emailService.sendHighSeverityAlert(user.getEmail(), user.getName());

            byte[] pdfBytes = reportService.generateReportPdf(session, user, summary);
            emailService.sendConsultationReport(user.getEmail(), user.getName(), pdfBytes);
        }
    }

    // ✅ ADDED - for the "Send Report" button (works once a session has ended, any severity)
    public void sendReportOnDemand(Long sessionId, String email) {
        Session session = sessionRepository.findById(sessionId).orElseThrow();
        User user = userRepository.findByEmail(email).orElseThrow();

        if (!session.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You are not authorized to access this session's report.");
        }

        if (!Boolean.TRUE.equals(session.getEnded())) {
            throw new IllegalStateException("This consultation hasn't ended yet. Please end the session first.");
        }

        byte[] pdfBytes = reportService.generateReportPdf(session, user, session.getSummary());
        emailService.sendConsultationReport(user.getEmail(), user.getName(), pdfBytes);
    }

    private int severityRank(String severity) {
        if (severity == null) return 0;
        switch (severity.toUpperCase()) {
            case "HIGH": return 3;
            case "MEDIUM": return 2;
            case "LOW": return 1;
            default: return 0;
        }
    }

    public List<Session> getHistory(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return sessionRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public List<Message> getMessages(Long sessionId) {
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }
}