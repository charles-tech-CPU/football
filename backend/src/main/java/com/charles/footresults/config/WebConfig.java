package com.charles.footresults.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Autorise le serveur de dev Vite (http://localhost:5174) a appeler l'API
 * pendant le developpement. Port different de celui du projet LoL (5173)
 * pour pouvoir faire tourner les deux projets en parallele si besoin.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5174")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
