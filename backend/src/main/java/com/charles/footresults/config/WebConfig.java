package com.charles.footresults.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Autorise le frontend Vite (port 5174, different du projet LoL sur 5173) a appeler l'API
 * quel que soit l'hote utilise pour l'ouvrir (localhost, IP du reseau local...) : le frontend
 * appelle le backend sur le meme hote que celui de la page (cf. frontend/src/services/api.js).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    }
}
