package com.charles.footresults.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Rivalite entre deux clubs (derby de la meme ville ou grande rivalite, ex: Marseille - PSG),
 * signalee dans le calendrier. Paire normalisee : team1 a le plus petit id (cf. V139).
 */
@Entity
@Table(name = "rivalry")
@Getter
@Setter
@NoArgsConstructor
public class Rivalry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "team1_id", nullable = false)
    private Team team1;

    @ManyToOne(optional = false)
    @JoinColumn(name = "team2_id", nullable = false)
    private Team team2;

    /** Nom de la rivalite ("Le Classique", "Derby de Lisbonne"...) ; null si sans nom. */
    @Column(length = 80)
    private String label;
}
