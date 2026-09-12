package com.charles.footresults.dto;

import com.charles.footresults.domain.Team;

public record TeamDto(Long id, String name, String country, String logoPath) {
    public static TeamDto from(Team team) {
        return new TeamDto(team.getId(), team.getName(), team.getCountry(), team.getLogoPath());
    }
}
