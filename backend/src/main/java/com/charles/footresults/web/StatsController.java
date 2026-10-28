package com.charles.footresults.web;

import com.charles.footresults.dto.AllLeaguesStatsDto;
import com.charles.footresults.dto.CompetitionStatsDto;
import com.charles.footresults.service.StatsService;
import java.util.Set;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    /** GET /api/stats?competitionId=1 : statistiques recalculees a partir des matchs joues. */
    @GetMapping("/api/stats")
    public CompetitionStatsDto stats(@RequestParam Long competitionId) {
        return statsService.competitionStats(competitionId);
    }

    /**
     * GET /api/stats/leagues : tous les championnats de la saison reunis, et le bilan de chacun.
     * GET /api/stats/leagues?countries=France,Espagne : seulement ces championnats.
     */
    @GetMapping("/api/stats/leagues")
    public AllLeaguesStatsDto allLeagues(@RequestParam(required = false) @Nullable Set<String> countries) {
        return statsService.allLeaguesStats(countries == null ? Set.of() : countries);
    }
}
