#!/usr/bin/env python3
"""
Archives des grandes competitions de selections : toutes les editions (phases finales ET
qualifications) anterieures a celles d'international_history.py, depuis la premiere (Copa
America 1916, Coupe du monde 1930...) -> migrations Flyway V126 et suivantes.

Resultats : meme jeu de donnees qu'international_history.py (github.com/martj42/international_results,
results.csv + shootouts.csv). Trop d'editions pour decrire leurs phases a la main : la structure est
reprise de la page Wikipedia (anglais) de chaque edition, mise en cache localement :
  - groupes : chaque tableau de classement (colonnes Team / Pld / Pts) donne la composition d'un
    groupe, nomme d'apres ses titres de section ("First group stage" > "Group 1") ; les matchs de
    l'edition entre deux membres du groupe y sont ranges (autant de rencontres par paire que le
    nombre de matchs joues l'indique, les suivantes etant a elimination directe) ;
  - tours a elimination directe : deduits des matchs restants (phase finale : finale, puis tour
    precedent de chaque finaliste, etc. ; qualifications : tour preliminaire / barrages) ;
  - tirs au but : score lu dans les feuilles de match ("footballbox") des pages, vainqueur du jeu
    de donnees ; score inconnu si aucune feuille de match ne le donne.
Une edition sans page ou sans tableau exploitable garde ses matchs, sans groupe.

Usage :
  python3 international_archive.py fetch <results.csv> <cache wikipedia>
  python3 international_archive.py sql <results.csv> <shootouts.csv> <cache wikipedia> <dossier migrations>
"""
import csv
import json
import re
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from collections import Counter, defaultdict
from pathlib import Path

from international_history import leg_labels, load_shootouts, sql
from international_history_data import CONF_LABELS, CONFEDERATIONS, TEAM_NAMES
from wikitables import wikitables

FIRST_VERSION = 126
USER_AGENT = 'FootResults-history/1.0 (import local)'

# Selections disparues absentes d'international_history_data (le jeu de donnees donne aux autres
# leur nom actuel : "Russia" pour l'URSS, "Germany" pour la RFA...). La Tchecoslovaquie et la
# Yougoslavie sont comptees avec leur successeur reconnu par la FIFA, la Tchequie et la Serbie (cf. V135).
EXTRA_TEAMS = {
    'Czechoslovakia': ('Tchequie', 'UEFA'), 'German DR': ('RDA', 'UEFA'), 'Saarland': ('Sarre', 'UEFA'),
    'Yugoslavia': ('Serbie', 'UEFA'), 'Vietnam Republic': ('Vietnam du Sud', 'AFC'),
    'Yemen DPR': ('Yemen du Sud', 'AFC'),
}

# Nom Wikipedia (au moment de l'edition) -> nom du jeu de donnees.
WIKI_NAMES = {
    'West Germany': 'Germany', 'Soviet Union': 'Russia', 'CIS': 'Russia', 'FR Yugoslavia': 'Serbia',
    'Serbia and Montenegro': 'Serbia', 'East Germany': 'German DR', 'Saar': 'Saarland', 'Zaire': 'DR Congo',
    'Zaïre': 'DR Congo', 'China PR': 'China', 'Korea Republic': 'South Korea', 'Korea DPR': 'North Korea',
    'IR Iran': 'Iran', 'USA': 'United States', 'Macedonia': 'North Macedonia', 'FYR Macedonia': 'North Macedonia',
    "Côte d'Ivoire": 'Ivory Coast', 'Swaziland': 'Eswatini', 'Burma': 'Myanmar', 'Ceylon': 'Sri Lanka',
    'Western Samoa': 'Samoa', 'Netherlands Antilles': 'Curaçao', 'United Arab Republic': 'Egypt',
    'Rhodesia': 'Zimbabwe', 'Khmer Republic': 'Cambodia', 'Kampuchea': 'Cambodia', 'South Vietnam': 'Vietnam Republic',
    'North Vietnam': 'Vietnam', 'South Yemen': 'Yemen DPR', 'North Yemen': 'Yemen', 'Chinese Taipei': 'Taiwan',
    'Republic of China': 'Taiwan', 'Dutch East Indies': 'Indonesia', 'Upper Volta': 'Burkina Faso',
    'Dahomey': 'Benin', 'Czech Republic': 'Czech Republic', 'Czechia': 'Czech Republic', 'Ireland': 'Republic of Ireland',
    'Irish Free State': 'Republic of Ireland', 'Congo-Kinshasa': 'DR Congo', 'Congo-Léopoldville': 'DR Congo',
    'Congo-Brazzaville': 'Congo', 'Gold Coast': 'Ghana', 'Tanganyika': 'Tanzania', 'Malaya': 'Malaysia',
    'Mandatory Palestine': 'Israel', 'British Guiana': 'Guyana', 'Dutch Guiana': 'Suriname', 'Surinam': 'Suriname',
    'Hong Kong, China': 'Hong Kong', 'Brunei Darussalam': 'Brunei', 'Kyrgyz Republic': 'Kyrgyzstan',
    'Timor-Leste': 'East Timor', 'Cape Verde Islands': 'Cape Verde', 'São Tomé and Príncipe': 'São Tomé and Príncipe',
    'Turkiye': 'Turkey', 'Türkiye': 'Turkey', 'Bosnia-Herzegovina': 'Bosnia and Herzegovina',
}

