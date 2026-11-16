package com.charles.footresults.dto;

/**
 * Ajustement de points d'un club au classement final fige (sanction, correction d'une erreur) :
 * points ajoutes (negatif pour un retrait) et motif facultatif. teamName n'est rempli qu'en sortie.
 */
public record PointsAdjustmentDto(Long teamId, String teamName, int points, String reason) {}
