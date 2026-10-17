package com.charles.footresults.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Une ligne du classement mondial des selections (Elo recalcule a la volee, cf.
 * InternationalRankingService ; jamais stocke, meme principe que StandingRowDto).
 *
 * <p>rankChange : places gagnees (positif) ou perdues depuis le classement d'il y a
 * TREND_DAYS jours, nul si l'equipe n'etait pas encore classee. form : 5 derniers resultats, du
 * plus ancien au plus recent ("W", "D", "L" ; tirs au but = nul). confederation : celle des
 * competitions continentales de l'equipe (UEFA, CAF...), dont startRating est la note de depart.
 */
public record InternationalRankingRowDto(
        int rank,
        Integer rankChange,
        Long teamId,
        String teamName,
        String country,
        String confederation,
        int startRating,
        int rating,
        int ratingChange,
        int played,
        int wins,
        int draws,
        int losses,
        int goalsFor,
        int goalsAgainst,
        List<String> form,
        BestWin bestWin,
        LocalDate lastPlayed) {

    public record BestWin(
            Long matchId,
            Long opponentId,
            String opponentName,
            int opponentRating,
            String score,
            String competitionName) {}
}
