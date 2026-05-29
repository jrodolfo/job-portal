package net.jrodolfo.jobportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Job Portal Spring Boot application.
 */
@SpringBootApplication
public class JobportalApplication {

    /**
     * Starts the Job Portal application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(JobportalApplication.class, args);
    }

}
