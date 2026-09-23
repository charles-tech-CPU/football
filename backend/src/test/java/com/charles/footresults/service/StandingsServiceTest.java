package com.charles.footresults.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.MatchStatus;
import com.charles.footresults.domain.Team;
import com.charles.footresults.domain.TeamCompetitionStatus;
import com.charles.footresults.dto.HeadToHeadCellDto;
import com.charles.footresults.dto.StandingRowDto;
import com.charles.footresults.repository.MatchRepository;
import com.charles.footresults.repository.TeamCompetitionStatusRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StandingsServiceTest {

    private static final Long COMPETITION_ID = 1L;

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private TeamCompetitionStatusRepository statusRepository;

    @InjectMocks
    private StandingsService standingsService;

    private Team psg;
    private Team om;
    private Team lyon;
    private Team lens;

    @BeforeEach
    void setUp() {
        psg = team(1L, "PSG");
        om = team(2L, "OM");
        lyon = team(3L, "Lyon");
        lens = team(4L, "Lens");
        lenient().when(statusRepository.findByCompetitionId(anyLong())).thenReturn(List.of());
    }

    @Test
    void classementTrieParPointsPuisDifferenceDeButsPuisButsMarques() {
        givenPlayedMatches(
                match(psg, om, 3, 0), // PSG 3 pts, diff +3
                match(lyon, lens, 4, 2), // Lyon 3 pts, diff +2
                match(om, lens, 1, 1), // OM et Lens : 1 pt chacun
                match(lyon, psg, 0, 0)); // Lyon et PSG : 1 pt chacun

        List<StandingRowDto> standings = standingsService.computeStandings(COMPETITION_ID);

        // PSG et Lyon a 4 pts : PSG devant a la difference de buts (+3 contre +2).
        // OM et Lens a 1 pt : Lens devant a la difference de buts (-2 contre -3).
        assertThat(standings).extracting(StandingRowDto::teamName).containsExactly("PSG", "Lyon", "Lens", "OM");
        StandingRowDto psgRow = standings.get(0);
        assertThat(psgRow.played()).isEqualTo(2);
        assertThat(psgRow.won()).isEqualTo(1);
        assertThat(psgRow.drawn()).isEqualTo(1);
        assertThat(psgRow.lost()).isZero();
        assertThat(psgRow.goalsFor()).isEqualTo(3);
        assertThat(psgRow.goalsAgainst()).isZero();
        assertThat(psgRow.goalDifference()).isEqualTo(3);
        assertThat(psgRow.points()).isEqualTo(4);
        assertThat(psgRow.group()).isNull();
    }

    @Test
    void aEgaliteDePointsEtDeDifferenceLesButsMarquesDepartagent() {
        givenPlayedMatches(match(psg, lens, 3, 2), match(om, lyon, 1, 0));

        List<StandingRowDto> standings = standingsService.computeStandings(COMPETITION_ID);

        assertThat(standings).extracting(StandingRowDto::teamName).startsWith("PSG", "OM");
    }

    @Test
    void unMatchSansScoreNeCompteNiDansLeClassementNiDansLeTeteATete() {
        givenPlayedMatches(match(psg, om, null, null));

        List<StandingRowDto> standings = standingsService.computeStandings(COMPETITION_ID);
        List<HeadToHeadCellDto> h2h = standingsService.computeHeadToHead(COMPETITION_ID);

        assertThat(standings).allSatisfy(row -> {
            assertThat(row.played()).isZero();
            assertThat(row.points()).isZero();
        });
        assertThat(h2h).isEmpty();
    }

    @Test
    void championnatScindeLeGroupeAvecLePlusDePointsEnPremier() {
        Competition league = competition(CompetitionType.LEAGUE);
        when(statusRepository.findByCompetitionId(COMPETITION_ID))
                .thenReturn(List.of(
                        status(league, psg, "Relegation"),
                        status(league, lens, "Relegation"),
                        status(league, om, "Championnat"),
                        status(league, lyon, "Championnat")));
        givenPlayedMatches(match(om, lyon, 2, 0), match(psg, lens, 1, 1));

        List<StandingRowDto> standings = standingsService.computeStandings(COMPETITION_ID);

        assertThat(standings).extracting(StandingRowDto::teamName).containsExactly("OM", "Lyon", "PSG", "Lens");
        assertThat(standings)
                .extracting(StandingRowDto::group)
                .containsExactly("Championnat", "Championnat", "Relegation", "Relegation");
    }

    @Test
    void uneEquipeGroupeeSansMatchApparaitQuandMemeAZeroPoint() {
        Competition league = competition(CompetitionType.LEAGUE);
        when(statusRepository.findByCompetitionId(COMPETITION_ID))
                .thenReturn(List.of(status(league, psg, "Championnat"), status(league, om, "Championnat")));
        givenPlayedMatches();

        List<StandingRowDto> standings = standingsService.computeStandings(COMPETITION_ID);

        assertThat(standings).extracting(StandingRowDto::teamName).containsExactlyInAnyOrder("PSG", "OM");
        assertThat(standings).allSatisfy(row -> assertThat(row.played()).isZero());
    }

    @Test
    void competitionInternationaleLesGroupesSontTriesParOrdreAlphabetique() {
        Competition nations = competition(CompetitionType.INTERNATIONAL);
        when(statusRepository.findByCompetitionId(COMPETITION_ID))
                .thenReturn(List.of(
                        status(nations, psg, "Groupe B"),
                        status(nations, om, "Groupe B"),
                        status(nations, lyon, "Groupe A"),
                        status(nations, lens, "Groupe A")));
        // Le groupe B a plus de points, mais l'ordre reste alphabetique.
        givenPlayedMatches(match(psg, om, 5, 0), match(lyon, lens, 0, 0));

        List<StandingRowDto> standings = standingsService.computeStandings(COMPETITION_ID);

        assertThat(standings)
                .extracting(StandingRowDto::group)
                .containsExactly("Groupe A", "Groupe A", "Groupe B", "Groupe B");
    }

    @Test
    void lesEquipesSansGroupeSontAjouteesApresLesGroupes() {
        Competition league = competition(CompetitionType.LEAGUE);
        when(statusRepository.findByCompetitionId(COMPETITION_ID))
                .thenReturn(List.of(
                        status(league, psg, "Championnat"),
                        status(league, om, "Championnat"),
                        status(league, lyon, " ")));
        givenPlayedMatches(match(lyon, lens, 3, 0));

        List<StandingRowDto> standings = standingsService.computeStandings(COMPETITION_ID);

        assertThat(standings).extracting(StandingRowDto::teamName).containsExactly("PSG", "OM", "Lyon", "Lens");
        assertThat(standings.get(2).group()).isNull();
    }

    @Test
    void classementDUnePhaseNeRetientQueLesMatchsDeCettePhase() {
        when(matchRepository.findByCompetitionIdAndStatusAndRoundLabelContainingIgnoreCaseOrderByDateAscTimeAsc(
                        COMPETITION_ID, MatchStatus.COMPLETED, "Phase de ligue"))
                .thenReturn(List.of(match(om, psg, 2, 1), match(lyon, lens, 1, 0)));

        List<StandingRowDto> standings = standingsService.computeStandingsForRound(COMPETITION_ID, "Phase de ligue");

        assertThat(standings).extracting(StandingRowDto::teamName).containsExactly("OM", "Lyon", "PSG", "Lens");
        assertThat(standings).allSatisfy(row -> assertThat(row.group()).isNull());
    }

    @Test
    void teteATeteCompteVictoiresNulsEtDefaitesDuPointDeVueDeChaqueEquipe() {
        givenPlayedMatches(match(psg, om, 2, 1), match(om, psg, 0, 0), match(om, psg, 3, 0));

        List<HeadToHeadCellDto> h2h = standingsService.computeHeadToHead(COMPETITION_ID);

        assertThat(h2h)
                .containsExactlyInAnyOrder(
                        new HeadToHeadCellDto(psg.getId(), om.getId(), 1, 1, 1),
                        new HeadToHeadCellDto(om.getId(), psg.getId(), 1, 1, 1));
    }

    private void givenPlayedMatches(Match... matches) {
        when(matchRepository.findByCompetitionIdAndStatusOrderByDateAscTimeAsc(COMPETITION_ID, MatchStatus.COMPLETED))
                .thenReturn(List.of(matches));
    }

    private static Team team(Long id, String name) {
        Team team = new Team(name, "France");
        team.setId(id);
        return team;
    }

    private static Competition competition(CompetitionType type) {
        Competition competition = new Competition("TEST", "Competition de test", type, null, 2027);
        competition.setId(COMPETITION_ID);
        return competition;
    }

    private static TeamCompetitionStatus status(Competition competition, Team team, String group) {
        TeamCompetitionStatus status = new TeamCompetitionStatus(competition, team);
        status.setGroupName(group);
        return status;
    }

    private static Match match(Team team1, Team team2, Integer score1, Integer score2) {
        Match match = new Match();
        match.setTeam1(team1);
        match.setTeam2(team2);
        match.setScore1(score1);
        match.setScore2(score2);
        match.setStatus(MatchStatus.COMPLETED);
        return match;
    }
}
