package com.hospital.hms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Hospital Management System.
 *
 * This is a layered Spring Boot MVC application:
 * Controller -> Service -> Repository -> Entity -> Database
 */
@SpringBootApplication
public class HospitalManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(HospitalManagementApplication.class, args);
    }

}
