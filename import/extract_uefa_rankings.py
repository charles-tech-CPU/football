#!/usr/bin/env python3
"""
Extrait les onglets UEFA (classement des clubs) et PAYS (classement des pays,
coefficient UEFA) du fichier Excel source, et genere les INSERT SQL pour les
tables club_uefa_ranking et country_uefa_ranking.

Les noms de club de l'onglet UEFA sont resolus vers les Team deja en base avec
les memes techniques de normalisation/alias que extract_league_phase.py (ce
sont les memes clubs, vus depuis un onglet different). Quand la resolution
echoue, la ligne est quand meme importee (team_id NULL, club_name garde le
libelle brut de l'onglet) : son classement reste affichable, seul le lien vers
les matchs (donc le recalcul dynamique des points 2027, cf. UefaRankingService
cote backend) ne fonctionnera pas pour ce club.

La colonne "PTS 2027" de l'Excel n'est PAS reprise comme un total de depart a
additionner : elle reflete deja exactement, club par club, les resultats des
matchs de phase de ligue LDC/EL/EC deja en base au moment de l'export (verifie
a la main sur plusieurs clubs). L'additionner en plus des memes matchs
doublerait les points. Elle est donc seulement conservee en colonne
points_2027_imported a titre d'audit/comparaison ; la valeur affichee dans
l'appli est entierement recalculee a la volee par UefaRankingService a partir
des matchs "PHASE DE LIGUE" COMPLETED.

Usage : python3 extract_uefa_rankings.py <fichier.xlsx> <teams.json>
teams.json = sortie de GET /api/teams (tous les clubs deja en base, pour la
resolution de noms).
Affiche les INSERT SQL sur stdout, un rapport de resolution sur stderr.
"""
import json
import sys

import openpyxl

import extract_league_phase as elp
from extract_league_phase import sql_escape

# Alias de noms de club specifiques a l'onglet UEFA : ces clubs n'apparaissent
# dans aucun calendrier national ni tour de qualification deja importe (donc
# jamais rencontres par extract_league_phase.py), leur nom "long" ne peut donc
# se resoudre que par ajout ici. Verifie a la main, un par un, comme pour
# NAME_ALIASES. Fusionne avec NAME_ALIASES avant resolution (voir main()).
UEFA_NAME_ALIASES = {
    "ATLETICO MADRID": "ATL. MADRID",
    "BENFICA LISBONNE": "SL BENFICA",
    "ROME": "AS ROME",
    "SPORTING Portugal": "SPORTING",
    "FERENCVAROS": "FERENCVAROS TC",
    "BRAGA": "SPORTING BRAGA",
    "OLYMPIAKOS LE PIREE": "OLYMPIAKOS",
    "CHAKTHAR DONETSK": "SHAKTHAR",
    "UNION ST GILLES": "UNION SAINT GILLOISE",
    "LEIPZIG": "RB LEIPZIG",
    "LA GANTOISE": "KAA LA GANTOISE",
    "OLYMPIQUE MARSEILLE": "MARSEILLE",
    "ANDERLECHT": "RSC ANDERLECHT",
    "PAPHOS FC": "FC PAFOS",
    "OMONIA NICOSIE": "OMONIA",
    "HAPOEL BEER SHEVA": "H. BEER SHEVA",
    "BRIGHTON": "BRIGHTON &HOVE ALBION",
    "KS EGNATIA RROGOZHINE": "EGNATIA",
    "LINZER ASK": "LASK",
    "TSG HOFFENHEIM": "HOFFENHEIM",
    "SANTA COLOMA UE": "FC SANTA COLOMA",
}

# L'onglet PAYS/UEFA utilise parfois un libelle different de celui deja utilise
# partout ailleurs dans l'appli (competition.country, importe a partir de la
# colonne pays de chaque feuille calendrier national) pour le meme pays --
# comme HOLLANDE (nom d'onglet source) vs "Pays-Bas" (libelle reellement
# present dans le calendrier), deja gere au meme titre dans import_excel.py.
COUNTRY_ALIASES = {
    "HOLLANDE": "Pays-Bas",
    "BIELORUSSIE": "Belarus",
    "ST MARIN": "San Marin",
    "SAN MARIN": "San Marin",
}


def normalize_country(raw):
    if raw is None:
        return None
    key = str(raw).strip().upper()
    if key in COUNTRY_ALIASES:
        return COUNTRY_ALIASES[key]
    return str(raw).strip().title()


def num(v):
    return "NULL" if v is None else v