# Pages Wikipedia d'une edition : la premiere qui existe parmi les candidates (y = annee).
FAMILIES = [
    {'key': 'WC', 'title': 'Coupe du monde', 'file': 'archive_coupe_du_monde', 'before': 2018,
     'finals': {'FIFA World Cup': ('FIFA_WC', 'Coupe du monde {y}')},
     'qualification': 'FIFA World Cup qualification',
     'pages': lambda y: [f'{y} FIFA World Cup', f'{y} FIFA World Cup knockout stage'],
     'qual_pages': lambda y, conf: ([f'{y} FIFA World Cup qualification ({conf})'] if conf else [])
     + [f'{y} FIFA World Cup qualification']},
    {'key': 'EURO', 'title': 'Euro', 'file': 'archive_euro', 'before': 2016,
     'finals': {'UEFA Euro': ('UEFA_EURO', 'Euro {y}')},
     'qualification': 'UEFA Euro qualification', 'qual_code': ('UEFA_EUROQ', 'Qualifications Euro {y}'),
     'pages': lambda y: [f'UEFA Euro {y}', f"{y} European Nations' Cup", f'UEFA Euro {y} knockout stage'],
     'qual_pages': lambda y, conf: [f'UEFA Euro {y} qualifying', f"{y} European Nations' Cup qualifying",
                                    f'UEFA Euro {y} qualification']},
    {'key': 'COPA', 'title': 'Copa America', 'file': 'archive_copa_america', 'before': 2019,
     'finals': {'Copa América': ('CONMEBOL_COPA', 'Copa América {y}')},
     'pages': lambda y: [f'{y} Copa América', f'{y} South American Championship', f'{y} Copa América knockout stage']
     + (['Copa América Centenario', 'Copa América Centenario knockout stage'] if y == 2016 else [])
     + ([f'{y} South American Championship (Argentina)'] if y == 1959 else []),
     # Deux editions en 1959 : Argentine (mars-avril), puis Equateur (decembre).
     'pages_bis': lambda y: [f'{y} South American Championship (Ecuador)']},
    {'key': 'CAN', 'title': "Coupe d'Afrique des nations", 'file': 'archive_can', 'before': 2021,
     'finals': {'African Cup of Nations': ('CAF_AFCON', 'CAN {y}')},
     'qualification': 'African Cup of Nations qualification', 'qual_code': ('CAF_AFCONQ', 'Qualifications CAN {y}'),
     'pages': lambda y: [f'{y} African Cup of Nations', f'{y} Africa Cup of Nations',
                         f'{y} Africa Cup of Nations knockout stage'],
     'qual_pages': lambda y, conf: [f'{y} African Cup of Nations qualification',
                                    f'{y} Africa Cup of Nations qualification']},
    {'key': 'ASIE', 'title': "Coupe d'Asie", 'file': 'archive_coupe_asie', 'before': 2019,
     'finals': {'AFC Asian Cup': ('AFC_ASIANCUP', "Coupe d'Asie {y}")},
     'qualification': 'AFC Asian Cup qualification',
     'qual_code': ('AFC_ASIANCUPQ', "Qualifications Coupe d'Asie {y}"),
     'pages': lambda y: [f'{y} AFC Asian Cup', f'{y} Asian Cup', f'{y} AFC Asian Cup knockout stage'],
     'qual_pages': lambda y, conf: [f'{y} AFC Asian Cup qualification']},
    {'key': 'GOLD', 'title': 'Gold Cup et Championnat CONCACAF', 'file': 'archive_gold_cup', 'before': 2021,
     'finals': {'Gold Cup': ('CONCACAF_GOLDCUP', 'Gold Cup {y}'),
                'CONCACAF Championship': ('CONCACAF_GOLDCUP', 'Championnat CONCACAF {y}')},
     'qualification': ('Gold Cup qualification', 'CONCACAF Championship qualification'),
     'qual_code': ('CONCACAF_GOLDCUPQ', 'Qualifications Gold Cup {y}'),
     'pages': lambda y: [f'{y} CONCACAF Gold Cup', f'{y} CONCACAF Championship', f'{y} CONCACAF Gold Cup knockout stage'],
     'qual_pages': lambda y, conf: [f'{y} CONCACAF Gold Cup qualification', f'{y} CONCACAF Championship qualification']},
]

