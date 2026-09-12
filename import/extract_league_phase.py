#!/usr/bin/env python3
"""
Extrait la phase de ligue (36 clubs) d'une feuille de coupe d'Europe
(LDC/EL/EC) du fichier Excel source : une grille croisee ou la ligne R
represente l'equipe qui recoit, la colonne (R+11) son propre nom (donc
colonne C => equipe a domicile de la ligne C-11), chaque cellule [row, col]
donnant soit un score "X-Y" (match joue, row=domicile), soit une date/heure
(programme), soit "J/M" (jour/mois sans annee -> saison se terminant en
janvier de l'annee suivante, comme identifie pour LDC en V8).

Les noms de clubs de cette feuille sont parfois plus "longs" que le nom deja
en base (import calendrier national ou tour de qualification anterieur) :
resolus par normalisation (accents/ponctuation/sigles club) puis, si
necessaire, par un alias explicite verifie a la main (cf. NAME_ALIASES).

Usage : python3 extract_league_phase.py <fichier.xlsx> <SHEET> <SEASON> <roster.json>
roster.json = sortie de GET /api/teams?competitionId=X (equipes deja connues
de cette competition, incluant celles des tours de qualification).
Affiche les INSERT SQL sur stdout, un rapport sur stderr.
"""
import json
import re
import sys
import unicodedata
from datetime import datetime

import openpyxl

SCORE_RE = re.compile(r"^\s*(\d+)\s*-\s*(\d+)\s*$")
SHORT_DATE_RE = re.compile(r"^\s*(\d{1,2})\s*/\s*(\d{1,2})\s*$")

CLUB_NOISE_RE = re.compile(
    r"\b(FC|CF|AC|AS|SC|SK|FK|NK|BK|CD|CS|UD|RC|CA|OGC|OL|PSG|KF|HNK|MSK|MFK|CP|"
    r"CLUB|FOOTBALL|CLUB DE FUTBOL|CALCIO|ATLETICO|ATHLETIC)\b",
    re.IGNORECASE,
)

# Nom "long" (feuille Excel) -> nom deja existant en base, pour les cas que la
# simple normalisation (accents/ponctuation/sigles) ne suffit pas a rapprocher
# (abreviation opaque type PSG, ou orthographe differente type Bruges/Brugge).
# Verifie a la main pour chaque club concerne, comme pour V6 (LDC).
NAME_ALIASES = {
    "PARIS SAINT GERMAIN": "PSG",
    "INTER MILAN": "INTER",
    "ATLETICO DE MADRID": "ATL. MADRID",
    "BORUSSIA DORTMUND": "DORTMUND",
    "MANCHESTER UNITED": "MANCHESTER UTD",
    "CLUB BRUGES": "CLUB BRUGGE",
    "PSV EINDHOVEN": "PSV",
    "FEYENOORD ROTTERDAM": "FEYENOORD",
    "LILLE OSC": "LILLE",
    "SSC NAPLES": "NAPLES",
    "VFB STUTTGART": "STUTTGART",
    "COME 1907": "COMO",
    "REAL BETIS": "BETIS",
    "JUVENTUS TURIN": "JUVENTUS",
    "AZ ALKMAAR": "ALKMAAR",
    "OLYMPIQUE DE MARSEILLE": "MARSEILLE",
    "FERENCVATOS": "FERENCVAROS TC",
    "CELTIC": "CELTIC GLASGOW",
    "STADE RENNAIS": "RENNES",
    "BESIKTAS": "BESIKTAS JK",
    "SAINT TROND": "SAINT TROND",
    "HEART OF MIDLOTHIAN": "HEARTS OF MIDDLOTHIAN",
    "NORDSJAEELAND": "NORDSJAELLAND",
    "MJALLBY": "MJALLBY AIF",
    "EGNATIA RROGOZHINE": "EGANTIA RROGOZHINE",
}

# Clubs absents de la base (jamais rencontres dans un tour de qualification ni
# un championnat national importe) : a creer avant de pouvoir les referencer.
NEW_TEAMS = {
    "TORREENSE": "Portugal",
}


def strip_accents(s):
    return "".join(c for c in unicodedata.normalize("NFKD", s) if not unicodedata.combining(c))


def normalize(name):
    n = strip_accents(name).upper()
    n = n.replace(".", " ").replace("-", " ")
    n = CLUB_NOISE_RE.sub(" ", n)
    n = re.sub(r"[^A-Z0-9 ]", " ", n)
    n = re.sub(r"\s+", " ", n).strip()
    return n


def build_resolver(roster):
    by_norm = {}
    for t in roster:
        by_norm.setdefault(normalize(t["name"]), []).append(t["name"])

    def resolve(name):
        if name in NAME_ALIASES:
            return NAME_ALIASES[name], "alias"
        matches = by_norm.get(normalize(name), [])
        if len(matches) == 1:
            return matches[0], "fuzzy"
        if len(matches) > 1:
            return name, f"AMBIGU parmi {matches}"
        return name, "NON RESOLU"

    return resolve


