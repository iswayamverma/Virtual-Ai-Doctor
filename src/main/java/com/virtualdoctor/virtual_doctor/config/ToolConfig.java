package com.virtualdoctor.virtual_doctor.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.virtualdoctor.virtual_doctor.tool.PatientTools;
import com.virtualdoctor.virtual_doctor.tool.SeverityTools;

@Configuration
public class ToolConfig {

    @Bean
    public ToolCallbackProvider consultationTools(PatientTools patientTools, SeverityTools severityTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(patientTools, severityTools)
                .build();
    }
}