package com.charles.footresults.service;

import com.charles.footresults.domain.Honour;
import com.charles.footresults.domain.Team;
import com.charles.footresults.dto.PalmaresDto;
import com.charles.footresults.dto.PalmaresDto.Club;
import com.charles.footresults.dto.PalmaresDto.CompetitionHonours;
import com.charles.footresults.dto.PalmaresDto.SeasonHonour;
import com.charles.footresults.dto.PalmaresDto.TitleCount;
import com.charles.footresults.repository.HonourRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Palmares historique (table honour, cf. import/palmares.py) : saisons regroupees par competition
 * (championnat puis coupe pour un pays ; Ligue des Champions, Europa League, Conference League et
 * Coupe des coupes pour l'Europe) et decompte des titres par club.
 */
@Service
@Transactional(readOnly = true)
public class PalmaresService {

    static final String EUROPE = "Europe";
    private static final List<String> KIND_ORDER = List.of("LEAGUE", "CUP", "LDC", "EL", "ECL", "C2", "INTERTOTO");

    private final HonourRepository honourRepository;

    public PalmaresService(HonourRepository honourRepository) {
        this.honourRepository = honourRepository;
    }

    /** Pays par ordre alphabetique, l'Europe en premier. */
    public List<String> scopes() {
        List<String> scopes = new ArrayList<>(honourRepository.findScopes());
        if (scopes.remove(EUROPE)) {
            scopes.add(0, EUROPE);
        }
        return scopes;
    }

    public PalmaresDto palmares(String scope) {
        Map<String, List<Honour>> byKind = new LinkedHashMap<>();
        for (Honour h : honourRepository.findByScopeOrderBySeasonEndDescIdAsc(scope)) {
            byKind.computeIfAbsent(h.getKind(), k -> new ArrayList<>()).add(h);
        }
        List<CompetitionHonours> competitions = byKind.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> kindRank(e.getKey())))
                .map(e -> competition(e.getKey(), e.getValue()))
                .toList();
        return new PalmaresDto(scope, competitions);
    }

    private static int kindRank(String kind) {
        int rank = KIND_ORDER.indexOf(kind);
        return rank < 0 ? KIND_ORDER.size() : rank;
    }

    private static CompetitionHonours competition(String kind, List<Honour> honours) {
        List<SeasonHonour> seasons = honours.stream()
                .map(h -> new SeasonHonour(
                        h.getSeason(),
                        club(h.getWinner(), h.getWinnerTeam(), h.getWinnerCountry()),
                        club(h.getRunnerUp(), h.getRunnerUpTeam(), h.getRunnerUpCountry()),
                        club(h.getThird(), h.getThirdTeam(), null)))
                .toList();
        // Saisons de la plus recente a la plus ancienne : le 1er titre vu d'un club est son dernier.
        Map<String, TitleCount> titles = new LinkedHashMap<>();
        for (SeasonHonour s : seasons) {
            titles.merge(
                    s.winner().name(),
                    new TitleCount(s.winner(), 1, s.season()),
                    (a, b) -> new TitleCount(a.team(), a.titles() + 1, a.lastSeason()));
        }
        // Tri stable : a egalite de titres, l'ordre d'insertion (titre le plus recent) est garde.
        List<TitleCount> ranking = titles.values().stream()
                .sorted(Comparator.comparingInt(TitleCount::titles).reversed())
                .toList();
        return new CompetitionHonours(kind, honours.get(0).getCompetitionLabel(), seasons, ranking);
    }

    private static Club club(String name, Team team, String country) {
        if (name == null) {
            return null;
        }
        return team == null
                ? new Club(name, null, null, country)
                : new Club(name, team.getId(), team.getLogoPath(), country);
    }
}
