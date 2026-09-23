package com.charles.footresults.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

class WebConfigTest {

    /** Expose la configuration CORS enregistree (methode protegee de CorsRegistry). */
    private static final class InspectableCorsRegistry extends CorsRegistry {
        Map<String, CorsConfiguration> configurations() {
            return getCorsConfigurations();
        }
    }

    @Test
    void autoriseLeServeurDeDevViteSurLApi() {
        InspectableCorsRegistry registry = new InspectableCorsRegistry();

        new WebConfig().addCorsMappings(registry);

        CorsConfiguration api = registry.configurations().get("/api/**");
        assertThat(api.getAllowedOrigins()).containsExactly("http://localhost:5174");
        assertThat(api.getAllowedMethods()).contains("GET", "POST", "PUT", "PATCH", "DELETE");
    }
}
