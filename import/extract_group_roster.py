#!/usr/bin/env python3
"""
Extrait uniquement le roster (36 clubs) de la section GROUPE d'une feuille de
coupe d'Europe (LDC/EL/EC), sans se preoccuper de la grille de matchs (utile
pour EL/EC dont la phase de ligue n'a pas encore de calendrier/scores dans le
fichier Excel - seule la liste des clubs qualifies existe).

Genere les INSERT team_competition_status necessaires (et INSERT team pour les
rares clubs absents de la base, cf. NEW_TEAMS).

Usage : python3 extract_group_roster.py <fichier.xlsx> <SHEET> <SEASON> <roster.json>
"""
import sys

import openpyxl

from extract_league_phase import build_resolver, find_team_rows, NEW_TEAMS, sql_escape
import json


def main():
    if len(sys.argv) != 5:
        print("Usage: extract_group_roster.py <fichier.xlsx> <SHEET> <SEASON> <roster.json>", file=sys.stderr)
        sys.exit(1)

    path, sheet_name, season, roster_path = sys.argv[1], sys.argv[2], int(sys.argv[3]), sys.argv[4]
    with open(roster_path, encoding="utf-8") as f:
        roster = json.load(f)
    resolve = build_resolver(roster)

    wb = openpyxl.load_workbook(path, data_only=True)
    ws = wb[sheet_name]

    team_rows, name_col = find_team_rows(ws)
    if not team_rows:
        print("Aucune ligne d'equipe trouvee (section GROUPE introuvable).", file=sys.stderr)
        sys.exit(1)

    rows_sorted = sorted(team_rows.keys())
    print(f"{len(rows_sorted)} equipes trouvees, lignes {rows_sorted[0]}-{rows_sorted[-1]}", file=sys.stderr)

    resolved = []
    for r in rows_sorted:
        raw = team_rows[r]
        name, how = resolve(raw)
        resolved.append((raw, name, how))
        if how not in ("fuzzy", "alias"):
            marker = "NOUVELLE EQUIPE" if raw in NEW_TEAMS else "NON RESOLU"
            print(f"  {marker}: '{raw}'", file=sys.stderr)

    print(f"-- Roster phase de ligue {sheet_name} {season} ({len(resolved)} clubs).")
    for raw, name, how in resolved:
        if raw in NEW_TEAMS:
            country = NEW_TEAMS[raw]
            country_sql = f"'{sql_escape(country)}'" if country else "NULL"
            print(f"INSERT INTO team (name, country) VALUES ('{sql_escape(raw)}', {country_sql});")

    for raw, name, how in resolved:
        team_name = raw if raw in NEW_TEAMS else name
        print(
            "INSERT INTO team_competition_status (competition_id, team_id) VALUES ("
            f"(SELECT id FROM competition WHERE code = '{sheet_name}' AND season = {season}), "
            f"(SELECT id FROM team WHERE name = '{sql_escape(team_name)}')); -- {raw}"
        )


if __name__ == "__main__":
    main()
