package com.charles.footresults.service;

import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.Match;
import com.charles.footresults.dto.StandingRowDto;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Icones du calendrier en plus des affiches (FeaturedMatchService) :
 *
 * <ul>
 *   <li>Match de relegation : championnat dans ses {@link #FINAL_ROUNDS} dernieres journees
 *       (d'apres son nombre de journees attendu), entre deux equipes en zone rouge (relegation
 *       directe + barrage de maintien) ou a {@link #POINTS_MARGIN} points maximum de cette zone.
 *   <li>Rivalite : les deux clubs forment une rivalite connue (RivalryService), toutes
 *       competitions confondues.
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class MatchHighlightService {

    static final int FINAL_ROUNDS = 5;
    static final int POINTS_MARGIN = 3;

    private static final Pattern ROUND_NUMBER = Pattern.compile("^J(\\d+)$");

    private final StandingsService standingsService;
    private final RivalryService rivalryService;

    public MatchHighlightService(StandingsService standingsService, RivalryService rivalryService) {
        this.standingsService = standingsService;
        this.rivalryService = rivalryService;
    }

    /** Ids des matchs de relegation parmi les matchs donnes. */
    public Set<Long> relegationBattleIds(List<Match> matches) {
        Map<Long, Set<Long>> threatenedByCompetition = new HashMap<>();
        Set<Long> result = new HashSet<>();
        for (Match m : matches) {
            Competition c = m.getCompetition();
            if (!isFinalStretch(m)) {
                continue;
            }
            Set<Long> threatened = threatenedByCompetition.computeIfAbsent(c.getId(), id -> threatenedTeams(c));
            if (threatened.contains(m.getTeam1().getId())
                    && threatened.contains(m.getTeam2().getId())) {
                result.add(m.getId());
            }
        }
        return result;
    }

    /** Nom de la rivalite par id de match, pour les matchs entre rivaux. */
    public Map<Long, String> rivalryLabels(List<Match> matches) {
        if (matches.isEmpty()) {
            return Map.of();
        }
        Map<String, String> labels = rivalryService.labelsByPair();
        Map<Long, String> result = new HashMap<>();
        for (Match m : matches) {
            String label = labels.get(
                    RivalryService.pairKey(m.getTeam1().getId(), m.getTeam2().getId()));
            if (label != null) {
                result.put(m.getId(), label);
            }
        }
        return result;
    }

    /** Journee de championnat parmi les FINAL_ROUNDS dernieres (nombre de journees connu). */
    static boolean isFinalStretch(Match m) {
        Competition c = m.getCompetition();
        if (c.getType() != CompetitionType.LEAGUE || c.getTotalRounds() == null || m.getRoundLabel() == null) {
            return false;
        }
        Matcher round = ROUND_NUMBER.matcher(m.getRoundLabel().trim());
        return round.matches() && Integer.parseInt(round.group(1)) > c.getTotalRounds() - FINAL_ROUNDS;
    }

    /** Equipes en zone rouge ou a POINTS_MARGIN points maximum de la premiere place de cette zone. */
    private Set<Long> threatenedTeams(Competition c) {
        int zone = zero(c.getRelegationSlots()) + zero(c.getBarrageSlots());
        List<StandingRowDto> rows = standingsService.computeStandings(c.getId());
        return threatened(rows, zone);
    }

    static Set<Long> threatened(List<StandingRowDto> rows, int zone) {
        if (zone <= 0 || rows.size() <= zone) {
            return Set.of();
        }
        int firstInZone = rows.size() - zone;
        int threshold = rows.get(firstInZone).points() + POINTS_MARGIN;
        Set<Long> result = new HashSet<>();
        for (int i = 0; i < rows.size(); i++) {
            if (i >= firstInZone || rows.get(i).points() <= threshold) {
                result.add(rows.get(i).teamId());
            }
        }
        return result;
    }

    private static int zero(Integer value) {
        return value == null ? 0 : value;
    }
}