def main():
    if len(sys.argv) != 3:
        print("Usage: extract_uefa_rankings.py <fichier.xlsx> <teams.json>", file=sys.stderr)
        sys.exit(1)

    path, teams_path = sys.argv[1], sys.argv[2]
    with open(teams_path, encoding="utf-8") as f:
        teams = json.load(f)
    elp.NAME_ALIASES.update(UEFA_NAME_ALIASES)
    resolve = elp.build_resolver(teams)

    wb = openpyxl.load_workbook(path, data_only=True)

    print("-- Classement des clubs (onglet UEFA) et des pays (onglet PAYS), coefficient UEFA.")
    print("-- Genere par import/extract_uefa_rankings.py, ne pas editer a la main.")

    # --- Onglet UEFA : classement des clubs (rang 1 = ligne 2) ---
    ws = wb["UEFA"]
    resolved_count = 0
    unresolved = []
    row = 2
    club_count = 0
    while True:
        rank = ws.cell(row=row, column=1).value
        club = ws.cell(row=row, column=2).value
        if rank is None or club is None:
            break
        club = str(club).strip()
        country_raw = ws.cell(row=row, column=3).value
        cup = ws.cell(row=row, column=4).value
        total = ws.cell(row=row, column=5).value
        pts2027 = ws.cell(row=row, column=6).value
        pts2026 = ws.cell(row=row, column=7).value
        pts2025 = ws.cell(row=row, column=8).value
        pts2024 = ws.cell(row=row, column=9).value
        pts2023 = ws.cell(row=row, column=10).value

        team_name, how = resolve(club)
        if how in ("fuzzy", "alias"):
            team_id_sql = f"(SELECT id FROM team WHERE name = '{sql_escape(team_name)}')"
            resolved_count += 1
        else:
            team_id_sql = "NULL"
            unresolved.append((club, cup, how))

        country = normalize_country(country_raw)
        country_sql = "NULL" if country is None else f"'{sql_escape(country)}'"
        cup_sql = "NULL" if cup is None else f"'{sql_escape(str(cup).strip())}'"

        print(
            "INSERT INTO club_uefa_ranking "
            "(uefa_rank, team_id, club_name, country, current_cup, total, points_2027_imported, points_2026, points_2025, points_2024, points_2023) "
            f"VALUES ({int(rank)}, {team_id_sql}, '{sql_escape(club)}', {country_sql}, {cup_sql}, "
            f"{num(total)}, {num(pts2027)}, {num(pts2026)}, {num(pts2025)}, {num(pts2024)}, {num(pts2023)});"
        )
        club_count += 1
        row += 1

    print(f"-- {club_count} clubs, {resolved_count} resolus vers un Team existant, {len(unresolved)} non resolus.", file=sys.stderr)
    active_unresolved = [u for u in unresolved if u[1]]
    print(f"-- dont {len(active_unresolved)} non resolus actuellement engages en coupe d'Europe (COUPE renseignee) :", file=sys.stderr)
    for club, cup, how in active_unresolved:
        print(f"  NON RESOLU (actif, {cup}): '{club}' ({how})", file=sys.stderr)
    for club, cup, how in unresolved:
        if not cup:
            print(f"  non resolu (inactif): '{club}' ({how})", file=sys.stderr)

    # --- Onglet PAYS : classement des pays (rang 1 = ligne 3, jusqu'a la ligne TOTAL) ---
    ws = wb["PAYS"]
    row = 3
    country_count = 0
    while row <= ws.max_row:
        country_raw = ws.cell(row=row, column=2).value
        if country_raw is None:
            row += 1
            continue
        if str(country_raw).strip().upper() == "TOTAL":
            break
        rank = ws.cell(row=row, column=1).value
        vals = [ws.cell(row=row, column=c).value for c in range(3, 20)]
        (total, pts2027, pts2026, pts2025, pts2024, pts2023,
         ldc_now, el_now, ec_now, ldc_debut, el_debut, ec_debut,
         nb2027, nb2026, nb2025, nb2024, nb2023) = vals

        country = normalize_country(country_raw)
        print(
            "INSERT INTO country_uefa_ranking "
            "(uefa_rank, country, total, points_2027_imported, points_2026, points_2025, points_2024, points_2023, "
            "ldc_now, el_now, ec_now, ldc_debut, el_debut, ec_debut, nb_2027, nb_2026, nb_2025, nb_2024, nb_2023) "
            f"VALUES ({int(rank)}, '{sql_escape(country)}', {num(total)}, {num(pts2027)}, {num(pts2026)}, {num(pts2025)}, {num(pts2024)}, {num(pts2023)}, "
            f"{num(ldc_now)}, {num(el_now)}, {num(ec_now)}, {num(ldc_debut)}, {num(el_debut)}, {num(ec_debut)}, "
            f"{num(nb2027)}, {num(nb2026)}, {num(nb2025)}, {num(nb2024)}, {num(nb2023)});"
        )
        country_count += 1
        row += 1

    print(f"-- {country_count} pays.", file=sys.stderr)


if __name__ == "__main__":
    main()
