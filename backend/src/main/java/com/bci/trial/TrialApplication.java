package com.bci.trial;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Clinical Trial Platform Spring Boot application.
 *
 * <p>{@code @SpringBootApplication} is a meta-annotation that enables:
 * <ul>
 *   <li>{@code @Configuration} — this class is a Spring configuration source</li>
 *   <li>{@code @EnableAutoConfiguration} — activates Spring Boot's auto-config</li>
 *   <li>{@code @ComponentScan} — scans all classes under {@code com.bci.trial}</li>
 * </ul>
 *
 * <p>The embedded Tomcat server starts on port 8080 (configurable via
 * {@code server.port} in {@code application.yml} or the {@code PORT} env var).
 */
@SpringBootApplication
public class TrialApplication {
    public static void main(String[] args) {
        SpringApplication.run(TrialApplication.class, args);
    }
}
