package com.charles.footresults.service;

import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.MatchStatus;
import com.charles.footresults.domain.Team;
import com.charles.footresults.domain.TeamCompetitionStatus;
import com.charles.footresults.dto.HeadToHeadCellDto;
import com.charles.footresults.dto.StandingRowDto;
import com.charles.footresults.repository.MatchRepository;
import com.charles.footresults.repository.TeamCompetitionStatusRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Classement et tete-a-tete ne sont jamais stockes : ils sont recalcules a
 * partir des matchs COMPLETED d'une competition. Points classiques : victoire
 * = 3, nul = 1, defaite = 0. N'a de sens que pour une competition de type
 * LEAGUE (les coupes sont a elimination directe, pas de classement).
 *
 * Certains championnats se scindent en 2e partie de saison en plusieurs
 * mini-groupes (ex: Finlande - top 6 / bottom 6, meme principe en Ecosse,
 * Autriche, Belgique...) : les points de la phase 1 (saison reguliere,
 * TOUS les adversaires) sont conserves tels quels, puis les matchs de la
 * phase 2 (uniquement entre membres du meme groupe final) s'ajoutent par
 * dessus. Comme ce calcul est deja exactement "additionner tous les matchs
 * joues par l'equipe", aucun filtrage particulier n'est necessaire : on
 * garde le meme calcul qu'une competition classique, et on se contente de
 * REPARTIR les lignes obtenues par groupe (TeamCompetitionStatus.groupName)
 * pour l'affichage. Une competition sans aucune equipe groupee garde un
 * classement unique, inchange.
 */
@Service
public class StandingsService {

    private static final int POINTS_WIN = 3;
    private static final int POINTS_DRAW = 1;

    private final MatchRepository matchRepository;
    private final TeamCompetitionStatusRepository statusRepository;

    public StandingsService(MatchRepository matchRepository, TeamCompetitionStatusRepository statusRepository) {
        this.matchRepository = matchRepository;
        this.statusRepository = statusRepository;
    }

    /**
     * Classement d'une seule phase d'une competition (ex: "Phase de ligue" des
     * coupes d'Europe format 36 clubs), identifiee par un fragment contenu
     * dans le round_label des matchs. Pas de notion de groupe ici : une phase
     * de ligue continentale n'est jamais scindee en mini-groupes comme un
     * championnat national.
     */
    public List<StandingRowDto> computeStandingsForRound(Long competitionId, String roundLabelPart) {
        Map<Long, TeamTally> byTeam = new LinkedHashMap<>();
        for (Match m :
                matchRepository.findByCompetitionIdAndStatusAndRoundLabelContainingIgnoreCaseOrderByDateAscTimeAsc(
                        competitionId, MatchStatus.COMPLETED, roundLabelPart)) {
            tallyFor(byTeam, m.getTeam1()).addResult(m.getScore1(), m.getScore2());
            tallyFor(byTeam, m.getTeam2()).addResult(m.getScore2(), m.getScore1());
        }
        List<StandingRowDto> rows =
                byTeam.values().stream().map(t -> t.toDto(null)).toList();
        return sortRows(rows);
    }

    public List<StandingRowDto> computeStandings(Long competitionId) {
        Map<Long, String> teamGroup = new LinkedHashMap<>();
        // Une equipe inscrite dans un groupe apparait des le depart (0 match, 0 pt),
        // meme avant son premier match : un groupe s'affiche toujours complet.
        Map<Long, TeamTally> byTeam = new LinkedHashMap<>();
        boolean international = false;
        for (TeamCompetitionStatus s : statusRepository.findByCompetitionId(competitionId)) {
            international = s.getCompetition().getType() == CompetitionType.INTERNATIONAL;
            if (s.getGroupName() != null && !s.getGroupName().isBlank()) {
                teamGroup.put(s.getTeam().getId(), s.getGroupName());
                tallyFor(byTeam, s.getTeam());
            }
        }

        for (Match m : playedMatches(competitionId)) {
            tallyFor(byTeam, m.getTeam1()).addResult(m.getScore1(), m.getScore2());
            tallyFor(byTeam, m.getTeam2()).addResult(m.getScore2(), m.getScore1());
        }

        List<StandingRowDto> rows = byTeam.values().stream()
                .map(t -> t.toDto(teamGroup.get(t.team.getId())))
                .toList();

        if (teamGroup.isEmpty()) {
            return sortRows(rows);
        }

        Map<String, List<StandingRowDto>> byGroup = new LinkedHashMap<>();
        List<StandingRowDto> ungrouped = new ArrayList<>();
        for (StandingRowDto row : rows) {
            if (row.group() == null) {
                ungrouped.add(row);
            } else {
                byGroup.computeIfAbsent(row.group(), g -> new ArrayList<>()).add(row);
            }
        }

        // Championnat scinde : le groupe du haut (plus de points) d'abord. Selections
        // nationales : ordre alphabetique ("Groupe A" avant "Groupe B", "Ligue A - ..."
        // avant "Ligue B - ..."), independant des resultats.
        Comparator<Map.Entry<String, List<StandingRowDto>>> groupOrder = international
                ? Map.Entry.comparingByKey()
                : Comparator.comparingInt((Map.Entry<String, List<StandingRowDto>> e) -> e.getValue().stream()
                                .mapToInt(StandingRowDto::points)
                                .max()
                                .orElse(0))
                        .reversed();
        List<StandingRowDto> result = byGroup.entrySet().stream()
                .sorted(groupOrder)
                .map(e -> sortRows(e.getValue()))
                .flatMap(List::stream)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        result.addAll(sortRows(ungrouped));
        return result;
    }