EDITION_GAP_DAYS = 150


# --- Donnees ----------------------------------------------------------------------------------

def load_results(path):
    with open(path, encoding='utf-8') as f:
        return list(csv.DictReader(f))


def days(date):
    y, m, d = map(int, date.split('-'))
    return y * 372 + m * 31 + d


def confederation(team):
    if team in EXTRA_TEAMS:
        return EXTRA_TEAMS[team][1]
    for conf, members in CONFEDERATIONS.items():
        if team in members:
            return conf
    return None


def french(team):
    if team in EXTRA_TEAMS:
        return EXTRA_TEAMS[team][0]
    return TEAM_NAMES[team]


def finals_editions(results, family):
    """[(annee, suffixe, code, nom, matchs)] des phases finales, regroupees par periode de jeu."""
    editions = []
    for tournament, (code, name) in family['finals'].items():
        matches = sorted((r for r in results if r['tournament'] == tournament), key=lambda r: r['date'])
        clusters = []
        for m in matches:
            if clusters and days(m['date']) - days(clusters[-1][-1]['date']) <= EDITION_GAP_DAYS:
                clusters[-1].append(m)
            else:
                clusters.append([m])
        for c in clusters:
            # Annee du dernier match : Copa America 1937 jouee de decembre 1936 a fevrier 1937.
            editions.append({'year': int(c[-1]['date'][:4]), 'code': code, 'name': name, 'matches': c,
                             'start': c[0]['date']})
    editions.sort(key=lambda e: e['start'])
    # Deux editions la meme annee (Copa America 1959) : la seconde prend le suffixe B.
    seen = Counter()
    for e in editions:
        seen[e['year']] += 1
        e['suffix'] = '' if seen[e['year']] == 1 else 'B'
    return editions


def qualification_matches(results, family, finals):
    """{index de l'edition visee: [matchs]} : chaque match de qualification va a la premiere phase
    finale qui commence apres lui."""
    tournaments = family.get('qualification')
    if not tournaments:
        return {}
    if isinstance(tournaments, str):
        tournaments = (tournaments,)
    by_edition = defaultdict(list)
    for r in results:
        if r['tournament'] not in tournaments:
            continue
        target = next((i for i, e in enumerate(finals) if e['start'] > r['date']), None)
        if target is not None:
            by_edition[target].append(r)
    return by_edition


def split_by_confederation(matches):
    """{confederation ou 'INTERCONF': [matchs]} des qualifications d'une Coupe du monde. La confederation
    d'une equipe pour cette edition est celle de la majorite de ses adversaires (Israel en Asie dans les
    annees 1970, l'Australie en Oceanie avant 2006...)."""
    opponents = defaultdict(Counter)
    for m in matches:
        opponents[m['home_team']][confederation(m['away_team'])] += 1
        opponents[m['away_team']][confederation(m['home_team'])] += 1
    conf_of = {}
    for team, counts in opponents.items():
        own = confederation(team)
        best = max(counts.items(), key=lambda kv: (kv[1], kv[0] == own))[0]
        conf_of[team] = best or own
    out = defaultdict(list)
    for m in matches:
        a, b = conf_of[m['home_team']], conf_of[m['away_team']]
        out[a if a == b else 'INTERCONF'].append(m)
    return out


# --- Wikipedia --------------------------------------------------------------------------------

def cache_file(cache, title):
    return Path(cache) / (re.sub(r'[^A-Za-z0-9]+', '_', title) + '.html')


