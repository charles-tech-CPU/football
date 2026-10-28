package com.charles.footresults.dto;

import java.util.List;

/** Palmares d'un pays (ou de l'Europe) : une entree par competition, saisons les plus recentes d'abord. */
public record PalmaresDto(String scope, List<CompetitionHonours> competitions) {

    public record CompetitionHonours(String kind, String label, List<SeasonHonour> seasons, List<TitleCount> titles) {}

    public record SeasonHonour(String season, Club winner, Club runnerUp, Club third) {}

    /**
     * Club du palmares : nom Wikipedia, club de l'appli quand il existe (teamId / logo, sinon null) et
     * pays (coupes d'Europe ; null pour le palmares d'un pays).
     */
    public record Club(String name, Long teamId, String logoPath, String country) {}

    /** Nombre de titres par club, du plus titre au moins titre (a egalite, le plus recent d'abord). */
    public record TitleCount(Club team, int titles, String lastSeason) {}
}
