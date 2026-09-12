#!/usr/bin/env python3
"""
Deuxieme passe sur les equipes non reconnues par fetch_logos.py (matching
strict). Ici on est plus permissif mais on NE TELECHARGE PAS a l'aveugle :
- Match exact (comme fetch_logos.py) mais en filtrant en plus sur
  strSport == "Soccer" (bug corrige : la 1ere passe prenait n'importe quel
  sport, d'ou des faux "aucun match exact" a cause de candidats parasites).
- Si pas de match exact : on cherche un candidat "approximatif" (nom qui
  contient/est contenu par le nom cherche, une fois nettoye des equipes
  reserve/jeunes/femmes) et on le met de cote pour VALIDATION HUMAINE - on ne
  telecharge rien pour ces cas tant que l'utilisateur n'a pas confirme quel
  candidat correspond a quel club.

Sorties :
  - migration.sql : uniquement les matches exacts (nouveaux, sport filtre).
  - approx.json    : liste des candidats approximatifs, pour une page de revue.
  - report.txt     : equipes toujours sans aucun candidat plausible.
"""
import csv
import json
import re
import sys
import time
import unicodedata
import urllib.error
import urllib.parse
import urllib.request

API_BASE = "https://www.thesportsdb.com/api/v1/json/3"
SLEEP_BETWEEN_CALLS = 2.2
MAX_RETRIES = 5

CLUB_NOISE_RE = re.compile(
    r"\b(FC|CF|AC|AS|SC|SK|FK|NK|BK|CD|CS|UD|RC|CA|OGC|OL|PSG|KF|HNK|MSK|MFK|"
    r"CLUB|FOOTBALL|CLUB DE FUTBOL|CALCIO|ATLETICO|ATHLETIC)\b",
    re.IGNORECASE,
)

RESERVE_NOISE_RE = re.compile(
    r"\b(B|II|2|YOUTH|ACADEMY|WOMEN|RESERVES?|U1[0-9]|U2[0-9]|JUNIOR|FEMININ[EA]?)\b",
    re.IGNORECASE,
)


def strip_accents(s):
    return "".join(c for c in unicodedata.normalize("NFKD", s) if not unicodedata.combining(c))


def normalize(name, strip_reserve=False):
    n = strip_accents(name).upper()
    n = n.replace(".", " ").replace("-", " ")
    n = CLUB_NOISE_RE.sub(" ", n)
    if strip_reserve:
        n = RESERVE_NOISE_RE.sub(" ", n)
    n = re.sub(r"[^A-Z0-9 ]", " ", n)
    n = re.sub(r"\s+", " ", n).strip()
    return n


def is_reserve_like(name):
    return bool(RESERVE_NOISE_RE.search(name))


def api_search(name):
    url = f"{API_BASE}/searchteams.php?t={urllib.parse.quote(name)}"
    wait = 5
    for attempt in range(1, MAX_RETRIES + 1):
        try:
            with urllib.request.urlopen(url, timeout=15) as resp:
                data = json.loads(resp.read().decode("utf-8"))
            teams = data.get("teams") or []
            return [t for t in teams if (t.get("strSport") or "").lower() == "soccer"], None
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


def all_names(c):
    names = [c.get("strTeam") or ""]
    alt = c.get("strTeamAlternate") or ""
    names += [a.strip() for a in alt.split(",") if a.strip()]
    return [n for n in names if n]


def exact_match(query_name, candidates):
    norm_query = normalize(query_name)
    if not norm_query:
        return None
    for c in candidates:
        for n in all_names(c):
            if normalize(n) == norm_query:
                return c
    return None