def download(url, attempts=6):
    req = urllib.request.Request(url, headers={'User-Agent': USER_AGENT})
    for attempt in range(attempts):
        try:
            with urllib.request.urlopen(req) as resp:
                return resp.read()
        except urllib.error.HTTPError as e:
            if e.code == 404:
                return None
            if e.code != 429 or attempt == attempts - 1:
                raise
            time.sleep(20 * (attempt + 1))
    return None


def existing_titles(titles):
    """{titre demande: titre de la page (redirection suivie)} pour les titres qui existent."""
    found = {}
    titles = sorted(set(titles))
    for i in range(0, len(titles), 40):
        chunk = titles[i:i + 40]
        query = urllib.parse.urlencode({'action': 'query', 'titles': '|'.join(chunk), 'redirects': 1,
                                        'format': 'json'})
        data = json.loads(download('https://en.wikipedia.org/w/api.php?' + query))['query']
        normalized = {n['from']: n['to'] for n in data.get('normalized', [])}
        redirects = {r['from']: r['to'] for r in data.get('redirects', [])}
        pages = {p['title'] for p in data['pages'].values() if 'missing' not in p and 'invalid' not in p}
        for t in chunk:
            target = redirects.get(normalized.get(t, t), normalized.get(t, t))
            if target in pages:
                found[t] = target
        time.sleep(1)
    return found


def all_editions(results):
    """[(famille, edition)] de toutes les editions a archiver, qualifications comprises."""
    out = []
    for family in FAMILIES:
        finals = finals_editions(results, family)
        quals = qualification_matches(results, family, finals)
        for i, e in enumerate(finals):
            if e['year'] >= family['before']:
                continue
            key = f"{e['year']}{e['suffix']}"
            out.append((family, {'code': f"{e['code']}_{key}", 'name': e['name'].format(y=e['year']) +
                                 (' (bis)' if e['suffix'] else ''), 'season': e['year'], 'matches': e['matches'],
                                 'finals': True,
                                 'pages': family['pages_bis'](e['year']) if e['suffix'] else family['pages'](e['year'])}))
            q = quals.get(i, [])
            if not q:
                continue
            if family['key'] == 'WC':
                for conf, matches in sorted(split_by_confederation(q).items()):
                    if conf == 'INTERCONF':
                        code, name, pages = f"FIFA_WCQ_{e['year']}", f"Barrages intercontinentaux Coupe du monde {e['year']}", \
                            family['qual_pages'](e['year'], None)
                    else:
                        code = f"{conf}_WCQ_{e['year']}"
                        name = f"Qualifications Coupe du monde {e['year']} - {CONF_LABELS[conf]}"
                        pages = family['qual_pages'](e['year'], conf)
                    out.append((family, {'code': code, 'name': name, 'season': e['year'], 'matches': matches,
                                         'finals': False, 'pages': pages}))
            else:
                code, name = family['qual_code']
                out.append((family, {'code': f'{code}_{key}', 'name': name.format(y=e['year']), 'season': e['year'],
                                     'matches': q, 'finals': False, 'pages': family['qual_pages'](e['year'], None)}))
    return out


def fetch(results_path, cache):
    Path(cache).mkdir(parents=True, exist_ok=True)
    results = load_results(results_path)
    wanted = {t for _, e in all_editions(results) for t in e['pages']}
    found = existing_titles(wanted)
    Path(cache, 'titles.json').write_text(json.dumps(found, ensure_ascii=False, indent=1), encoding='utf-8')
    for title in sorted(set(found.values())):
        path = cache_file(cache, title)
        if path.exists():
            continue
        content = download('https://en.wikipedia.org/api/rest_v1/page/html/'
                           + urllib.parse.quote(title.replace(' ', '_'), safe=''))
        if content:
            path.write_bytes(content)
            print(f'{title} : {len(content)} octets', file=sys.stderr)
        time.sleep(1.5)


def edition_html(cache, titles, edition):
    """HTML des pages existantes de l'edition (dans l'ordre des candidates, sans doublon)."""
    pages = []
    for t in edition['pages']:
        page = titles.get(t)
        if page and page not in pages and cache_file(cache, page).exists():
            pages.append(page)
    return [cache_file(cache, p).read_text(encoding='utf-8') for p in pages]


# --- Groupes ----------------------------------------------------------------------------------

SKIPPED_HEADINGS = re.compile(r'rank|third|second-placed|runners?-up|best|overall|statistic|record|seed|pot\b|'
                              r'draw|coefficient|summary|qualified|all-time|aggregate|standings|comparison|'
                              r'tiebreak|disciplin|fair play|attendance|goalscorer|venue|squad|broadcast', re.I)
