package com.charles.footresults.domain;

/**
 * LEAGUE : championnat national (classement calcule sur toute la saison).
 * DOMESTIC_CUP : coupe nationale (a partir d'un tour variable selon le pays).
 * CONTINENTAL_CUP : Ligue des Champions / Europa League / Conference League.
 */
public enum CompetitionType {
    LEAGUE,
    DOMESTIC_CUP,
    CONTINENTAL_CUP
}
