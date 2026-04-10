package com.bci.trial.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

/**
 * CORS configuration — controls which origins can call the API.
 *
 * <p>{@code app.cors.allowed-origins} is read from {@code application.yml}, which
 * in turn reads the {@code FRONTEND_URL} environment variable set in the
 * Render dashboard. In local development it defaults to {@code http://localhost:5173}
 * (the default Vite dev server port).
 *
 * <p>Only {@code /api/**} paths are CORS-enabled — the H2 console and
 * actuator endpoints are not exposed to cross-origin requests.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Injected from {@code app.cors.allowed-origins} in application.yml.
     * In production: the Netlify frontend URL.
     * In development: http://localhost:5173.
     */
    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            // Only allow the known frontend — never use "*" in production
            .allowedOrigins(allowedOrigins)
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            // Cache the pre-flight response for 1 hour to reduce OPTIONS calls
            .maxAge(3600);
    }
}
