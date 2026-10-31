package com.charles.footresults.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Etat de cloture d'une competition : date de cloture (null si en cours), saison proposee pour le
 * palmares et classement final fige (vide pour une coupe ou une competition en cours).
 */
public record ClosureDto(
        Long competitionId, LocalDateTime closedAt, String suggestedSeason, List<StandingRowDto> finalStandings) {}
