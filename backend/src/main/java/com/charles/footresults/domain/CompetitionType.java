package com.charles.footresults.domain;

/**
 * LEAGUE : championnat national (classement calcule sur toute la saison).
 * DOMESTIC_CUP : coupe nationale (a partir d'un tour variable selon le pays).
 * CONTINENTAL_CUP : Ligue des Champions / Europa League / Conference League.
 * INTERNATIONAL : selections nationales, toutes confederations (Ligue des Nations
 * UEFA, qualifs CAN, Ligue des Nations Concacaf, Coupe d'Asie...) - volontairement
 * exclu du regroupement de la page Competitions (qui ne connait que les 3 types
 * ci-dessus), affiche dans les onglets dedies "Calendrier international" /
 * "Classements internationaux".
 */
public enum CompetitionType {
    LEAGUE,
    DOMESTIC_CUP,
    CONTINENTAL_CUP,
    INTERNATIONAL
}
