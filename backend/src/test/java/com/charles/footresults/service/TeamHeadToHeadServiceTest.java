package com.charles.footresults.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.MatchDto;
import com.charles.footresults.dto.TeamHeadToHeadDto;
import com.charles.footresults.repository.MatchRepository;
import com.charles.footresults.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeamHeadToHeadServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private TeamHeadToHeadService service;

    private Team psg;
    private Team om;
    private Competition ligue1;
    private Competition coupe;
    private long nextMatchId = 1;

    @BeforeEach
    void setUp() {
        psg = team(1L, "PSG");
        om = team(2L, "OM");
        ligue1 = competition(10L, CompetitionType.LEAGUE);
        coupe = competition(11L, CompetitionType.DOMESTIC_CUP);
        lenient().when(teamRepository.findById(1L)).thenReturn(Optional.of(psg));
        lenient().when(teamRepository.findById(2L)).thenReturn(Optional.of(om));
    }

    @Test
    void bilanDuPointDeVueDeLaPremiereEquipeQuelQueSoitLeTerrain() {
        givenMatches(
                played(ligue1, date(1), psg, om, 3, 0),
                played(ligue1, date(10), om, psg, 2, 1),
                played(coupe, date(15), om, psg, 1, 1),
                played(ligue1, date(20), om, psg, 0, 4));

        TeamHeadToHeadDto h2h = service.headToHead(1L, 2L);

        assertThat(h2h.played()).isEqualTo(4);
        assertThat(h2h.wins()).isEqualTo(2);
        assertThat(h2h.draws()).isEqualTo(1);
        assertThat(h2h.losses()).isEqualTo(1);
        assertThat(h2h.goalsFor()).isEqualTo(9);
        assertThat(h2h.goalsAgainst()).isEqualTo(3);
        // 4-0 a l'exterieur : plus large ecart que le 3-0 a domicile
        assertThat(h2h.biggestWin().date()).isEqualTo(date(20));
        assertThat(h2h.biggestLoss().date()).isEqualTo(date(10));
        assertThat(h2h.playedMatches())
                .extracting(MatchDto::date)
                .containsExactly(date(20), date(15), date(10), date(1));
    }

    @Test
    void vuDeLAutreEquipeLeBilanEstInverse() {
        List<Match> matches = List.of(played(ligue1, date(1), psg, om, 3, 0), played(ligue1, date(10), om, psg, 2, 1));
        when(matchRepository.findBetween(2L, 1L)).thenReturn(matches);

        TeamHeadToHeadDto h2h = service.headToHead(2L, 1L);

        assertThat(h2h.team().name()).isEqualTo("OM");
        assertThat(h2h.wins()).isEqualTo(1);
        assertThat(h2h.losses()).isEqualTo(1);
        assertThat(h2h.goalsFor()).isEqualTo(2);
        assertThat(h2h.goalsAgainst()).isEqualTo(4);
    }

    @Test
    void lesTirsAuButRestentUnNulMaisSontComptesAPart() {
        Match finale = played(coupe, date(5), om, psg, 1, 1);
        finale.setPenaltyScore1(3);
        finale.setPenaltyScore2(4);
        givenMatches(finale);

        TeamHeadToHeadDto h2h = service.headToHead(1L, 2L);

        assertThat(h2h.draws()).isEqualTo(1);
        assertThat(h2h.wins()).isZero();
        assertThat(h2h.penaltyWins()).isEqualTo(1);
        assertThat(h2h.penaltyLosses()).isZero();
        assertThat(h2h.biggestWin()).isNull();
    }

    @Test
    void bilanParCompetitionEtMatchsAVenirSepares() {
        givenMatches(
                played(ligue1, date(1), psg, om, 1, 0),
                played(coupe, date(8), psg, om, 0, 2),
                played(ligue1, date(12), om, psg, 1, 1),
                match(ligue1, date(28), psg, om),
                match(coupe, null, om, psg));

        TeamHeadToHeadDto h2h = service.headToHead(1L, 2L);

        assertThat(h2h.byCompetition())
                .containsExactly(
                        new TeamHeadToHeadDto.CompetitionRecord(
                                10L, "Competition 10", CompetitionType.LEAGUE, "France", 2, 1, 1, 0),
                        new TeamHeadToHeadDto.CompetitionRecord(
                                11L, "Competition 11", CompetitionType.DOMESTIC_CUP, "France", 1, 0, 0, 1));
        assertThat(h2h.upcomingMatches()).extracting(MatchDto::date).containsExactly(date(28), null);
    }

    @Test
    void aucuneConfrontation() {
        givenMatches();

        TeamHeadToHeadDto h2h = service.headToHead(1L, 2L);

        assertThat(h2h.played()).isZero();
        assertThat(h2h.biggestWin()).isNull();
        assertThat(h2h.playedMatches()).isEmpty();
    }

    @Test
    void refuseLaMemeEquipeDesDeuxCotes() {
        assertThatThrownBy(() -> service.headToHead(1L, 1L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equipeInconnue() {
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.headToHead(1L, 99L)).isInstanceOf(EntityNotFoundException.class);
    }

    private void givenMatches(Match... matches) {
        when(matchRepository.findBetween(1L, 2L)).thenReturn(List.of(matches));
    }

    private Match played(Competition c, LocalDate date, Team team1, Team team2, int score1, int score2) {
        Match m = match(c, date, team1, team2);
        m.setScore1(score1);
        m.setScore2(score2);
        return m;
    }

    private Match match(Competition c, LocalDate date, Team team1, Team team2) {
        Match m = new Match();
        m.setId(nextMatchId++);
        m.setCompetition(c);
        m.setRoundLabel("J1");
        m.setDate(date);
        m.setTeam1(team1);
        m.setTeam2(team2);
        return m;
    }

    private static LocalDate date(int day) {
        return LocalDate.of(2026, 10, day);
    }

    private static Competition competition(Long id, CompetitionType type) {
        Competition c = new Competition("C" + id, "Competition " + id, type, "France", 2027);
        c.setId(id);
        return c;
    }

    private static Team team(Long id, String name) {
        Team team = new Team(name, "France");
        team.setId(id);
        return team;
    }
}
