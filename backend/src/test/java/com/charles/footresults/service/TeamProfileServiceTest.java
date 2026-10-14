package com.charles.footresults.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.ClubUefaRankingDto;
import com.charles.footresults.dto.MatchDto;
import com.charles.footresults.dto.StandingRowDto;
import com.charles.footresults.dto.TeamProfileDto;
import com.charles.footresults.repository.MatchRepository;
import com.charles.footresults.repository.TeamRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeamProfileServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private StandingsService standingsService;

    @Mock
    private UefaRankingService uefaRankingService;

    @InjectMocks
    private TeamProfileService teamProfileService;

    private Team psg;
    private Team om;
    private Competition ligue1;
    private Competition ligue1Ancienne;
    private Competition ldc;
    private long nextMatchId = 1;

    @BeforeEach
    void setUp() {
        psg = team(1L, "PSG");
        om = team(2L, "OM");
        ligue1 = competition(10L, CompetitionType.LEAGUE, 2027);
        ligue1Ancienne = competition(9L, CompetitionType.LEAGUE, 2026);
        ldc = competition(20L, CompetitionType.CONTINENTAL_CUP, 2027);
        lenient().when(teamRepository.findById(1L)).thenReturn(Optional.of(psg));
    }

    @Test
    void formeEtSerieDuPlusRecentAuPlusAncienToutesCompetitionsConfondues() {
        // Chronologie : D (ancienne saison), W, W (LDC, PSG a l'exterieur), L, W, W.
        givenMatches(
                played(ligue1Ancienne, date(1), psg, om, 0, 0),
                played(ligue1, date(10), psg, om, 3, 1),
                played(ldc, date(12), om, psg, 0, 2),
                played(ligue1, date(15), psg, om, 0, 1),
                played(ligue1, date(20), om, psg, 1, 2),
                played(ligue1, date(25), psg, om, 1, 0));

        TeamProfileDto profile = teamProfileService.profile(1L);

        assertThat(profile.form()).containsExactly("W", "W", "L", "W", "W");
        assertThat(profile.streak()).isEqualTo(new TeamProfileDto.Streak("W", 2, 2));
        assertThat(profile.recentMatches()).hasSize(6);
        assertThat(profile.recentMatches().get(0).date()).isEqualTo(date(25));
    }

    @Test
    void prochainsMatchsDuPlusProcheAuPlusLointainSansDateEnDernier() {
        Match sansDate = upcoming(ligue1, null);
        Match lointain = upcoming(ligue1, date(28));
        Match proche = upcoming(ldc, date(21));
        givenMatches(sansDate, lointain, proche);

        TeamProfileDto profile = teamProfileService.profile(1L);

        assertThat(profile.upcomingMatches())
                .extracting(MatchDto::id)
                .containsExactly(proche.getId(), lointain.getId(), sansDate.getId());
        assertThat(profile.streak()).isNull();
        assertThat(profile.form()).isEmpty();
    }

    @Test
    void rangDansLeChampionnatDeLaSaisonLaPlusRecente() {
        givenMatches(played(ligue1Ancienne, date(1), psg, om, 1, 0), played(ligue1, date(10), psg, om, 3, 1));
        when(standingsService.computeStandings(10L)).thenReturn(List.of(row(2L, 3, 0), row(1L, 3, 3)));

        TeamProfileDto.LeagueStanding league = teamProfileService.profile(1L).league();

        assertThat(league.competitionId()).isEqualTo(10L);
        assertThat(league.rank()).isEqualTo(2);
        assertThat(league.teamCount()).isEqualTo(2);
        assertThat(league.points()).isEqualTo(3);
        assertThat(league.goalsFor()).isEqualTo(3);
    }

    @Test
    void sansMatchDeChampionnatNiLigneUefaCesBlocsSontVides() {
        givenMatches(played(ldc, date(12), om, psg, 0, 2));
        ClubUefaRankingDto autreClub = new ClubUefaRankingDto(
                5L, 1, 2L, "OM", null, "France", "LDC", BigDecimal.TEN, BigDecimal.ONE, null, null, null, null, 1);
        when(uefaRankingService.findClubRankings()).thenReturn(List.of(autreClub));

        TeamProfileDto profile = teamProfileService.profile(1L);

        assertThat(profile.league()).isNull();
        assertThat(profile.uefa()).isNull();
    }

    @Test
    void ligneUefaDuClubAvecRangsRecalcules() {
        givenMatches();
        ClubUefaRankingDto ligne = new ClubUefaRankingDto(
                7L, 3, 1L, "PSG", null, "France", "LDC", BigDecimal.TEN, BigDecimal.ONE, null, null, null, null, 5);
        when(uefaRankingService.findClubRankings()).thenReturn(List.of(ligne));

        TeamProfileDto.UefaStanding uefa = teamProfileService.profile(1L).uefa();

        assertThat(uefa).isEqualTo(new TeamProfileDto.UefaStanding(7L, 3, 5, BigDecimal.TEN, BigDecimal.ONE, "LDC"));
    }

    @Test
    void equipeInconnue() {
        when(teamRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamProfileService.profile(404L)).isInstanceOf(EntityNotFoundException.class);
    }

    private void givenMatches(Match... matches) {
        // L'ordre renvoye par le depot ne doit pas compter : le service retrie.
        List<Match> list = new ArrayList<>(List.of(matches));
        when(matchRepository.findByTeam1_IdOrTeam2_IdOrderByDateDesc(1L, 1L)).thenReturn(list);
    }

    private Match played(Competition c, LocalDate date, Team team1, Team team2, int score1, int score2) {
        Match m = match(c, date, team1, team2);
        m.setScore1(score1);
        m.setScore2(score2);
        return m;
    }

    private Match upcoming(Competition c, LocalDate date) {
        return match(c, date, psg, om);
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

    private static StandingRowDto row(Long teamId, int points, int goalsFor) {
        return new StandingRowDto(teamId, "Equipe " + teamId, null, null, 1, 0, 0, 0, goalsFor, 0, 0, points, null);
    }

    private static Competition competition(Long id, CompetitionType type, int season) {
        Competition c = new Competition("C" + id, "Competition " + id, type, "France", season);
        c.setId(id);
        return c;
    }

    private static Team team(Long id, String name) {
        Team team = new Team(name, "France");
        team.setId(id);
        return team;
    }
}
