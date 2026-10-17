#!/usr/bin/env python3
"""
Historique des grandes competitions de selections nationales (phases finales ET qualifications)
-> migrations Flyway, sur le meme principe que l'import Leaguepedia du projet LoL.

Source des resultats : jeu de donnees "international_results" (Mart Jurisoo,
https://github.com/martj42/international_results), compile depuis Wikipedia/RSSSF et mis a
jour en continu : results.csv (un match par ligne, terrain neutre indique) et shootouts.csv
(vainqueur des seances de tirs au but). Le detail des tirs au but (score) n'y figure pas : il
est repris des pages Wikipedia de chaque tournoi (SHOOTOUT_SCORES, dans
international_history_data.py), comme les lettres des groupes (GROUP_LETTERS) et les scores
officiels des matchs donnes sur tapis vert (SCORE_OVERRIDES). Les matchs donnes gagnes sans
avoir ete joues (forfaits) ne figurent pas dans le jeu de donnees, donc pas en base.

Le decoupage en phases (groupes / tours a elimination directe) est decrit edition par edition
dans EDITIONS ; dans une phase de groupes, les groupes sont reconstitues a partir des matchs
(composantes connexes du graphe des rencontres) et le script verifie que chacun est un
mini-championnat complet (chaque paire d'equipes s'est rencontree) avant d'ecrire quoi que ce
soit.

Usage : python3 international_history.py <results.csv> <shootouts.csv> <dossier migrations>
"""
import csv
import sys
from collections import defaultdict
from pathlib import Path

from international_history_data import (
    CONFEDERATIONS,
    EDITIONS,
    FIRST_VERSION,
    GROUP_LETTERS,
    SCORE_OVERRIDES,
    SHOOTOUT_SCORES,
    TEAM_NAMES,
)


def sql(value):
    if value is None:
        return 'NULL'
    if isinstance(value, bool):
        return 'TRUE' if value else 'FALSE'
    if isinstance(value, int):
        return str(value)
    return "'" + str(value).replace("'", "''") + "'"


def load_results(path):
    with open(path, encoding='utf-8') as f:
        return list(csv.DictReader(f))


def load_shootouts(path):
    with open(path, encoding='utf-8') as f:
        return {(r['date'], r['home_team'], r['away_team']): r['winner'] for r in csv.DictReader(f)}


def confederation_of(team):
    for conf, members in CONFEDERATIONS.items():
        if team in members:
            return conf
    raise SystemExit(f'Confederation inconnue pour {team}')


def french(team):
    try:
        return TEAM_NAMES[team]
    except KeyError:
        raise SystemExit(f'Nom francais manquant pour {team}') from None


class UnionFind:
    def __init__(self):
        self.parent = {}

    def find(self, x):
        self.parent.setdefault(x, x)
        while self.parent[x] != x:
            self.parent[x] = self.parent[self.parent[x]]
            x = self.parent[x]
        return x

    def union(self, a, b):
        self.parent[self.find(a)] = self.find(b)


def select_matches(results, edition):
    """Matchs d'une edition : tournoi(s), fenetre de dates, et confederation pour les qualifs CdM."""
    selected = []
    for r in results:
        if r['tournament'] not in edition['tournaments']:
            continue
        if not (edition['start'] <= r['date'] <= edition['end']):
            continue
        window = edition.get('tournament_windows', {}).get(r['tournament'])
        if window and not (window[0] <= r['date'] <= window[1]):
            continue
        conf = edition.get('confederation')
        if conf:
            c1, c2 = confederation_of(r['home_team']), confederation_of(r['away_team'])
            if conf == 'INTERCONF':
                if c1 == c2:
                    continue
            elif not (c1 == conf and c2 == conf):
                continue
        selected.append(r)
    selected.sort(key=lambda r: r['date'])
    return selected


def in_window(phase, date):
    if 'dates' in phase:
        return date in phase['dates']
    return phase['start'] <= date <= phase['end']


def phase_of(match, phases):
    """Phase d'un match : d'abord celles qui listent leurs affiches (pairs), puis par dates."""
    pair = frozenset((match['home_team'], match['away_team']))
    for phase in phases:
        if 'pairs' in phase and pair in phase['pairs'] and in_window(phase, match['date']):
            return phase
    for phase in phases:
        if 'pairs' not in phase and in_window(phase, match['date']):
            return phase
    return None


def group_components(matches):
    uf = UnionFind()
    for m in matches:
        uf.union(m['home_team'], m['away_team'])
    components = defaultdict(set)
    for m in matches:
        components[uf.find(m['home_team'])].update((m['home_team'], m['away_team']))
    return list(components.values())


