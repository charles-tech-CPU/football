package com.charles.footresults.service;

import com.charles.footresults.domain.ClubUefaRanking;
import com.charles.footresults.domain.Competition;
import com.charles.footresults.domain.CompetitionType;
import com.charles.footresults.domain.Match;
import com.charles.footresults.domain.MatchStatus;
import com.charles.footresults.repository.ClubUefaRankingRepository;
import com.charles.footresults.repository.CountryUefaRankingRepository;
import com.charles.footresults.repository.MatchRepository;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntFunction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Affiches "a voir" du calendrier : par journee (competition + round_label), les matchs qui
 * opposent les equipes les mieux classees. 2 affiches par journee dans les 10 premiers
 * championnats du classement UEFA des pays, 1 dans les autres, 4 par tour en coupe d'Europe
 * (LDC/EL/EC). Les coupes nationales n'ont pas d'affiche.
 *
 * Le choix se fait sur TOUTE la journee (pas seulement la page affichee), matchs deja joues
 * compris, pour que les affiches d'une journee ne changent pas au fil de ses matchs. Classement
 * pris en compte : rang en championnat pour un championnat (les 2 equipes doivent deja etre
 * classees, donc pas d'affiche avant la 1re journee jouee), rang UEFA du club pour une coupe
 * d'Europe (un club absent du classement UEFA passe apres tous les autres).
 */
@Service
@Transactional(readOnly = true)
public class FeaturedMatchService {

    static final int BIG_LEAGUE_COUNT = 10;
    static final int BIG_LEAGUE_PICKS = 2;
    static final int OTHER_LEAGUE_PICKS = 1;
    static final int CONTINENTAL_PICKS = 4;

    private static final Set<String> CONTINENTAL_CODES = Set.of("LDC", "EL", "EC");
    private static final Set<MatchStatus> EXCLUDED_STATUSES = Set.of(MatchStatus.POSTPONED, MatchStatus.SUSPENDED);
    private static final int UNRANKED_CLUB = 10_000;

    private final MatchRepository matchRepository;
    private final CountryUefaRankingRepository countryRankingRepository;
    private final ClubUefaRankingRepository clubRankingRepository;

    public FeaturedMatchService(
            MatchRepository matchRepository,
            CountryUefaRankingRepository countryRankingRepository,
            ClubUefaRankingRepository clubRankingRepository) {
        this.matchRepository = matchRepository;
        this.countryRankingRepository = countryRankingRepository;
        this.clubRankingRepository = clubRankingRepository;
    }

    /**
     * Ids des affiches des journees auxquelles appartiennent les matchs donnes (peut contenir
     * des matchs hors de cette liste). leagueRanks : rang en championnat par id d'equipe, qui
     * doit couvrir les pays des championnats concernes.
     */
    public Set<Long> featuredMatchIds(List<Match> matches, Map<Long, Integer> leagueRanks) {
        Map<String, Match> rounds = new HashMap<>();
        for (Match m : matches) {
            if (m.getRoundLabel() != null && (isLeague(m.getCompetition()) || isContinental(m.getCompetition()))) {
                rounds.putIfAbsent(m.getCompetition().getId() + "|" + m.getRoundLabel(), m);
            }
        }
        if (rounds.isEmpty()) {
            return Set.of();
        }

        Set<String> bigLeagueCountries = null;
        Map<Long, Integer> clubRanks = null;
        Set<Long> featured = new HashSet<>();
        for (Match sample : rounds.values()) {
            Competition competition = sample.getCompetition();
            List<Match> round =
                    matchRepository
                            .findByCompetitionIdAndRoundLabel(competition.getId(), sample.getRoundLabel())
                            .stream()
                            .filter(m -> !EXCLUDED_STATUSES.contains(m.getStatus()))
                            .toList();
            if (isContinental(competition)) {
                if (clubRanks == null) clubRanks = clubUefaRanks();
                Map<Long, Integer> ranks = clubRanks;
                featured.addAll(pick(
                        round,
                        m -> rankOf(ranks, m.getTeam1().getId()),
                        m -> rankOf(ranks, m.getTeam2().getId()),
                        CONTINENTAL_PICKS));
            } else {
                if (bigLeagueCountries == null) bigLeagueCountries = bigLeagueCountries();
                int picks =
                        bigLeagueCountries.contains(competition.getCountry()) ? BIG_LEAGUE_PICKS : OTHER_LEAGUE_PICKS;
                List<Match> ranked = round.stream()
                        .filter(m -> leagueRanks.containsKey(m.getTeam1().getId())
                                && leagueRanks.containsKey(m.getTeam2().getId()))
                        .toList();
                featured.addAll(pick(
                        ranked,
                        m -> leagueRanks.get(m.getTeam1().getId()),
                        m -> leagueRanks.get(m.getTeam2().getId()),
                        picks));
            }
        }
        return featured;
    }

    /** Les matchs dont les 2 equipes sont les mieux classees : somme des rangs, puis le moins bon des 2. */
    private static List<Long> pick(
            List<Match> round, ToIntFunction<Match> rank1, ToIntFunction<Match> rank2, int count) {
        return round.stream()
                .sorted(Comparator.<Match>comparingInt(m -> rank1.applyAsInt(m) + rank2.applyAsInt(m))
                        .thenComparingInt(m -> Math.max(rank1.applyAsInt(m), rank2.applyAsInt(m))))
                .limit(count)
                .map(Match::getId)
                .toList();
    }

    private Set<String> bigLeagueCountries() {
        Set<String> countries = new HashSet<>();
        countryRankingRepository.findAllByOrderByUefaRankAsc().stream()
                .limit(BIG_LEAGUE_COUNT)
                .forEach(r -> countries.add(r.getCountry()));
        return countries;
    }

    private Map<Long, Integer> clubUefaRanks() {
        Map<Long, Integer> ranks = new HashMap<>();
        for (ClubUefaRanking r : clubRankingRepository.findAll()) {
            if (r.getTeam() != null) {
                ranks.merge(r.getTeam().getId(), r.getUefaRank(), Math::min);
            }
        }
        return ranks;
    }

    private static int rankOf(Map<Long, Integer> ranks, Long teamId) {
        return ranks.getOrDefault(teamId, UNRANKED_CLUB);
    }

    private static boolean isLeague(Competition c) {
        return c.getType() == CompetitionType.LEAGUE;
    }

    private static boolean isContinental(Competition c) {
        return CONTINENTAL_CODES.contains(c.getCode());
    }
}
