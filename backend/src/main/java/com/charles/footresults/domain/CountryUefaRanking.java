package com.charles.footresults.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Une ligne de l'onglet PAYS du fichier Excel source (classement des pays, coefficient
 * UEFA) : importee telle quelle par import/extract_uefa_rankings.py, a l'exception des
 * points de la saison en cours (voir points2027Imported), jamais affiches directement
 * (cf. UefaRankingService).
 */
@Entity
@Table(name = "country_uefa_ranking")
@Getter
@Setter
@NoArgsConstructor
public class CountryUefaRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uefa_rank", nullable = false)
    private Integer uefaRank;

    @Column(nullable = false, length = 60, unique = true)
    private String country;

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

    /** Photo au moment de l'export Excel (donc vite perimee) : la valeur reellement servie a
     * l'API est recalculee en direct par UefaRankingService a partir des matchs deja saisis,
     * ces colonnes ne sont plus lues que par le script d'import. */
    @Column(name = "ldc_now")
    private Integer ldcNow;

    @Column(name = "el_now")
    private Integer elNow;

    @Column(name = "ec_now")
    private Integer ecNow;

    @Column(name = "ldc_debut")
    private Integer ldcDebut;

    @Column(name = "el_debut")
    private Integer elDebut;

    @Column(name = "ec_debut")
    private Integer ecDebut;

    @Column(name = "nb_2027")
    private Integer nb2027;

    @Column(name = "nb_2026")
    private Integer nb2026;

    @Column(name = "nb_2025")
    private Integer nb2025;

    @Column(name = "nb_2024")
    private Integer nb2024;

    @Column(name = "nb_2023")
    private Integer nb2023;
}
