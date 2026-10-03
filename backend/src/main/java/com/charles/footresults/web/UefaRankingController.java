package com.charles.footresults.web;

import com.charles.footresults.dto.ClubUefaRankingDto;
import com.charles.footresults.dto.CountryUefaRankingDto;
import com.charles.footresults.dto.UefaHistoryPointDto;
import com.charles.footresults.service.UefaRankingService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/uefa-rankings")
public class UefaRankingController {

    private final UefaRankingService uefaRankingService;

    public UefaRankingController(UefaRankingService uefaRankingService) {
        this.uefaRankingService = uefaRankingService;
    }

    /** GET /api/uefa-rankings/clubs */
    @GetMapping("/clubs")
    public List<ClubUefaRankingDto> clubs() {
        return uefaRankingService.findClubRankings();
    }

    /** GET /api/uefa-rankings/clubs/{id}/history (id = ligne du classement des clubs) */
    @GetMapping("/clubs/{id}/history")
    public List<UefaHistoryPointDto> clubHistory(@PathVariable Long id) {
        return uefaRankingService.findClubHistory(id);
    }

    /** GET /api/uefa-rankings/countries/{id}/history (id = ligne du classement des pays) */
    @GetMapping("/countries/{id}/history")
    public List<UefaHistoryPointDto> countryHistory(@PathVariable Long id) {
        return uefaRankingService.findCountryHistory(id);
    }

    /** GET /api/uefa-rankings/countries */
    @GetMapping("/countries")
    public List<CountryUefaRankingDto> countries() {
        return uefaRankingService.findCountryRankings();
    }
}
