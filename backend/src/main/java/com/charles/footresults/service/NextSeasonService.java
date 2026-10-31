package com.charles.footresults.service;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.Team;
import com.charles.footresults.domain.TeamCompetitionStatus;
import com.charles.footresults.dto.CompetitionDto;
import com.charles.footresults.dto.NextSeasonRequestDto;
import com.charles.footresults.repository.CompetitionRepository;
import com.charles.footresults.repository.TeamCompetitionStatusRepository;
import com.charles.footresults.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Prepare la saison suivante d'un championnat cloture : nouveau championnat (meme code, memes
 * places qualificatives et nombre de journees, modifiables ensuite), clubs engages inscrits avec
 * leurs statuts (promu, tenant du titre, vainqueur de la coupe) et, au besoin, coupe nationale de
 * la meme saison, vide. Le calendrier et le tableau de coupe se saisissent ensuite a la main.
 */
@Service
@Transactional
public class NextSeasonService {

    private final CompetitionRepository competitionRepository;
    private final TeamRepository teamRepository;
    private final TeamCompetitionStatusRepository statusRepository;

    public NextSeasonService(
            CompetitionRepository competitionRepository,
            TeamRepository teamRepository,
            TeamCompetitionStatusRepository statusRepository) {
        this.competitionRepository = competitionRepository;
        this.teamRepository = teamRepository;
        this.statusRepository = statusRepository;
    }

    public CompetitionDto createNextSeason(Long leagueId, NextSeasonRequestDto dto) {
        Competition league = competitionRepository
                .findById(leagueId)
                .orElseThrow(() -> new EntityNotFoundException("Competition introuvable : " + leagueId));
        if (league.getType() != CompetitionType.LEAGUE || !league.isClosed()) {
            throw new IllegalArgumentException("Seul un championnat cloture prepare sa saison suivante");
        }
        int next = league.getSeason() + 1;
        String country = league.getCountry();
        if (competitionRepository
                .findFirstByTypeAndCountryAndSeason(CompetitionType.LEAGUE, country, next)
                .isPresent()) {
            throw new IllegalArgumentException("Le championnat " + next + " existe deja");
        }
        if (new HashSet<>(dto.teamIds()).size() != dto.teamIds().size()) {
            throw new IllegalArgumentException("Un club est engage deux fois");
        }
        Set<Long> promoted = dto.promotedIds() == null ? Set.of() : new HashSet<>(dto.promotedIds());

        Competition nextLeague = nextOf(league);
        nextLeague.setLdcSlots(league.getLdcSlots());
        nextLeague.setElSlots(league.getElSlots());
        nextLeague.setEclSlots(league.getEclSlots());
        nextLeague.setRelegationSlots(league.getRelegationSlots());
        nextLeague.setBarrageSlots(league.getBarrageSlots());
        nextLeague.setTotalRounds(league.getTotalRounds());
        competitionRepository.save(nextLeague);

        Competition cup = competitionRepository
                .findFirstByTypeAndCountryAndSeason(CompetitionType.DOMESTIC_CUP, country, league.getSeason())
                .orElse(null);
        Team cupWinner = cup != null && cup.isClosed() ? cup.getWinnerTeam() : null;
        for (Long teamId : dto.teamIds()) {
            Team team = teamRepository
                    .findById(teamId)
                    .orElseThrow(() -> new EntityNotFoundException("Equipe introuvable : " + teamId));
            TeamCompetitionStatus status = new TeamCompetitionStatus(nextLeague, team);
            status.setPromoted(promoted.contains(teamId));
            status.setDefendingChampion(sameTeam(league.getWinnerTeam(), team));
            status.setPreviousCupWinner(sameTeam(cupWinner, team));
            statusRepository.save(status);
        }

        boolean cupMissing = competitionRepository
                .findFirstByTypeAndCountryAndSeason(CompetitionType.DOMESTIC_CUP, country, next)
                .isEmpty();
        if (dto.withCup() && cup != null && cupMissing) {
            competitionRepository.save(nextOf(cup));
        }
        return CompetitionDto.from(nextLeague);
    }

    /** Meme competition la saison suivante : meme code, saison du nom decalee ("... 2027" -> "... 2028"). */
    static Competition nextOf(Competition c) {
        int next = c.getSeason() + 1;
        String season = String.valueOf(c.getSeason());
        String name = c.getName().contains(season)
                ? c.getName().replace(season, String.valueOf(next))
                : c.getName() + " " + next;
        return new Competition(c.getCode(), name, c.getType(), c.getCountry(), next);
    }

    private static boolean sameTeam(Team a, Team b) {
        return a != null && Objects.equals(a.getId(), b.getId());
    }
}