def check_round_robin(code, phase, teams, matches, errors):
    """Chaque paire du groupe s'est rencontree, sauf forfaits connus (phase['forfeits'], matchs donnes
    gagnes sur tapis vert et absents du jeu de donnees)."""
    phase_label = phase['label']
    pairs = {frozenset(p) for p in phase.get('forfeits', [])}
    pairs |= {frozenset((m['home_team'], m['away_team'])) for m in matches}
    missing = [(a, b) for a in sorted(teams) for b in sorted(teams) if a < b and frozenset((a, b)) not in pairs]
    if missing:
        errors.append(f'{code} / {phase_label} : groupe incomplet {sorted(teams)}, manque {missing[:4]}')


def letter_for(code, phase_label, teams, errors):
    """Nom du groupe : lettre officielle (GROUP_LETTERS : lettre -> une equipe du groupe, ou plusieurs
    separees par '|'), retrouvee par l'equipe representative ; exactement une lettre doit convenir."""
    letters = GROUP_LETTERS.get((code, phase_label))
    if letters is None:
        errors.append(f'{code} / {phase_label} : lettres de groupes manquantes ({sorted(teams)})')
        return None
    found = [letter for letter, members in letters.items() if set(members.split('|')) <= teams]
    if len(found) != 1:
        errors.append(f'{code} / {phase_label} : {len(found)} lettre(s) pour le groupe {sorted(teams)}')
        return None
    return found[0]


def check_letters(edition, errors):
    """Chaque equipe representative de GROUP_LETTERS appartient bien a un groupe de la phase."""
    for phase in edition['phases']:
        for letter, members in GROUP_LETTERS.get((edition['code'], phase['label']), {}).items():
            for team in members.split('|'):
                if team not in TEAM_NAMES:
                    errors.append(f"{edition['code']} / {phase['label']} : equipe inconnue {team} ({letter})")


def leg_labels(matches):
    """Libelle de manche des confrontations a elimination directe : Aller/Retour si deux manches."""
    by_pair = defaultdict(list)
    for m in matches:
        by_pair[frozenset((m['home_team'], m['away_team']))].append(m)
    labels = {}
    for legs in by_pair.values():
        legs.sort(key=lambda m: m['date'])
        if len(legs) == 2:
            labels[id(legs[0])] = 'Aller'
            labels[id(legs[1])] = 'Retour'
    return labels


def build_edition(results, shootouts, edition, errors):
    code = edition['code']
    matches = select_matches(results, edition)
    if not matches:
        errors.append(f'{code} : aucun match')
        return []
    rows = []
    by_phase = defaultdict(list)
    for m in matches:
        phase = phase_of(m, edition['phases'])
        if phase is None:
            errors.append(f"{code} : match hors phase {m['date']} {m['home_team']}-{m['away_team']}")
            continue
        by_phase[phase['label']].append(m)

    for phase in edition['phases']:
        phase_matches = by_phase.get(phase['label'], [])
        if not phase_matches:
            errors.append(f"{code} / {phase['label']} : aucun match")
            continue
        if phase.get('expected') is not None and len(phase_matches) != phase['expected']:
            errors.append(f"{code} / {phase['label']} : {len(phase_matches)} matchs, {phase['expected']} attendus")
        group_of = {}
        if phase['kind'] == 'groups':
            if phase.get('single_table'):
                group_of = {id(m): phase.get('group', phase['label']) for m in phase_matches}
                teams = {t for m in phase_matches for t in (m['home_team'], m['away_team'])}
                if not phase.get('no_round_robin'):
                    check_round_robin(code, phase, teams, phase_matches, errors)
            else:
                for teams in group_components(phase_matches):
                    in_group = [m for m in phase_matches if m['home_team'] in teams]
                    if not phase.get('no_round_robin'):
                        check_round_robin(code, phase, teams, in_group, errors)
                    letter = letter_for(code, phase['label'], teams, errors)
                    prefix = phase.get('group_prefix', '')
                    if letter is None or phase.get('raw_names'):
                        name = letter
                    else:
                        name = f'{prefix}Groupe {letter}'
                    for m in in_group:
                        group_of[id(m)] = name
        legs = leg_labels(phase_matches) if phase['kind'] == 'ko' else {}
        for m in phase_matches:
            round_label = phase['label']
            if id(m) in legs:
                round_label = f"{phase['label']} - {legs[id(m)]}"
            penalties = None
            winner = shootouts.get((m['date'], m['home_team'], m['away_team']))
            if winner is not None:
                if m['home_score'] != m['away_score'] and legs.get(id(m)) != 'Retour':
                    errors.append(f"{code} : tirs au but sur un match gagne {m['date']} {m['home_team']}")
                key = (m['date'], m['home_team'], m['away_team'])
                penalties = SHOOTOUT_SCORES.get(key)
                if penalties is None:
                    errors.append(f"{code} : score des tirs au but manquant {key} vainqueur {winner}")
                elif (penalties[0] > penalties[1]) != (winner == m['home_team']):
                    errors.append(f'{code} : tirs au but incoherents {key} {penalties} vainqueur {winner}')
            score = SCORE_OVERRIDES.get((m['date'], m['home_team'], m['away_team']),
                                        (int(m['home_score']), int(m['away_score'])))
            rows.append({
                'code': code,
                'round': round_label,
                'group': group_of.get(id(m)),
                'date': m['date'],
                'home': french(m['home_team']),
                'away': french(m['away_team']),
                'score1': score[0],
                'score2': score[1],
                'pen': penalties,
                'neutral': m['neutral'] == 'TRUE',
            })
    expected = edition.get('expected')
    if expected is not None and len(rows) != expected:
        errors.append(f'{code} : {len(rows)} matchs, {expected} attendus')
    return rows


