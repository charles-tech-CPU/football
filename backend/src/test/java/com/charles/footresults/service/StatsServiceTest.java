package com.charles.footresults.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.CompetitionStatsDto;
import com.charles.footresults.dto.CompetitionStatsDto.TeamValue;
import com.charles.footresults.dto.MatchDto;
import com.charles.footresults.repository.CompetitionRepository;
import com.charles.footresults.repository.MatchRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private StatsService statsService;

    private Competition ligue1;
    private Team psg;
    private Team om;
    private Team lens;
    private long nextMatchId = 1;

    @BeforeEach
    void setUp() {
        ligue1 = new Competition("FRANCE", "France - Championnat 2027", CompetitionType.LEAGUE, "France", 2027);
        ligue1.setId(10L);
        psg = team(1L, "PSG");
        om = team(2L, "OM");
        lens = team(3L, "Lens");
    }

    @Test
    void totauxEtRepartitionDesResultats() {
        givenMatches(
                played(1, psg, om, 3, 0),
                played(2, om, lens, 1, 1),
                played(3, lens, psg, 2, 1),
                upcoming(4, psg, lens));

        CompetitionStatsDto stats = statsService.competitionStats(10L);

        assertThat(stats.matchesPlayed()).isEqualTo(3);
        assertThat(stats.goals()).isEqualTo(8);
        assertThat(stats.homeWins()).isEqualTo(2);
        assertThat(stats.draws()).isEqualTo(1);
        assertThat(stats.awayWins()).isZero();
    }

    @Test
    void meilleuresAttaquesEtDefenses() {
        givenMatches(played(1, psg, om, 3, 0), played(2, om, lens, 1, 1), played(3, lens, psg, 2, 1));

        CompetitionStatsDto stats = statsService.competitionStats(10L);

        // PSG 4 buts, Lens 3, OM 1 ; encaisses : PSG 2, Lens 2, OM 4.
        assertThat(stats.bestAttacks()).extracting(TeamValue::teamName).containsExactly("PSG", "Lens", "OM");
        assertThat(stats.bestAttacks()).extracting(TeamValue::value).containsExactly(4, 3, 1);
        assertThat(stats.bestDefenses()).extracting(TeamValue::value).containsExactly(2, 2, 4);
        assertThat(stats.bestDefenses().get(2).teamName()).isEqualTo("OM");
    }

    @Test
    void plusLargesVictoiresEtMatchsLesPlusProlifiques() {
        Match large = played(1, psg, om, 4, 0);
        Match prolifique = played(2, om, lens, 3, 3);
        Match serre = played(3, lens, psg, 1, 0);
        givenMatches(large, prolifique, serre);

        CompetitionStatsDto stats = statsService.competitionStats(10L);

        assertThat(stats.biggestWins()).extracting(MatchDto::id).startsWith(large.getId());
        assertThat(stats.highestScoring()).extracting(MatchDto::id).startsWith(prolifique.getId(), large.getId());
    }

    @Test
    void seriesEnCoursCompteesDepuisLeDernierMatch() {
        givenMatches(
                played(1, psg, om, 0, 1), // PSG L, OM W
                played(2, psg, lens, 2, 0), // PSG W, Lens L
                played(3, om, psg, 1, 1), // OM D, PSG D
                played(4, lens, psg, 0, 3), // Lens L, PSG W
                played(5, om, lens, 0, 0)); // OM D, Lens D

        CompetitionStatsDto stats = statsService.competitionStats(10L);

        // PSG : L W D W -> 3 sans defaite, 1 victoire ; OM : W D D -> 3 sans defaite, 0 victoire.
        assertThat(stats.unbeatenRuns()).extracting(TeamValue::teamName).containsExactly("PSG", "OM", "Lens");
        assertThat(stats.unbeatenRuns()).extracting(TeamValue::value).containsExactly(3, 3, 1);
        assertThat(stats.winRuns()).extracting(TeamValue::teamName).containsExactly("PSG");
        // Sans victoire : Lens L L D -> 3 ; OM D D -> 2 ; PSG vient de gagner.
        assertThat(stats.winlessRuns()).extracting(TeamValue::teamName).containsExactly("Lens", "OM");
        assertThat(stats.winlessRuns()).extracting(TeamValue::value).containsExactly(3, 2);
    }

    @Test
    void competitionInconnue() {
        when(competitionRepository.existsById(404L)).thenReturn(false);

        assertThatThrownBy(() -> statsService.competitionStats(404L)).isInstanceOf(EntityNotFoundException.class);
    }

    private void givenMatches(Match... matches) {
        when(competitionRepository.existsById(10L)).thenReturn(true);
        when(matchRepository.findByCompetitionIdOrderByDateAscTimeAsc(10L)).thenReturn(List.of(matches));
    }

    private Match played(int day, Team team1, Team team2, int score1, int score2) {
        Match m = upcoming(day, team1, team2);
        m.setScore1(score1);
        m.setScore2(score2);
        return m;
    }

    private Match upcoming(int day, Team team1, Team team2) {
        Match m = new Match();
        m.setId(nextMatchId++);
        m.setCompetition(ligue1);
        m.setRoundLabel("J" + day);
        m.setDate(LocalDate.of(2026, 9, day));
        m.setTeam1(team1);
        m.setTeam2(team2);
        return m;
    }

    private static Team team(Long id, String name) {
        Team team = new Team(name, "France");
        team.setId(id);
        return team;
    }
}
