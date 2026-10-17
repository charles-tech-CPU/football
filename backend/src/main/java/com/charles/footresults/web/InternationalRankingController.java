package com.charles.footresults.web;

import com.charles.footresults.dto.InternationalRankingRowDto;
import com.charles.footresults.service.InternationalRankingService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InternationalRankingController {

    private final InternationalRankingService internationalRankingService;

    public InternationalRankingController(InternationalRankingService internationalRankingService) {
        this.internationalRankingService = internationalRankingService;
    }

    /** GET /api/international-ranking : classement Elo des selections, recalcule a chaque appel. */
    @GetMapping("/api/international-ranking")
    public List<InternationalRankingRowDto> ranking() {
        return internationalRankingService.compute();
    }
}