ORDINALS = {'First': '1er', 'Second': '2e', 'Third': '3e', 'Fourth': '4e', 'Fifth': '5e', 'Sixth': '6e'}


def translate(heading):
    """Titre de section Wikipedia -> libelle francais (groupe ou phase)."""
    h = heading.strip()
    m = re.fullmatch(r'(?:Group|Pool)\s+(\S+)', h, re.I)
    if m:
        return f'Groupe {m.group(1)}'
    m = re.fullmatch(r'(First|Second|Third|Fourth|Fifth|Sixth) (round|stage|phase)', h, re.I)
    if m:
        return f'{ORDINALS[m.group(1).capitalize()]} tour'
    m = re.fullmatch(r'(First|Second|Third) group stage', h, re.I)
    if m:
        return f"{ {'First': '1re', 'Second': '2e', 'Third': '3e'}[m.group(1).capitalize()]} phase de groupes"
    fixed = {'group stage': 'Phase de groupes', 'final round': 'Tour final', 'final tournament': 'Phase finale',
             'preliminary round': 'Tour préliminaire', 'qualifying round': 'Tour de qualification',
             'group phase': 'Phase de groupes', 'groups': 'Phase de groupes', 'first group phase': '1re phase de groupes',
             'second group phase': '2e phase de groupes', 'final group': 'Groupe final', 'final stage': 'Phase finale',
             'semi-final round': 'Demi-finales'}
    return fixed.get(h.lower(), h)


def clean_wiki_team(text):
    text = re.sub(r'\[[^\]]*\]', '', text)
    while True:
        new = re.sub(r'\s*\([^()]*\)\s*$', '', text).strip()
        if new == text:
            break
        text = new
    text = text.strip(' *†‡§')
    return WIKI_NAMES.get(text, text)


def standings_tables(htmls):
    """[(titres, equipes, matchs par paire, repli)] des tableaux de classement des pages, dans l'ordre.
    repli : tableau sous un titre ecarte ("Standings", "Final standings"), retenu seulement si l'edition
    n'a aucun autre groupe et que ses equipes se sont toutes rencontrees (cf. assign_groups)."""
    found = []
    for html in htmls:
        for t in wikitables(html):
            rows = t['rows']
            if len(rows) < 3:
                continue
            # "Teamvte" : liens v·t·e (voir / discuter / modifier) colles a l'en-tete.
            header = [re.sub(r'\s*v\s*t\s*e$', '', c['text']).strip() for c in rows[0]]
            pld_col = next((i for i, h in enumerate(header) if h in ('Pld', 'P', 'GP', 'MP')), None)
            if pld_col is None or not ({'Pts', 'GF', 'W'} & set(header)):
                continue
            team_col = next((i for i, h in enumerate(header) if h in ('Team', 'Teams', 'Nation', 'Country')), None)
            if team_col is None:
                # Colonne des equipes sans titre, en tete ou juste apres le rang.
                team_col = next((i for i, h in enumerate(header[:pld_col]) if h == ''), None)
            if team_col is None:
                continue
            headings = [h for h in t['headings'] if h]
            if re.search(r'rank|third|second-placed|first-placed|runners?-up|best', ' '.join(headings[-1:]) + (t['caption'] or ''), re.I):
                continue
            fallback = any(SKIPPED_HEADINGS.search(h) for h in headings[-1:]) or bool(SKIPPED_HEADINGS.search(t['caption'] or ''))
            teams, played = [], []
            for r in rows[1:]:
                if len(r) <= max(team_col, pld_col):
                    continue
                teams.append(clean_wiki_team(r[team_col]['text']))
                digits = re.sub(r'\D', '', r[pld_col]['text'])
                if digits:
                    played.append(int(digits))
            if len(teams) >= 2 and played:
                legs = max(1, round(max(played) / (len(teams) - 1)))
                found.append((headings, teams, legs, fallback))
    return found


