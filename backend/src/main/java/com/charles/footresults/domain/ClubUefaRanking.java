package com.charles.footresults.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Une ligne de l'onglet UEFA du fichier Excel source (classement des clubs, coefficient
 * UEFA) : importee telle quelle par import/extract_uefa_rankings.py, a l'exception des
 * points de la saison en cours (voir points2027Imported) qui ne sont jamais affiches
 * directement (cf. UefaRankingService).
 */
@Entity
@Table(name = "club_uefa_ranking")
@Getter
@Setter
@NoArgsConstructor
public class ClubUefaRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uefa_rank", nullable = false)
    private Integer uefaRank;

    /** Null quand le nom de club de l'onglet UEFA n'a pas pu etre resolu vers un Team existant. */
    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    /** Libelle brut de l'onglet UEFA, garde meme quand team est resolu (affichage de secours). */
    @Column(name = "club_name", nullable = false, length = 150)
    private String clubName;

    @Column(length = 60)
    private String country;

    /** Coupe d'Europe jouee cette saison ("LDC"/"EL"/"EC") ; null si le club n'est pas (ou plus) engage. */
    @Column(name = "current_cup", length = 10)
    private String currentCup;

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
