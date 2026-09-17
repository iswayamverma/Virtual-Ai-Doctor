package com.virtualdoctor.virtual_doctor.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.virtualdoctor.virtual_doctor.model.User;
import com.virtualdoctor.virtual_doctor.repository.UserRepository;

@Component
public class PatientTools {

    private final UserRepository userRepository;

    public PatientTools(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Tool(description = "Fetches the current patient's profile: age, blood group, known allergies, and medical history. " +
            "Call this only when you need specific patient context to assess symptoms or suggest remedies - not for every message.")
    public String getPatientProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        StringBuilder sb = new StringBuilder();
        if (user.getAge() != null) sb.append("Age: ").append(user.getAge()).append("\n");
        if (user.getBloodGroup() != null && !user.getBloodGroup().isEmpty())
            sb.append("Blood Group: ").append(user.getBloodGroup()).append("\n");
        if (user.getAllergies() != null && !user.getAllergies().isEmpty())
            sb.append("Known Allergies: ").append(user.getAllergies()).append("\n");
        if (user.getMedicalHistory() != null && !user.getMedicalHistory().isEmpty())
            sb.append("Medical History: ").append(user.getMedicalHistory()).append("\n");

        return sb.length() > 0 ? sb.toString() : "No profile details on file for this patient.";
    }
}