def assign_groups(edition, tables):
    """{id(match): (round_label, group_name)} pour les matchs de groupe de l'edition."""
    by_pair = defaultdict(list)
    for m in sorted(edition['matches'], key=lambda r: r['date']):
        by_pair[frozenset((m['home_team'], m['away_team']))].append(m)
    teams = {t for m in edition['matches'] for t in (m['home_team'], m['away_team'])}
    groups = []
    for headings, members, legs, fallback in tables:
        known = [t for t in members if t in teams]
        if len(known) >= 2 and not fallback:
            groups.append((headings, known, legs))
    if not groups:
        # Classement unique (championnat a une poule) : seulement si tout le monde s'est rencontre.
        for headings, members, legs, fallback in tables:
            known = [t for t in members if t in teams]
            if fallback and len(known) >= 3 and all(frozenset((a, b)) in by_pair
                                                     for i, a in enumerate(known) for b in known[i + 1:]):
                groups.append((headings[:-1] + ['Groupe unique'], known, legs))
                break
    if not groups:
        return {}
    stages = {tuple(h[:-1]) for h, _, _ in groups}
    assigned = {}
    used_names = Counter()
    for headings, members, legs in groups:
        group_label = translate(headings[-1]) if headings else 'Groupe'
        stage = translate(headings[-2]) if len(headings) >= 2 else None
        if not group_label.startswith('Groupe') and not group_label.startswith('Poule'):
            stage, group_label = group_label, 'Groupe unique'
        name = group_label if len(stages) <= 1 or not stage else f'{stage} - {group_label}'
        used_names[name] += 1
        if used_names[name] > 1:
            name = f'{name} ({used_names[name]})'
        round_label = stage if stage and not stage.startswith('Groupe') else 'Phase de groupes'
        found = []
        for i, a in enumerate(members):
            for b in members[i + 1:]:
                pair_matches = [m for m in by_pair.get(frozenset((a, b)), []) if id(m) not in assigned]
                found.extend(pair_matches[:legs])
        # Moins de la moitie des matchs d'un mini-championnat : pas un groupe (classement final,
        # tableau des barragistes...), ses matchs restent a elimination directe.
        expected = len(members) * (len(members) - 1) // 2 * legs
        if len(found) * 2 < expected:
            continue
        for m in found:
            assigned[id(m)] = (round_label, name)
    return assigned


# --- Elimination directe ----------------------------------------------------------------------

ROUND_NAMES = {1: 'Finale', 2: 'Demi-finales', 4: 'Quarts de finale', 8: '8es de finale', 16: '16es de finale'}


def won_previous(team, match, ko):
    """Vrai si l'equipe a gagne (ou fait nul, faute de tirs au but connus) son dernier match avant celui-ci."""
    before = [o for o in ko if o['date'] < match['date'] and team in (o['home_team'], o['away_team'])]
    if not before:
        return True
    last = before[-1]
    home, away = int(last['home_score']), int(last['away_score'])
    if home == away:
        return True
    return (home > away) == (last['home_team'] == team)


def final_of(ko):
    """Finale = dernier match ; le meme jour que le match pour la 3e place (CAN 1968 et 1996, Coupe
    d'Asie 1972, Gold Cup 1996), celui qui oppose les deux vainqueurs des demi-finales."""
    last_day = [m for m in ko if m['date'] == ko[-1]['date']]
    for m in reversed(last_day):
        if won_previous(m['home_team'], m, ko) and won_previous(m['away_team'], m, ko):
            return m
    return ko[-1]


def finals_ko_labels(ko):
    """Phase finale : finale = dernier match, puis tour precedent de chaque equipe encore en lice."""
    labels = {}
    ko = sorted(ko, key=lambda m: m['date'])
    if not ko:
        return labels
    pair = lambda m: frozenset((m['home_team'], m['away_team']))
    final = final_of(ko)
    current = [final]
    size = 1
    while current:
        name = ROUND_NAMES.get(size, f'Tour à {size * 2} équipes')
        first_date = min(m['date'] for m in current)
        for m in current:
            labels[id(m)] = name
            # Match rejoue (meme affiche quelques jours apres un nul) : meme tour, le 2e marque comme rejoue.
            for other in ko:
                if id(other) not in labels and pair(other) == pair(m) and other['date'] < m['date'] \
                        and days(m['date']) - days(other['date']) <= 7:
                    labels[id(other)] = name
                    labels[id(m)] = f'{name} (match rejoué)'
                    first_date = min(first_date, other['date'])
        previous = []
        for m in current:
            for team in (m['home_team'], m['away_team']):
                before = [o for o in ko if id(o) not in labels and o['date'] < first_date
                          and team in (o['home_team'], o['away_team'])]
                if before:
                    last = before[-1]
                    if all(id(last) != id(p) for p in previous):
                        previous.append(last)
        size *= 2
        current = previous if len(previous) == size else []
    # Match pour la 3e place : entre deux non-finalistes, peu avant la finale.
    finalists = pair(final)
    for m in ko:
        if id(m) not in labels and not (pair(m) & finalists) and 0 <= days(final['date']) - days(m['date']) <= 4:
            labels[id(m)] = 'Match pour la 3e place'
    for m in ko:
        labels.setdefault(id(m), 'Tour préliminaire')
    return labels


