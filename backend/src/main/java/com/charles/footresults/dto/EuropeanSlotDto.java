package com.charles.footresults.dto;

/** Place d'un pays en coupe d'Europe (cf. EuropeanSlotService) ; club null tant qu'il n'est pas connu. */
public record EuropeanSlotDto(
        Long id,
        Integer season,
        String competitionCode,
        Integer entryStage,
        String entryRound,
        String country,
        String route,
        Long teamId,
        String teamName,
        String teamLogoPath,
        boolean manual) {}
