package com.charles.footresults.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * Colonnes de points UEFA communes aux classements clubs et pays (onglets UEFA/PAYS du fichier
 * Excel) : total du coefficient et points des 5 dernieres saisons.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class UefaPointsHistory {

    private BigDecimal total;

    /** Valeur "PTS 2027" du fichier Excel au moment de l'import ; a titre d'audit uniquement, jamais affichee telle quelle (cf. UefaRankingService). */
    @Column(name = "points_2027_imported")
    private BigDecimal points2027Imported;

    @Column(name = "points_2026")
    private BigDecimal points2026;

    @Column(name = "points_2025")
    private BigDecimal points2025;

    @Column(name = "points_2024")
    private BigDecimal points2024;

    @Column(name = "points_2023")
    private BigDecimal points2023;
}
