package com.virtualdoctor.virtual_doctor.service;

import org.springframework.stereotype.Component;

@Component
public class ConsultationContext {
    
    private static final ThreadLocal<Long> CURRENT_SESSION = new ThreadLocal<>();

    public void setSessionId(Long sessionId) { CURRENT_SESSION.set(sessionId); }
    public Long getSessionId() { return CURRENT_SESSION.get(); }
    public void clear() { CURRENT_SESSION.remove(); }
}