#!/usr/bin/env python3
"""
Convertit le fichier Excel de suivi Foot de Charles en un script SQL
(V2__seed_data.sql) que Flyway joue automatiquement au demarrage du backend.

Sources utilisees (les seules exploitables automatiquement) :
  - "Calendrier championnat" : calendrier a plat de TOUS les championnats
    nationaux (pays, journee, date, heure, club1, club2, score1, score2).
    -> une Competition LEAGUE par pays pour les lignes a journee numerique.
    -> Cette meme feuille contient aussi des lignes journee="CUP" avec de
       vrais noms de club et parfois un score deja joue (coupes nationales
       de plusieurs pays, ex: Norvege, Belarus) : importees comme une
       Competition DOMESTIC_CUP par pays, round_label = "Coupe nationale"
       (pas de tour precis disponible dans la donnee source).
    -> Les lignes journee="BARRAGE"/"B EUR" (playoffs promotion-relegation
       ou qualification europeenne) et les lignes ou club1==club2 sont des
       placeholders ("3E", "9E", "W1", "HF"...) desigant une position au
       classement ou le vainqueur d'un autre match plutot qu'une vraie
       equipe : ignorees, pas exploitables automatiquement.
  - "LDC" / "EL" / "EC" (Ligue des Champions / Europa League / Conference
    League) : seuls les TOURS DE QUALIFICATION ont deja un resultat connu
    (score aller/retour) dans le fichier ; la phase de ligue (36 clubs) et
    les tours a partir des seiziemes de finale sont des templates vides.
    -> une Competition CONTINENTAL_CUP par coupe, seulement les tours de
       qualification avec un score exploitable sont importes (2 matchs par
       confrontation : aller + retour).

PAS importes (donnees absentes ou non exploitables automatiquement) :
  - Coupes nationales par pays a partir des sections HUITIEME/QUART/DEMI/
    FINALE en bas de chaque onglet pays (templates vides, aucune equipe
    renseignee) ; seules les lignes "CUP" de "Calendrier championnat"
    (souvent des tours plus precoces, sans round precis) sont importees.
  - Phase de ligue et tours finaux de LDC/EL/EC (a partir des seiziemes) :
    templates vides ou matrice non fiable a parser automatiquement.
  - Playoffs promotion-relegation / barrages qualification europeenne
    ("BARRAGE", "B EUR") : equipes non determinees (positions au
    classement plutot que noms de clubs).
  - Selections nationales ("Calendrier national", "elim cdm") : hors
    perimetre v1 (decision Charles).

Limite connue : les noms de clubs peuvent differer entre le calendrier
national (ex: "PSG") et les feuilles de coupes d'Europe (ex: "PARIS SAINT
GERMAIN"), sans reconciliation automatique -> possibles doublons d'equipes
a fusionner plus tard via l'appli.

Usage :
    python3 import_excel.py /chemin/vers/FOOT_2027.xlsx > ../backend/src/main/resources/db/migration/V2__seed_data.sql
"""
import re
import sys
from datetime import datetime

import openpyxl

SEASON = 2027
SCORE_RE = re.compile(r"^\s*(\d+)\s*-\s*(\d+)\s*$")
# Jetons-placeholders rencontres dans les lignes BARRAGE / B EUR de "Calendrier
# championnat" : ils designent une position au classement ("3E", "9E") ou le
# vainqueur/perdant d'un autre match ("W1", "L2"), pas une vraie equipe.
PLACEHOLDER_RE = re.compile(r"^(HF|QF|DF|F|\d+E|W\d+|L\d+)$", re.IGNORECASE)

CONTINENTAL_CUPS = {
    "LDC": "Ligue des Champions",
    "EL": "Europa League",
    "EC": "Europa Conference League",
}


def sql_escape(value: str) -> str:
    return value.replace("'", "''")


def parse_time(raw):
    if not raw or not isinstance(raw, str) or "H" not in raw:
        return "NULL"
    try:
        hh, mm = raw.strip().upper().split("H")
        return f"'{int(hh):02d}:{int(mm):02d}:00'"
    except ValueError:
        return "NULL"


class Registry:
    """Dedoublonne equipes et competitions au fil de l'import."""

    def __init__(self):
        self.teams = {}          # nom (upper) -> (nom_affiche, pays)
        self.competitions = {}   # code -> (nom_affiche, type, pays)

    def add_team(self, name, country):
        if not name:
            return None
        name = str(name).strip()
        key = name.upper()
        if key not in self.teams:
            self.teams[key] = (name, country)
        return name

    def add_competition(self, code, name, comp_type, country):
        if code not in self.competitions:
            self.competitions[code] = (name, comp_type, country)