def qualification_ko_labels(ko, group_dates):
    labels = {}
    first = min(group_dates) if group_dates else None
    last = max(group_dates) if group_dates else None
    for m in ko:
        if first and m['date'] < first:
            labels[id(m)] = 'Tour préliminaire'
        elif last and m['date'] > last:
            labels[id(m)] = 'Barrages'
        else:
            labels[id(m)] = 'Élimination directe'
    return labels


# --- Tirs au but ------------------------------------------------------------------------------

FOOTBALLBOX = re.compile(r'class="footballbox"')
TAG = re.compile(r'<[^>]+>')


def shootout_scores(htmls):
    """{(date, frozenset(equipes)): {equipe: tirs au but reussis}} lus dans les feuilles de match."""
    scores = {}
    for html in htmls:
        starts = [m.start() for m in FOOTBALLBOX.finditer(html)] + [len(html)]
        for s, e in zip(starts, starts[1:]):
            box = html[s:e]
            date = box_date(box)
            home = re.search(r'<th class="fhome"[^>]*>(.*?)</th>', box, re.S)
            away = re.search(r'<th class="faway"[^>]*>(.*?)</th>', box, re.S)
            if not (home and away):
                continue
            pen = None
            i = box.find('>Penalties<')
            if i >= 0:
                pen = re.search(r'<th[^>]*>\s*(\d+)\s*[–-]\s*(\d+)\s*</th>', box[i:])
            if not pen:
                score = re.search(r'<th class="fscore"[^>]*>(.*?)</th>', box, re.S)
                if score:
                    pen = re.search(r'(\d+)\s*[–-]\s*(\d+)\s*p\b', TAG.sub('', score.group(1)))
            if not pen:
                continue
            h = clean_wiki_team(TAG.sub('', home.group(1)).replace('&nbsp;', ' ').strip())
            a = clean_wiki_team(TAG.sub('', away.group(1)).replace('&nbsp;', ' ').strip())
            # Sans date lisible, la seance est retrouvee par l'affiche seule (cf. build).
            scores[(date, frozenset((h, a)))] = {h: int(pen.group(1)), a: int(pen.group(2))}
    return scores


MONTHS = {m: i + 1 for i, m in enumerate(['January', 'February', 'March', 'April', 'May', 'June', 'July',
                                          'August', 'September', 'October', 'November', 'December'])}


def box_date(box):
    """Date ISO d'une feuille de match : microformat "bday", sinon "10 July 2007" / "July 10, 2007"."""
    iso = re.search(r'class="bday[^"]*">(\d{4}-\d{2}-\d{2})', box)
    if iso:
        return iso.group(1)
    fdate = re.search(r'class="fdate"[^>]*>(.*?)</div>', box, re.S)
    text = TAG.sub(' ', fdate.group(1)) if fdate else ''
    m = re.search(r'(\d{1,2})\s+([A-Z][a-z]+)\s+(\d{4})', text) or re.search(r'([A-Z][a-z]+)\s+(\d{1,2}),\s+(\d{4})', text)
    if not m:
        return None
    day, month, year = (m.group(1), m.group(2), m.group(3)) if m.group(1).isdigit() else (m.group(2), m.group(1), m.group(3))
    if month not in MONTHS:
        return None
    return f'{year}-{MONTHS[month]:02d}-{int(day):02d}'


# --- Migrations -------------------------------------------------------------------------------

