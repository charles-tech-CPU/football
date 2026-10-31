package com.charles.footresults.dto;

import java.util.List;

/**
 * Bilan historique d'une competition de selections (Coupe du monde, Euro...) : bilan de chaque
 * selection en phase finale et en qualifications, et parcours en phase finale (titres, finales,
 * demi-finales...).
 */
public record InternationalStatsDto(
        String family, List<TeamRecord> finals, List<TeamRecord> qualifiers, List<Journey> journeys) {

    /** Bilan d'une selection sur toutes les editions (editions = participations). */
    public record TeamRecord(
            Long teamId,
            String teamName,
            int editions,
            int played,
            int won,
            int drawn,
            int lost,
            int goalsFor,
            int goalsAgainst,
            int points) {}

    /**
     * Parcours en phase finale : nombre d'editions terminees a chaque stade (vainqueur, finaliste,
     * demi-finale, quarts, 8es, 16es, phase de groupes ou tour preliminaire), et meilleur resultat.
     */
    public record Journey(
            Long teamId,
            String teamName,
            int editions,
            int titles,
            int runnerUps,
            int semiFinals,
            int quarterFinals,
            int roundOf16,
            int roundOf32,
            int groupStage,
            List<String> titleEditions) {}
}