def import_league_calendar(wb, registry, out):
    ws = wb["Calendrier championnat"]
    matches = []
    countries_seen = {}

    for row in range(2, ws.max_row + 1):
        pays = ws.cell(row=row, column=1).value
        journee = ws.cell(row=row, column=2).value
        date = ws.cell(row=row, column=3).value
        horaire = ws.cell(row=row, column=4).value
        club1 = ws.cell(row=row, column=5).value
        club2 = ws.cell(row=row, column=6).value
        s1 = ws.cell(row=row, column=8).value
        s2 = ws.cell(row=row, column=9).value

        # "REPORTE" en date (au lieu d'une vraie date) : match reporte sans
        # nouvelle date connue - exploitable quand meme (contrairement a une
        # date manquante ordinaire, qui elle est rejetee ci-dessous).
        date_reported = isinstance(date, str) and date.strip().upper() == "REPORTE"
        if not pays or not club1 or not club2 or not (isinstance(date, datetime) or date_reported):
            continue
        # Lignes placeholder : match contre soi-meme, ou equipe = position au
        # classement / vainqueur d'un autre match ("BARRAGE", "B EUR") plutot
        # qu'un vrai nom de club. Rien d'exploitable, on ignore.
        if club1 == club2 or PLACEHOLDER_RE.match(str(club1)) or PLACEHOLDER_RE.match(str(club2)):
            continue

        country_key = str(pays).strip().upper()
        display_country = countries_seen.setdefault(country_key, str(pays).strip().title())
        registry.add_team(club1, display_country)
        registry.add_team(club2, display_country)

        if isinstance(journee, (int, float)):
            comp_code = country_key
            registry.add_competition(comp_code, f"{display_country} - Championnat {SEASON}", "LEAGUE", display_country)
            round_label = f"J{int(journee)}"
        elif journee == "CUP":
            comp_code = f"{country_key}-CUP"
            registry.add_competition(comp_code, f"{display_country} - Coupe nationale {SEASON}", "DOMESTIC_CUP", display_country)
            round_label = "Coupe nationale"
        else:
            # "BARRAGE" / "B EUR" restants (playoffs promotion/relegation ou
            # qualification europeenne) : hors perimetre, structure pas assez
            # fiable pour etre importee automatiquement.
            continue

        matches.append({
            "competition_code": comp_code,
            "round_label": round_label,
            "date": None if date_reported else date,
            "time": None if date_reported else horaire,
            "team1": club1,
            "team2": club2,
            "s1": s1,
            "s2": s2,
        })

    out["league_matches"] = matches
    print(f"Calendrier championnat : {len(matches)} matchs, {len(countries_seen)} pays.", file=sys.stderr)


def import_continental_qualifiers(wb, registry, out):
    all_ties = []
    for code, label in CONTINENTAL_CUPS.items():
        ws = wb[code]
        registry.add_competition(code, f"{label} {SEASON}", "CONTINENTAL_CUP", None)

        section = None
        count = 0
        for row in range(1, ws.max_row + 1):
            a = ws.cell(row=row, column=1).value
            b = ws.cell(row=row, column=2).value
            c = ws.cell(row=row, column=3).value
            d = ws.cell(row=row, column=4).value
            f = ws.cell(row=row, column=6).value
            h = ws.cell(row=row, column=8).value
            i = ws.cell(row=row, column=9).value

            if a is None and b is not None and c is None:
                section = str(b).strip()
                continue

            leg1 = SCORE_RE.match(str(d)) if d is not None else None
            leg2 = SCORE_RE.match(str(f)) if f is not None else None
            if not (a and b and h and i and leg1 and leg2):
                continue

            registry.add_team(b, a)
            registry.add_team(h, i)

            all_ties.append({
                "competition_code": code,
                "round_label": section or "Qualification",
                "team1": b,
                "team2": h,
                "leg1": (int(leg1.group(1)), int(leg1.group(2))),
                "leg2": (int(leg2.group(1)), int(leg2.group(2))),
            })
            count += 1
        print(f"{code} : {count} confrontations de qualification importees.", file=sys.stderr)

    out["continental_ties"] = all_ties


