package com.fintrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Personal Finance Manager application.
 * Bootstraps the Spring Boot context and starts the embedded server.
 */
@SpringBootApplication
public class FinanceManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceManagerApplication.class, args);
    }
}
