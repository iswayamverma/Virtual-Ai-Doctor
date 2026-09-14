package com.virtualdoctor.virtual_doctor.service;

import com.virtualdoctor.virtual_doctor.model.Message;
import com.virtualdoctor.virtual_doctor.model.Session;
import com.virtualdoctor.virtual_doctor.model.User;
import com.virtualdoctor.virtual_doctor.repository.MessageRepository;
import com.virtualdoctor.virtual_doctor.repository.SessionRepository;
import com.virtualdoctor.virtual_doctor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

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
    private ReportService reportService; // ✅ ADDED

    public Session startSession(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Session session = new Session();
        session.setUser(user);
        return sessionRepository.save(session);
    }

    public String chat(Long sessionId, String userMessage, String email) {
        Session session = sessionRepository.findById(sessionId).orElseThrow();

        Message userMsg = new Message();
        userMsg.setSession(session);
        userMsg.setSender("user");
        userMsg.setContent(userMessage);
        messageRepository.save(userMsg);

        List<Message> history = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        StringBuilder conversationHistory = new StringBuilder();
        for (Message msg : history) {
            conversationHistory.append(msg.getSender())
                    .append(": ")
                    .append(msg.getContent())
                    .append("\n");
        }

        // Build user profile context
        User user = userRepository.findByEmail(email).orElseThrow();
        StringBuilder userContext = new StringBuilder();
        if (user.getAge() != null)
            userContext.append("Age: ").append(user.getAge()).append("\n");
        if (user.getBloodGroup() != null && !user.getBloodGroup().isEmpty())
            userContext.append("Blood Group: ").append(user.getBloodGroup()).append("\n");
        if (user.getAllergies() != null && !user.getAllergies().isEmpty())
            userContext.append("Known Allergies: ").append(user.getAllergies()).append("\n");
        if (user.getMedicalHistory() != null && !user.getMedicalHistory().isEmpty())
            userContext.append("Medical History: ").append(user.getMedicalHistory()).append("\n");

        String aiResponse = aiService.getAiResponse(
                userMessage,
                conversationHistory.toString(),
                userContext.toString()
        );

        Message aiMsg = new Message();
        aiMsg.setSession(session);
        aiMsg.setSender("ai");
        aiMsg.setContent(aiResponse);
        messageRepository.save(aiMsg);

        // Normalize for forgiving severity matching (handles spacing/case differences from the AI)
        String normalized = aiResponse.toUpperCase().replaceAll("\\s+", "");

        if (normalized.contains("[SEVERITY:HIGH]")) {
            session.setSeverity("HIGH");
        } else if (normalized.contains("[SEVERITY:MEDIUM]")) {
            session.setSeverity("MEDIUM");
        } else {
            session.setSeverity("LOW");
        }

        session.setDiagnosis(aiResponse.substring(0, Math.min(aiResponse.length(), 500)));
        sessionRepository.save(session);

        // ✅ HIGH severity: send both the urgent alert and the full report automatically
        if ("HIGH".equals(session.getSeverity())) {
            emailService.sendHighSeverityAlert(user.getEmail(), user.getName());

            byte[] pdfBytes = reportService.generateReportPdf(session, user);
            emailService.sendConsultationReport(user.getEmail(), user.getName(), pdfBytes);
        }

        return aiResponse;
    }

    // ✅ ADDED - for the "Send Report" button (LOW/MEDIUM severity, user-triggered)
    public void sendReportOnDemand(Long sessionId, String email) {
        Session session = sessionRepository.findById(sessionId).orElseThrow();
        User user = userRepository.findByEmail(email).orElseThrow();

        // Ensure the requesting user actually owns this session
        if (!session.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You are not authorized to access this session's report.");
        }

        byte[] pdfBytes = reportService.generateReportPdf(session, user);
        emailService.sendConsultationReport(user.getEmail(), user.getName(), pdfBytes);
    }

    public List<Session> getHistory(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return sessionRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public List<Message> getMessages(Long sessionId) {
        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }
}