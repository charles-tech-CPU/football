package com.charles.footresults.dto;

import java.util.List;

/** Inversion domicile / exterieur d'un match : ids des matchs retour (memes clubs, a l'envers) a inverser aussi. */
public record MatchSwapDto(List<Long> reverseMatchIds) {}
