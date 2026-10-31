package com.charles.footresults.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Cloture d'une competition : saison du palmares ("2026", "2026-2027"), classement final valide a la
 * main (championnat : identifiants des equipes du 1er au dernier) ou vainqueur / finaliste (coupe).
 */
public record ClosureRequestDto(@NotBlank String season, List<Long> ranking, Long winnerId, Long runnerUpId) {}
