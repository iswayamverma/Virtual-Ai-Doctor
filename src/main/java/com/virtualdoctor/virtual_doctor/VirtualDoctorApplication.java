package com.virtualdoctor.virtual_doctor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VirtualDoctorApplication {

	public static void main(String[] args) {
		SpringApplication.run(VirtualDoctorApplication.class, args);
	}
}
