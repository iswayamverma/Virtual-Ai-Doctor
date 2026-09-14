package com.virtualdoctor.virtual_doctor.controller;

import com.virtualdoctor.virtual_doctor.model.Message;
import com.virtualdoctor.virtual_doctor.model.Session;
import com.virtualdoctor.virtual_doctor.service.ConsultationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/consultation")
@CrossOrigin(origins = "*")
public class ConsultationController {

    @Autowired
    private ConsultationService consultationService;

    @PostMapping("/start")
    public ResponseEntity<Session> startSession(Authentication authentication) {
        String email = authentication.getName();
        Session session = consultationService.startSession(email);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/chat/{sessionId}")
    public ResponseEntity<String> chat(
            @PathVariable Long sessionId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        String email = authentication.getName();
        String message = request.get("message");
        String response = consultationService.chat(sessionId, message, email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<Session>> getHistory(Authentication authentication) {
        String email = authentication.getName();
        List<Session> history = consultationService.getHistory(email);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/messages/{sessionId}")
    public ResponseEntity<List<Message>> getMessages(@PathVariable Long sessionId) {
        List<Message> messages = consultationService.getMessages(sessionId);
        return ResponseEntity.ok(messages);
    }

    // ✅ ADDED - for the "Send Report" button in the UI (LOW/MEDIUM severity sessions)
    @PostMapping("/report/{sessionId}")
    public ResponseEntity<Map<String, String>> sendReport(
            @PathVariable Long sessionId,
            Authentication authentication) {
        try {
            String email = authentication.getName();
            consultationService.sendReportOnDemand(sessionId, email);
            return ResponseEntity.ok(Map.of("message", "Report sent successfully to your email."));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Failed to send report: " + e.getMessage()));
        }
    }
}