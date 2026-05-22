package com.greenhouse.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Greenhouse Management System application.
 *
 * <p>This Spring Boot application provides a REST API for managing greenhouses,
 * zones, sensors, actuators, automation rules, and alerts.</p>
 *
 * @author Greenhouse Team
 * @version 1.0.0
 */
@SpringBootApplication
public class GreenhouseApplication {

    /**
     * Main method that bootstraps the Spring Boot application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(GreenhouseApplication.class, args);
    }
}