def approx_candidates(query_name, candidates):
    """Candidats dont un nom (nettoye des marqueurs reserve/jeunes/femmes)
    contient ou est contenu dans le nom cherche. Retourne une liste
    (candidat, nom_qui_a_matche) triee par proximite de longueur."""
    norm_query = normalize(query_name, strip_reserve=True)
    if not norm_query:
        return []
    results = []
    for c in candidates:
        for n in all_names(c):
            norm_n = normalize(n, strip_reserve=True)
            if not norm_n:
                continue
            if norm_query == norm_n:
                continue  # deja traite par exact_match (avant nettoyage reserve)
            if norm_query in norm_n or norm_n in norm_query:
                results.append((c, n, abs(len(norm_n) - len(norm_query))))
    results.sort(key=lambda r: r[2])
    # dedoublonne par idTeam, garde la meilleure occurrence de chacun
    seen = set()
    dedup = []
    for c, n, score in results:
        if c["idTeam"] in seen:
            continue
        seen.add(c["idTeam"])
        dedup.append((c, n))
    return dedup


def main():
    if len(sys.argv) != 4:
        print("Usage: resolve_logos_v2.py <teams.psv> <migration_out.sql> <approx_out.json>", file=sys.stderr)
        sys.exit(1)

    teams_file, migration_out, approx_out = sys.argv[1:4]

    teams = []
    with open(teams_file, encoding="utf-8") as f:
        for row in csv.reader(f, delimiter="|"):
            if len(row) != 3:
                continue
            team_id, name, country = row
            teams.append((int(team_id), name, country))

    print(f"{len(teams)} equipes a traiter.", file=sys.stderr)

    exact_matches = []
    approx_list = []
    unresolved = []

    for i, (team_id, name, country) in enumerate(teams, 1):
        candidates, err = api_search(name)
        if err:
            unresolved.append((team_id, name, country, f"erreur API: {err}"))
        elif not candidates:
            unresolved.append((team_id, name, country, "aucun resultat (soccer)"))
        else:
            exact = exact_match(name, candidates)
            if exact:
                badge = exact.get("strBadge")
                if badge:
                    exact_matches.append((team_id, name, country, exact.get("strTeam"), badge))
                else:
                    unresolved.append((team_id, name, country, "match exact mais pas de blason"))
            else:
                cands = approx_candidates(name, candidates)
                cands_with_badge = [(c, n) for c, n in cands if c.get("strBadge")]
                if cands_with_badge:
                    approx_list.append({
                        "teamId": team_id,
                        "ourName": name,
                        "ourCountry": country,
                        "candidates": [
                            {
                                "matchedName": n,
                                "displayName": c.get("strTeam"),
                                "country": c.get("strCountry"),
                                "league": c.get("strLeague"),
                                "badge": c.get("strBadge"),
                                "idTeam": c.get("idTeam"),
                            }
                            for c, n in cands_with_badge[:4]
                        ]
                    })
                else:
                    unresolved.append((team_id, name, country, "aucun candidat plausible"))

        if i % 25 == 0:
            print(f"  ... {i}/{len(teams)} ({len(exact_matches)} exacts, {len(approx_list)} approx, {len(unresolved)} sans piste)", file=sys.stderr)

        time.sleep(SLEEP_BETWEEN_CALLS)

    with open(migration_out, "w", encoding="utf-8") as f:
        f.write(f"-- 2e passe : matches exacts retrouves apres filtrage sport=Soccer ({len(exact_matches)} equipes).\n")
        for team_id, name, country, matched_name, badge in exact_matches:
            f.write(f"UPDATE team SET logo_path = '{team_id}.png' WHERE id = {team_id}; -- {name} ({country}) -> {matched_name}\n")

    with open(approx_out, "w", encoding="utf-8") as f:
        json.dump({
            "exactMatches": [
                {"teamId": t, "ourName": n, "ourCountry": c, "matchedName": mn, "badge": b}
                for t, n, c, mn, b in exact_matches
            ],
            "approx": approx_list,
            "unresolved": [
                {"teamId": t, "ourName": n, "ourCountry": c, "reason": r}
                for t, n, c, r in unresolved
            ]
        }, f, ensure_ascii=False, indent=2)

    print(f"\nOK : {len(exact_matches)} exacts, {len(approx_list)} approximatifs a valider, {len(unresolved)} sans piste.", file=sys.stderr)


if __name__ == "__main__":
    main()