    private List<StandingRowDto> sortRows(List<StandingRowDto> rows) {
        return rows.stream()
                .sorted(Comparator.comparingInt(StandingRowDto::points)
                        .reversed()
                        .thenComparing(Comparator.comparingInt(StandingRowDto::goalDifference)
                                .reversed())
                        .thenComparing(Comparator.comparingInt(StandingRowDto::goalsFor)
                                .reversed()))
                .toList();
    }

    public List<HeadToHeadCellDto> computeHeadToHead(Long competitionId) {
        Map<Long, Map<Long, int[]>> tally = new LinkedHashMap<>();

        for (Match m : playedMatches(competitionId)) {
            addH2H(tally, m.getTeam1().getId(), m.getTeam2().getId(), m.getScore1(), m.getScore2());
            addH2H(tally, m.getTeam2().getId(), m.getTeam1().getId(), m.getScore2(), m.getScore1());
        }

        List<HeadToHeadCellDto> result = new ArrayList<>();
        tally.forEach((teamAId, opponents) -> opponents.forEach(
                (teamBId, wdl) -> result.add(new HeadToHeadCellDto(teamAId, teamBId, wdl[0], wdl[1], wdl[2]))));
        return result;
    }

    private List<Match> playedMatches(Long competitionId) {
        return matchRepository.findByCompetitionIdAndStatusOrderByDateAscTimeAsc(competitionId, MatchStatus.COMPLETED);
    }

    private TeamTally tallyFor(Map<Long, TeamTally> byTeam, Team team) {
        return byTeam.computeIfAbsent(team.getId(), id -> new TeamTally(team));
    }

    private void addH2H(Map<Long, Map<Long, int[]>> tally, Long teamAId, Long teamBId, Integer goalsA, Integer goalsB) {
        if (goalsA == null || goalsB == null) {
            return;
        }
        int[] wdl =
                tally.computeIfAbsent(teamAId, id -> new LinkedHashMap<>()).computeIfAbsent(teamBId, id -> new int[3]);
        if (goalsA > goalsB) {
            wdl[0]++;
        } else if (goalsA.equals(goalsB)) {
            wdl[1]++;
        } else {
            wdl[2]++;
        }
    }

    /** Petit accumulateur mutable, interne au calcul, jamais expose ni persiste. */
    private static final class TeamTally {
        private final Team team;
        private int played;
        private int won;
        private int drawn;
        private int lost;
        private int goalsFor;
        private int goalsAgainst;

        private TeamTally(Team team) {
            this.team = team;
        }

        private void addResult(Integer goalsFor, Integer goalsAgainst) {
            if (goalsFor == null || goalsAgainst == null) {
                return;
            }
            played++;
            this.goalsFor += goalsFor;
            this.goalsAgainst += goalsAgainst;
            if (goalsFor > goalsAgainst) {
                won++;
            } else if (goalsFor.equals(goalsAgainst)) {
                drawn++;
            } else {
                lost++;
            }
        }

        private StandingRowDto toDto(String group) {
            int points = won * POINTS_WIN + drawn * POINTS_DRAW;
            return new StandingRowDto(
                    team.getId(),
                    team.getName(),
                    team.getLogoPath(),
                    team.getCountry(),
                    played,
                    won,
                    drawn,
                    lost,
                    goalsFor,
                    goalsAgainst,
                    goalsFor - goalsAgainst,
                    points,
                    group);
        }
    }
}
