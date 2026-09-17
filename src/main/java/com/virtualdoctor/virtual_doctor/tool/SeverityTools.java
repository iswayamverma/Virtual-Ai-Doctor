package com.virtualdoctor.virtual_doctor.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.virtualdoctor.virtual_doctor.model.Session;
import com.virtualdoctor.virtual_doctor.repository.SessionRepository;
import com.virtualdoctor.virtual_doctor.service.ConsultationContext;

@Component
public class SeverityTools {

    private final SessionRepository sessionRepository;
    private final ConsultationContext consultationContext;

    public SeverityTools(SessionRepository sessionRepository, ConsultationContext consultationContext) {
        this.sessionRepository = sessionRepository;
        this.consultationContext = consultationContext;
    }

    @Tool(description = "Call this ONLY when the patient's symptoms indicate a HIGH severity / emergency condition " +
            "requiring immediate medical attention. This triggers an urgent alert to the patient. Do not call for mild or moderate symptoms.")
    public String flagHighSeverity(@ToolParam(description = "Brief clinical reason for flagging as high severity") String reason) {
        Long sessionId = consultationContext.getSessionId();
        if (sessionId == null) return "No active session - could not record flag.";

        Session session = sessionRepository.findById(sessionId).orElseThrow();
        session.setSeverity("HIGH");
        sessionRepository.save(session);

        return "High severity recorded for this consultation. Reason: " + reason;
    }
}