def build(edition, htmls, shootouts, report):
    tables = standings_tables(htmls)
    groups = assign_groups(edition, tables)
    ko = [m for m in edition['matches'] if id(m) not in groups]
    if edition['finals']:
        ko_labels = finals_ko_labels(ko)
    else:
        ko_labels = qualification_ko_labels(ko, [m['date'] for m in edition['matches'] if id(m) in groups])
    legs = leg_labels(ko)
    pens = shootout_scores(htmls)
    rows = []
    missing_pens = 0
    for m in sorted(edition['matches'], key=lambda r: r['date']):
        if id(m) in groups:
            round_label, group = groups[id(m)]
        else:
            round_label, group = ko_labels[id(m)], None
            if id(m) in legs:
                round_label = f'{round_label} - {legs[id(m)]}'
        penalties = None
        if (m['date'], m['home_team'], m['away_team']) in shootouts:
            pair = frozenset((m['home_team'], m['away_team']))
            found = pens.get((m['date'], pair)) or pens.get((None, pair))
            if found:
                penalties = (found[m['home_team']], found[m['away_team']])
            else:
                missing_pens += 1
        rows.append({'round': round_label, 'group': group, 'date': m['date'], 'home': french(m['home_team']),
                     'away': french(m['away_team']), 'score1': int(m['home_score']), 'score2': int(m['away_score']),
                     'pen': penalties, 'neutral': m['neutral'] == 'TRUE'})
    report.append(f"{edition['code']} : {len(rows)} matchs, {len(groups)} en groupes "
                  f"({len({g for _, g in groups.values()})} groupes), {len(ko)} a elimination directe"
                  + (f', {missing_pens} tirs au but sans score' if missing_pens else '')
                  + ('' if htmls else ' [aucune page Wikipedia]'))
    return rows


def migration(family, editions, rows_by_code, existing_teams):
    lines = [f"-- Archives {family['title']} (phases finales et qualifications) : import/international_archive.py,",
             '-- resultats du jeu de donnees international_results (github.com/martj42/international_results),',
             '-- groupes et scores des tirs au but repris des pages Wikipedia de chaque edition.', '']
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
        values = []
        for r in rows_by_code[e['code']]:
            pen1, pen2 = r['pen'] if r['pen'] else (None, None)
            values.append(
                f"({comp}, {sql(r['round'])}, {sql(r['group'])}, {sql(r['date'])}, "
                f"(SELECT id FROM team WHERE name = {sql(r['home'])}), (SELECT id FROM team WHERE name = {sql(r['away'])}), "
                f"{r['score1']}, {r['score2']}, {sql(pen1)}, {sql(pen2)}, {sql(r['neutral'])}, 'COMPLETED')")
        lines.append(f"-- {e['name']}")
        lines.append('INSERT INTO match (competition_id, round_label, group_name, date, team1_id, team2_id, score1, '
                     'score2, penalty_score1, penalty_score2, neutral, status) VALUES')
        lines.append(',\n'.join(values) + ';')
        lines.append('')
    return '\n'.join(lines)


def write_sql(results_path, shootouts_path, cache, out_dir):
    results = load_results(results_path)
    shootouts = load_shootouts(shootouts_path)
    titles = json.loads(Path(cache, 'titles.json').read_text(encoding='utf-8'))
    unknown = sorted({t for _, e in all_editions(results) for m in e['matches'] for t in (m['home_team'], m['away_team'])
                      if t not in TEAM_NAMES and t not in EXTRA_TEAMS})
    if unknown:
        raise SystemExit(f'Noms francais manquants : {unknown}')
    report = []
    by_family = defaultdict(list)
    rows_by_code = {}
    for family, edition in all_editions(results):
        rows_by_code[edition['code']] = build(edition, edition_html(cache, titles, edition), shootouts, report)
        by_family[family['key']].append(edition)
    print('\n'.join(report), file=sys.stderr)

    existing_teams = set()
    out_dir = Path(out_dir)
    for path in sorted(out_dir.glob('V*.sql')):
        if int(path.name[1:].split('__', 1)[0]) >= FIRST_VERSION:
            continue
        for line in path.read_text(encoding='utf-8').splitlines():
            if line.startswith('INSERT INTO team (name, country) SELECT '):
                existing_teams.add(line.split("SELECT '", 1)[1].split("', '", 1)[0].replace("''", "'"))
    version = FIRST_VERSION
    for family in FAMILIES:
        editions = by_family.get(family['key'])
        if not editions:
            continue
        path = out_dir / f"V{version}__{family['file']}.sql"
        path.write_text(migration(family, editions, rows_by_code, existing_teams) + '\n', encoding='utf-8')
        total = sum(len(rows_by_code[e['code']]) for e in editions)
        print(f'{path.name} : {len(editions)} editions, {total} matchs', file=sys.stderr)
        version += 1


if __name__ == '__main__':
    if len(sys.argv) >= 4 and sys.argv[1] == 'fetch':
        fetch(sys.argv[2], sys.argv[3])
    elif len(sys.argv) == 6 and sys.argv[1] == 'sql':
        write_sql(*sys.argv[2:])
    else:
        print(__doc__, file=sys.stderr)
        sys.exit(1)
