package com.charles.footresults.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Saison suivante d'un championnat cloture : clubs engages (maintenus et promus), dont les promus,
 * et creation de la coupe nationale de la meme saison.
 */
public record NextSeasonRequestDto(@NotEmpty List<Long> teamIds, List<Long> promotedIds, boolean withCup) {}
