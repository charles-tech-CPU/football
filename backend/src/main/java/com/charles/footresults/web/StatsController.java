package com.charles.footresults.web;

import com.charles.footresults.dto.CompetitionStatsDto;
import com.charles.footresults.service.StatsService;
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
}
