#!/usr/bin/env python3
"""
Recupere les blasons des clubs qui n'en ont pas encore via l'API gratuite
TheSportsDB (meme source que les 5 grands championnats deja en place, cf.
V5__team_logo.sql), et prepare une migration Flyway avec les logo_path
correspondants.

Matching volontairement strict (cf. discussion avec l'utilisateur) : un logo
n'est retenu automatiquement que si le nom de l'equipe correspond exactement
(apres normalisation - casse, ponctuation, prefixes/suffixes de club types
"FC"/"CF"/"AC"...) a un des noms renvoyes par l'API (nom principal ou noms
alternatifs). Sinon l'equipe est loggee dans un rapport pour revue manuelle,
aucune image n'est telechargee.

Usage :
    python3 fetch_logos.py <fichier .psv id|name|country> <dossier_logos_out> <migration_sql_out> <rapport_out>
"""
import csv
import re
import sys
import time
import unicodedata
import urllib.error
import urllib.parse
import urllib.request
import json

API_BASE = "https://www.thesportsdb.com/api/v1/json/3"
# Cle de test gratuite "3" : limitee (429 observe autour de 30 req/min). On
# reste sous ce seuil, avec un retry/backoff au cas ou une rafale externe
# (autre client sur la meme cle publique) nous fasse quand meme taper le mur.
SLEEP_BETWEEN_CALLS = 2.2
MAX_RETRIES = 5

CLUB_NOISE_RE = re.compile(
    r"\b(FC|CF|AC|AS|SC|SK|FK|NK|BK|CD|CS|UD|RC|CA|OGC|OL|PSG|KF|HNK|MSK|MFK|"
    r"CLUB|FOOTBALL|CLUB DE FUTBOL|CALCIO|ATLETICO|ATHLETIC)\b",
    re.IGNORECASE,
)


def strip_accents(s):
    return "".join(c for c in unicodedata.normalize("NFKD", s) if not unicodedata.combining(c))


def normalize(name):
    n = strip_accents(name).upper()
    n = n.replace(".", " ").replace("-", " ")
    n = CLUB_NOISE_RE.sub(" ", n)
    n = re.sub(r"[^A-Z0-9 ]", " ", n)
    n = re.sub(r"\s+", " ", n).strip()
    return n


def api_search(name):
    url = f"{API_BASE}/searchteams.php?t={urllib.parse.quote(name)}"
    wait = 5
    for attempt in range(1, MAX_RETRIES + 1):
        try:
            with urllib.request.urlopen(url, timeout=15) as resp:
                data = json.loads(resp.read().decode("utf-8"))
            return data.get("teams") or [], None
        except urllib.error.HTTPError as e:
            if e.code == 429 and attempt < MAX_RETRIES:
                print(f"    429 sur '{name}', pause {wait}s (essai {attempt}/{MAX_RETRIES})", file=sys.stderr)
                time.sleep(wait)
                wait *= 2
                continue
            return None, str(e)
        except Exception as e:
            return None, str(e)
    return None, "429 persistant apres retries"


def best_match(query_name, candidates):
    norm_query = normalize(query_name)
    if not norm_query:
        return None
    for c in candidates:
        names = [c.get("strTeam") or ""]
        alt = c.get("strTeamAlternate") or ""
        names += [a.strip() for a in alt.split(",") if a.strip()]
        for n in names:
            if normalize(n) == norm_query:
                return c
    return None


def download(url, dest_path):
    with urllib.request.urlopen(url, timeout=20) as resp:
        data = resp.read()
    with open(dest_path, "wb") as f:
        f.write(data)


def main():
    if len(sys.argv) != 5:
        print("Usage: fetch_logos.py <teams.psv> <logos_out_dir> <migration.sql> <report.txt>", file=sys.stderr)
        sys.exit(1)

    teams_file, logos_dir, migration_out, report_out = sys.argv[1:5]

    teams = []
    with open(teams_file, encoding="utf-8") as f:
        for row in csv.reader(f, delimiter="|"):
            if len(row) != 3:
                continue
            team_id, name, country = row
            teams.append((int(team_id), name, country))

    print(f"{len(teams)} equipes a traiter.", file=sys.stderr)

    matched = []
    skipped = []

    for i, (team_id, name, country) in enumerate(teams, 1):
        candidates, err = api_search(name)
        if err:
            skipped.append((team_id, name, country, f"erreur API: {err}"))
        elif not candidates:
            skipped.append((team_id, name, country, "aucun resultat"))
        else:
            match = best_match(name, candidates)
            if match is None:
                found_names = "; ".join(sorted({c.get("strTeam", "") for c in candidates}))
                skipped.append((team_id, name, country, f"pas de correspondance exacte parmi: {found_names}"))
            else:
                badge = match.get("strBadge")
                if not badge:
                    skipped.append((team_id, name, country, "match trouve mais pas de blason (strBadge vide)"))
                else:
                    try:
                        download(badge, f"{logos_dir}/{team_id}.png")
                        matched.append((team_id, name, country, match.get("strTeam")))
                    except Exception as e:
                        skipped.append((team_id, name, country, f"echec telechargement: {e}"))

        if i % 25 == 0:
            print(f"  ... {i}/{len(teams)} traites ({len(matched)} matches, {len(skipped)} skips)", file=sys.stderr)

        time.sleep(SLEEP_BETWEEN_CALLS)

    with open(migration_out, "w", encoding="utf-8") as f:
        f.write(f"-- Blasons recuperes automatiquement via TheSportsDB (meme source que V5),\n")
        f.write(f"-- matching strict sur le nom : {len(matched)} equipes reconnues avec certitude.\n")
        f.write(f"-- {len(skipped)} equipes non reconnues automatiquement : voir rapport separe.\n\n")
        for team_id, name, country, matched_name in matched:
            f.write(f"UPDATE team SET logo_path = '{team_id}.png' WHERE id = {team_id}; -- {name} ({country}) -> {matched_name}\n")

    with open(report_out, "w", encoding="utf-8") as f:
        f.write(f"{len(skipped)} equipes non reconnues automatiquement (aucun logo telecharge) :\n\n")
        for team_id, name, country, reason in skipped:
            f.write(f"[{team_id}] {name} ({country}) : {reason}\n")

    print(f"\nOK : {len(matched)} logos recuperes, {len(skipped)} a revoir manuellement.", file=sys.stderr)
    print(f"Migration : {migration_out}", file=sys.stderr)
    print(f"Rapport   : {report_out}", file=sys.stderr)


if __name__ == "__main__":
    main()