def sql_escape(v):
    return str(v).replace("'", "''")


def find_team_rows(ws, max_scan_row=60):
    """Localise la section 'GROUPE' (36 clubs) : la position de la colonne varie
    d'une feuille a l'autre (LDC/EL: col 12, EC: col 14, plus de colonnes de
    qualification avant), donc on la detecte plutot que de la fixer en dur."""
    started = False
    start_row = None
    name_col = None
    for row in range(1, max_scan_row + 1):
        for col in range(1, 20):
            if ws.cell(row=row, column=col).value == "GROUPE":
                started = True
                start_row = row + 1
                name_col = col
                break
        if started:
            break
    if start_row is None:
        return {}, None

    rows = {}
    row = start_row
    while row <= ws.max_row:
        name = ws.cell(row=row, column=name_col).value
        if name and str(name).strip() not in ("CLUB",):
            rows[row] = str(name).strip()
        elif rows:
            break
        row += 1
    return rows, name_col


def main():
    if len(sys.argv) != 5:
        print("Usage: extract_league_phase.py <fichier.xlsx> <SHEET> <SEASON> <roster.json>", file=sys.stderr)
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

    resolved_names = {}
    for r in rows_sorted:
        name, how = resolve(team_rows[r])
        resolved_names[r] = name
        if how != "fuzzy" and how != "alias":
            print(f"  RESOLUTION '{team_rows[r]}' : {how}", file=sys.stderr)

    # La grille croisee (colonnes = equipes a domicile pour la ligne du dessus, dans
    # le meme ordre que les lignes du tableau GROUPE) commence juste au-dessus de la
    # 1ere ligne d'equipe. La position de la colonne varie d'une feuille a l'autre
    # (cf. find_team_rows), donc on detecte les colonnes non vides dans l'ordre
    # plutot que de fixer un decalage arithmetique en dur.
    header_row = rows_sorted[0] - 1
    header_cols = []
    col = name_col + 1
    max_col = name_col + 5 + len(rows_sorted) * 2
    while col <= max_col and len(header_cols) < len(rows_sorted):
        if ws.cell(row=header_row, column=col).value is not None:
            header_cols.append(col)
        col += 1
    if len(header_cols) != len(rows_sorted):
        print(f"ATTENTION : {len(header_cols)} colonnes d'en-tete trouvees pour {len(rows_sorted)} equipes.", file=sys.stderr)

    col_to_row = {header_cols[i]: rows_sorted[i] for i in range(len(header_cols))}

    matches = []
    unparsed = []
    for home_row in rows_sorted:
        home_name = resolved_names[home_row]
        for col, away_row in col_to_row.items():
            if away_row == home_row:
                continue
            away_name = resolved_names[away_row]
            val = ws.cell(row=home_row, column=col).value
            if val is None:
                continue
            if isinstance(val, str) and SCORE_RE.match(val):
                m = SCORE_RE.match(val)
                matches.append((home_name, away_name, None, int(m.group(1)), int(m.group(2)), "COMPLETED"))
            elif isinstance(val, datetime):
                d = val.date()
                if d.month == 1:
                    d = d.replace(year=season)
                matches.append((home_name, away_name, d.isoformat(), None, None, "SCHEDULED"))
            elif isinstance(val, str) and SHORT_DATE_RE.match(val):
                m = SHORT_DATE_RE.match(val)
                day, month = int(m.group(1)), int(m.group(2))
                year = season if month == 1 else season - 1
                matches.append((home_name, away_name, f"{year:04d}-{month:02d}-{day:02d}", None, None, "SCHEDULED"))
            else:
                unparsed.append((home_name, away_name, col, repr(val)))

    print(f"{len(matches)} matchs extraits, {len(unparsed)} valeurs non reconnues", file=sys.stderr)
    for u in unparsed:
        print("  NON RECONNU:", u, file=sys.stderr)

    print(f"-- Phase de ligue {sheet_name} {season} : {len(matches)} matchs extraits automatiquement")
    print(f"-- depuis la grille croisee de la feuille Excel '{sheet_name}' (voir extract_league_phase.py).")
    for home, away, date, s1, s2, status in matches:
        date_sql = f"'{date}'" if date else "NULL"
        s1_sql = "NULL" if s1 is None else s1
        s2_sql = "NULL" if s2 is None else s2
        print(
            "INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ("
            f"(SELECT id FROM competition WHERE code = '{sheet_name}' AND season = {season}), "
            f"'PHASE DE LIGUE', {date_sql}, NULL, "
            f"(SELECT id FROM team WHERE name = '{sql_escape(home)}'), "
            f"(SELECT id FROM team WHERE name = '{sql_escape(away)}'), "
            f"{s1_sql}, {s2_sql}, '{status}'); -- {home} vs {away}"
        )


if __name__ == "__main__":
    main()