def write_sql(registry, league_matches, continental_ties):
    out = sys.stdout

    out.write("-- Genere automatiquement par import/import_excel.py depuis le fichier Excel de Charles.\n")
    out.write(f"-- {len(registry.teams)} equipes, {len(registry.competitions)} competitions, "
              f"{len(league_matches)} matchs de championnat, {len(continental_ties) * 2} matchs de qualification europeenne.\n\n")

    out.write("-- Equipes\n")
    for name, country in registry.teams.values():
        country_sql = "NULL" if not country else f"'{sql_escape(str(country))}'"
        out.write(f"INSERT INTO team (name, country) VALUES ('{sql_escape(name)}', {country_sql});\n")

    out.write("\n-- Competitions\n")
    for code, (name, comp_type, country) in registry.competitions.items():
        country_sql = "NULL" if not country else f"'{sql_escape(str(country))}'"
        out.write(
            f"INSERT INTO competition (code, name, type, country, season) VALUES "
            f"('{sql_escape(code)}', '{sql_escape(name)}', '{comp_type}', {country_sql}, {SEASON});\n"
        )

    out.write("\n-- Matchs de championnat (Calendrier championnat)\n")
    for m in league_matches:
        date_sql = f"'{m['date'].strftime('%Y-%m-%d')}'" if m["date"] is not None else "NULL"
        time_sql = parse_time(m["time"])
        s1 = m["s1"] if isinstance(m["s1"], (int, float)) else None
        s2 = m["s2"] if isinstance(m["s2"], (int, float)) else None
        s1_sql = "NULL" if s1 is None else int(s1)
        s2_sql = "NULL" if s2 is None else int(s2)
        # "REPORTE" en S1/S2 (pas un score numerique) : match reporte, pas
        # simplement "pas encore joue".
        reported = str(m["s1"]).strip().upper() == "REPORTE" or str(m["s2"]).strip().upper() == "REPORTE"
        if reported:
            status = "POSTPONED"
        elif s1 is not None and s2 is not None:
            status = "COMPLETED"
        else:
            status = "SCHEDULED"
        out.write(
            "INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ("
            f"(SELECT id FROM competition WHERE code = '{sql_escape(m['competition_code'])}' AND season = {SEASON}), "
            f"'{sql_escape(m['round_label'])}', {date_sql}, {time_sql}, "
            f"(SELECT id FROM team WHERE name = '{sql_escape(str(m['team1']))}'), "
            f"(SELECT id FROM team WHERE name = '{sql_escape(str(m['team2']))}'), "
            f"{s1_sql}, {s2_sql}, '{status}');\n"
        )

    out.write("\n-- Matchs de qualification europeenne (LDC / EL / EC) : aller puis retour.\n"
               "-- Hypothese de lecture des scores : voir commentaire en tete du script Python.\n")
    for tie in continental_ties:
        comp_sub = f"(SELECT id FROM competition WHERE code = '{sql_escape(tie['competition_code'])}' AND season = {SEASON})"
        team1_sub = f"(SELECT id FROM team WHERE name = '{sql_escape(str(tie['team1']))}')"
        team2_sub = f"(SELECT id FROM team WHERE name = '{sql_escape(str(tie['team2']))}')"
        for leg_name, (g1, g2) in (("Aller", tie["leg1"]), ("Retour", tie["leg2"])):
            round_label = f"{tie['round_label']} - {leg_name}"
            out.write(
                "INSERT INTO match (competition_id, round_label, date, time, team1_id, team2_id, score1, score2, status) VALUES ("
                f"{comp_sub}, '{sql_escape(round_label)}', NULL, NULL, {team1_sub}, {team2_sub}, {g1}, {g2}, 'COMPLETED');\n"
            )


def main():
    if len(sys.argv) != 2:
        print("Usage: import_excel.py <fichier.xlsx>", file=sys.stderr)
        sys.exit(1)

    wb = openpyxl.load_workbook(sys.argv[1], data_only=True)
    registry = Registry()
    out = {}

    import_league_calendar(wb, registry, out)
    import_continental_qualifiers(wb, registry, out)

    write_sql(registry, out["league_matches"], out["continental_ties"])

    print(
        f"OK : {len(registry.teams)} equipes, {len(registry.competitions)} competitions, "
        f"{len(out['league_matches'])} matchs de championnat, {len(out['continental_ties'])} confrontations europeennes.",
        file=sys.stderr,
    )


if __name__ == "__main__":
    main()
