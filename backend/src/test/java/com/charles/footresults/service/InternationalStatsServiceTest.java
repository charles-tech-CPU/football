package com.charles.footresults.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.MatchStatus;
import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.InternationalStatsDto;
import com.charles.footresults.dto.InternationalStatsDto.Journey;
import com.charles.footresults.dto.InternationalStatsDto.TeamRecord;
import com.charles.footresults.repository.CompetitionRepository;
import com.charles.footresults.repository.MatchRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InternationalStatsServiceTest {

    @Mock
    private CompetitionRepository competitionRepository;

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private InternationalStatsService statsService;

    private final Team france = team(1L, "France");
    private final Team bresil = team(2L, "Bresil");
    private final Team italie = team(3L, "Italie");
    private final Team maroc = team(4L, "Maroc");
    private final Team suede = team(5L, "Suede");

    private long nextId = 1;

    @Test
    void parcoursBilanEtQualificationsDUneCompetition() {
        Competition wc = competition("FIFA_WC_2022", 2022);
        Competition qualifs = competition("UEFA_WCQ_2022", 2022);
        List<Match> matches = new ArrayList<>(List.of(
                match(wc, "Phase de groupes", "A", france, suede, 2, 0, null, null, 10),
                match(wc, "Phase de groupes", "A", bresil, maroc, 1, 1, null, null, 10),
                match(wc, "Demi-finales", null, france, maroc, 2, 0, null, null, 14),
                match(wc, "Demi-finales", null, bresil, italie, 1, 0, null, null, 14),
                match(wc, "Match pour la 3e place", null, italie, maroc, 2, 1, null, null, 17),
                // Finale nulle gagnee aux tirs au but par le Bresil.
                match(wc, "Finale", null, france, bresil, 3, 3, 2, 4, 18),
                match(qualifs, "Groupe B", "B", france, suede, 1, 1, null, null, 1)));
        when(competitionRepository.findByType(CompetitionType.INTERNATIONAL)).thenReturn(List.of(wc, qualifs));
        when(matchRepository.findByCompetition_CodeIn(anyList())).thenReturn(matches);

        InternationalStatsDto stats = statsService.stats("WC");

        assertThat(stats.journeys())
                .extracting(
                        Journey::teamName,
                        Journey::titles,
                        Journey::runnerUps,
                        Journey::semiFinals,
                        Journey::groupStage)
                .containsExactly(
                        tuple("Bresil", 1, 0, 0, 0),
                        tuple("France", 0, 1, 0, 0),
                        tuple("Italie", 0, 0, 1, 0),
                        tuple("Maroc", 0, 0, 1, 0),
                        tuple("Suede", 0, 0, 0, 1));
        assertThat(stats.journeys().get(0).titleEditions()).containsExactly("2022");
        TeamRecord franceFinals = stats.finals().stream()
                .filter(r -> r.teamName().equals("France"))
                .findFirst()
                .orElseThrow();
        assertThat(franceFinals)
                .extracting(TeamRecord::editions, TeamRecord::played, TeamRecord::won, TeamRecord::drawn)
                .containsExactly(1, 3, 2, 1);
        assertThat(franceFinals.goalsFor()).isEqualTo(7);
        assertThat(stats.qualifiers()).extracting(TeamRecord::teamName).containsExactlyInAnyOrder("France", "Suede");
    }

    @Test
    void finaleRejoueeEtPouleFinaleSansFinale() {
        Competition copa = competition("CONMEBOL_COPA_1979", 1979);
        List<Match> final1979 = List.of(
                match(copa, "Finale - Aller", null, maroc, suede, 3, 0, null, null, 1),
                match(copa, "Finale", null, suede, maroc, 1, 0, null, null, 8),
                // Match d'appui nul : difference de buts sur toute la finale.
                match(copa, "Finale (match rejoué)", null, suede, maroc, 0, 0, null, null, 14));
        assertThat(InternationalStatsService.finalWinner(final1979)).isEqualTo(maroc.getId());

        Competition wc1950 = competition("FIFA_WC_1950", 1950);
        List<Match> poule = List.of(
                match(wc1950, "Phase de groupes", "1", bresil, suede, 7, 1, null, null, 1),
                match(wc1950, "Tour final", "Final", bresil, italie, 6, 1, null, null, 9),
                match(wc1950, "Tour final", "Final", france, suede, 3, 2, null, null, 9),
                match(wc1950, "Tour final", "Final", france, italie, 2, 2, null, null, 13),
                match(wc1950, "Tour final", "Final", italie, suede, 3, 1, null, null, 13),
                match(wc1950, "Tour final", "Final", france, bresil, 2, 1, null, null, 16),
                match(wc1950, "Tour final", "Final", bresil, suede, 7, 1, null, null, 16));
        Map<String, Integer> stages = InternationalStatsService.editionStages(poule, 1950).entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().getName(), Map.Entry::getValue));

        // A 2 points la victoire : France 5 pts, Bresil 4 pts (meme si le Bresil a plus de victoires).
        assertThat(stages)
                .containsEntry("France", InternationalStatsService.WINNER)
                .containsEntry("Bresil", InternationalStatsService.RUNNER_UP)
                .containsEntry("Italie", InternationalStatsService.SEMI);
    }

    @Test
    void uneCompetitionInconnueEstRefusee() {
        assertThatThrownBy(() -> statsService.stats("JO")).hasMessageContaining("inconnue");
    }

    private static Team team(Long id, String name) {
        Team t = new Team(name, name);
        t.setId(id);
        return t;
    }

    private static Competition competition(String code, int season) {
        return new Competition(code, code, CompetitionType.INTERNATIONAL, null, season);
    }

    private Match match(
            Competition c,
            String round,
            String group,
            Team team1,
            Team team2,
            int score1,
            int score2,
            Integer penalties1,
            Integer penalties2,
            int day) {
        Match m = new Match();
        m.setId(nextId++);
        m.setCompetition(c);
        m.setRoundLabel(round);
        m.setGroupName(group);
        m.setTeam1(team1);
        m.setTeam2(team2);
        m.setScore1(score1);
        m.setScore2(score2);
        m.setPenaltyScore1(penalties1);
        m.setPenaltyScore2(penalties2);
        m.setStatus(MatchStatus.COMPLETED);
        m.setDate(LocalDate.of(c.getSeason(), 6, day));
        return m;
    }
}