def migration(family, editions, rows_by_code, existing_teams):
    lines = [f"-- {family['title']} : historique importe par import/international_history.py, a partir du",
             "-- jeu de donnees international_results (github.com/martj42/international_results, compile depuis",
             "-- Wikipedia/RSSSF) ; lettres des groupes et scores des tirs au but repris de Wikipedia.",
             "-- Groupe porte par chaque match (match.group_name) : une equipe peut jouer plusieurs phases de",
             "-- groupes d'une meme competition (qualifications en plusieurs tours), et les matchs a elimination",
             "-- directe (group_name nul) ne comptent dans aucun classement de groupe.",
             '']
    new_teams = sorted({t for e in editions for r in rows_by_code[e['code']] for t in (r['home'], r['away'])}
                       - existing_teams)
    for team in new_teams:
        lines.append(f"INSERT INTO team (name, country) SELECT {sql(team)}, {sql(team)} "
                     f"WHERE NOT EXISTS (SELECT 1 FROM team WHERE name = {sql(team)});")
        existing_teams.add(team)
    if new_teams:
        lines.append('')
    for e in editions:
        lines.append(f"INSERT INTO competition (code, name, type, country, season) VALUES "
                     f"({sql(e['code'])}, {sql(e['name'])}, 'INTERNATIONAL', NULL, {e['season']});")
    lines.append('')
    for e in editions:
        comp = f"(SELECT id FROM competition WHERE code = {sql(e['code'])})"
        for r in rows_by_code[e['code']]:
            pen1, pen2 = r['pen'] if r['pen'] else (None, None)
            lines.append(
                'INSERT INTO match (competition_id, round_label, group_name, date, team1_id, team2_id, score1, score2, '
                'penalty_score1, penalty_score2, neutral, status) VALUES '
                f"({comp}, {sql(r['round'])}, {sql(r['group'])}, {sql(r['date'])}, "
                f"(SELECT id FROM team WHERE name = {sql(r['home'])}), (SELECT id FROM team WHERE name = {sql(r['away'])}), "
                f"{r['score1']}, {r['score2']}, {sql(pen1)}, {sql(pen2)}, {sql(r['neutral'])}, 'COMPLETED');")
        lines.append('')
    return '\n'.join(lines)


def main():
    if len(sys.argv) != 4:
        print(__doc__, file=sys.stderr)
        sys.exit(1)
    results = load_results(sys.argv[1])
    shootouts = load_shootouts(sys.argv[2])
    out_dir = Path(sys.argv[3])

    errors = []
    rows_by_code = {}
    for family in EDITIONS:
        for e in family['editions']:
            check_letters(e, errors)
            rows_by_code[e['code']] = build_edition(results, shootouts, e, errors)
    played = {(r['date'], r['home_team'], r['away_team']) for r in results}
    errors += [f'Score officiel sans match correspondant : {key}' for key in SCORE_OVERRIDES if key not in played]
    if errors:
        print('\n'.join(errors), file=sys.stderr)
        print(f'{len(errors)} probleme(s), aucune migration ecrite.', file=sys.stderr)
        sys.exit(2)

    # Selections deja creees par les migrations precedentes (V105, V107-V109).
    existing_teams = set()
    for path in sorted(out_dir.glob('V*.sql')):
        if int(path.name[1:].split('__', 1)[0]) >= FIRST_VERSION:
            continue
        for line in path.read_text(encoding='utf-8').splitlines():
            if line.startswith('INSERT INTO team (name, country) SELECT '):
                existing_teams.add(line.split("SELECT '", 1)[1].split("', '", 1)[0].replace("''", "'"))

    version = FIRST_VERSION
    for family in EDITIONS:
        path = out_dir / f"V{version}__{family['file']}.sql"
        path.write_text(migration(family, family['editions'], rows_by_code, existing_teams) + '\n', encoding='utf-8')
        total = sum(len(rows_by_code[e['code']]) for e in family['editions'])
        print(f'{path.name} : {total} matchs', file=sys.stderr)
        version += 1


if __name__ == '__main__':
    main()
