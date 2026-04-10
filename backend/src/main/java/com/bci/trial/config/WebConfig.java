package com.bci.trial.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

/**
 * General MVC configuration.
 *
 * <p>Currently only enables JSON pretty-printing in development for easier
 * manual testing. In production the {@code application-prod.yml} profile
 * can disable this for performance if needed.
 *
 * <p>Additional configuration (security, message converters, interceptors)
 * can be added here as the application grows.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // Spring Boot auto-configures Jackson, MVC, and Tomcat with sensible
    // defaults via spring-boot-starter-web. Only override here when needed.
}
