package com.charles.footresults.web;

import com.charles.footresults.dto.InternationalRankingRowDto;
import com.charles.footresults.dto.InternationalStatsDto;
import com.charles.footresults.service.InternationalRankingService;
import com.charles.footresults.service.InternationalStatsService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InternationalRankingController {

    private final InternationalRankingService internationalRankingService;
    private final InternationalStatsService internationalStatsService;

    public InternationalRankingController(
            InternationalRankingService internationalRankingService,
            InternationalStatsService internationalStatsService) {
        this.internationalRankingService = internationalRankingService;
        this.internationalStatsService = internationalStatsService;
    }

    /** GET /api/international-ranking : classement Elo des selections, recalcule a chaque appel. */
    @GetMapping("/api/international-ranking")
    public List<InternationalRankingRowDto> ranking() {
        return internationalRankingService.compute();
    }

    /** GET /api/international-stats?family=WC : bilan historique d'une competition (WC, EURO, COPA...). */
    @GetMapping("/api/international-stats")
    public InternationalStatsDto stats(@RequestParam String family) {
        return internationalStatsService.stats(family);
    }
}
