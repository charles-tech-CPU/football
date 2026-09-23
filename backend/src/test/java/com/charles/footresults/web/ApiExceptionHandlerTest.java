package com.charles.footresults.web;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityNotFoundException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void entiteIntrouvableDonneUne404AvecLeMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleNotFound(new EntityNotFoundException("Equipe introuvable : 9"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("error", "Equipe introuvable : 9");
    }

    @Test
    void requeteInvalideDonneUne400AvecLeMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleBadRequest(new IllegalArgumentException("Precise competitionId"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "Precise competitionId");
    }
}
