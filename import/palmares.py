"""Palmares historique des championnats, coupes nationales et coupes d'Europe (onglet Palmares).

Source : Wikipedia en anglais, une page par competition (listes des champions, des finales de
coupe), lue via l'API REST (HTML) et mise en cache localement. Chaque page est decoupee en
tableaux (wikitables.py) ; on garde les tableaux dont l'en-tete a une colonne saison/annee et une
colonne vainqueur, plus le 2e (finaliste / runners-up) et le 3e quand la page les donne.

Usage :
  python3 palmares.py fetch <cache>                    telecharge les pages manquantes
  python3 palmares.py report <cache> [scope]           resume par competition (verification)
  python3 palmares.py sql <cache> <clubs.txt> <migration.sql>   genere la migration Flyway
  python3 palmares.py unmatched <cache> <clubs.txt>             clubs de l'appli sans correspondance

<clubs.txt> : clubs de l'appli, une ligne "nom|pays" (export psql de la table team) ; sert a relier
chaque nom du palmares a son club (logo).
"""
import json
import re
import sys
import time
import unicodedata
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

from wikitables import wikitables

USER_AGENT = 'FootResults-palmares/1.0 (import local)'

# Pays (libelle de l'appli) -> (page des champions, page de la coupe). Plusieurs pages possibles
# (liste separee par " + ") quand l'historique est reparti sur plusieurs articles ; "Page#0,2"
# ne garde que ces tableaux de la page (ex : sans les championnats de RDA ni des federations
# ouvrieres pour l'Allemagne).
COUNTRIES = {
    'Albanie': ('Kategoria Superiore', 'Albanian Cup'),
    'Allemagne': ('List of German football champions#0,2,3,6', 'List of DFB-Pokal finals'),
    'Andorre': ('Primera Divisió', 'Copa Constitució'),
    'Angleterre': ('List of English football champions', 'List of FA Cup finals'),
    'Armenie': ('Armenian Premier League', 'Armenian Cup'),
    'Autriche': ('List of Austrian football champions', 'Austrian Cup'),
    'Azerbaidjan': ('Azerbaijan Premier League', 'Azerbaijan Cup'),
    'Belarus': ('Belarusian Premier League', 'Belarusian Cup'),
    'Belgique': ('List of Belgian football champions', 'List of Belgian Cup finals'),
    'Bosnie': ('Premier League of Bosnia and Herzegovina', 'Bosnia and Herzegovina Football Cup'),
    'Bulgarie': ('List of Bulgarian football champions', 'fr:Coupe de Bulgarie de football'),
    'Chypre': ('Cypriot First Division', 'Cypriot Cup'),
    'Croatie': ('Croatian Football League', 'Croatian Football Cup'),
    'Danemark': ('List of Danish football champions', 'Danish Cup'),
    'Ecosse': ('List of Scottish football champions', 'List of Scottish Cup finals'),
    'Eire': ('League of Ireland', 'FAI Cup'),
    'Espagne': ('List of Spanish football champions', 'List of Copa del Rey finals'),
    'Estonie': ('Meistriliiga', 'Estonian Cup'),
    'Finlande': ('List of Finnish football champions', 'Finnish Cup'),
    'France': ('List of French football champions', 'List of Coupe de France finals'),
    'Galles': ('Cymru Premier', 'List of Welsh Cup finals'),
    'Georgie': ('Erovnuli Liga', 'Georgian Cup'),
    'Gibraltar': ('Gibraltar Football League + Gibraltar Premier Division', 'fr:Coupe de Gibraltar de football'),
    'Grece': ('List of Greek football champions', 'Greek Football Cup'),
    'Hongrie': ('List of Hungarian football champions', 'Magyar Kupa'),
    'Iles Feroe': ('Faroe Islands Premier League', 'Faroe Islands Cup'),
    'Irlande Nord': ("fr:Championnat d'Irlande du Nord de football", 'Irish Cup'),
    'Islande': ("fr:Championnat d'Islande de football", "Icelandic Men's Football Cup"),
    'Israel': ("List of Israeli football champions + fr:Championnat d'Israël de football", "fr:Coupe d'Israël de football"),
    'Italie': ('List of Italian football champions', 'List of Coppa Italia finals'),
    'Kazakhstan': ('Kazakhstan Premier League', 'Kazakhstan Cup'),
    'Kosovo': ('Football Superleague of Kosovo', 'Kosovar Cup'),
    'Lettonie': ('Latvian Higher League', 'Latvian Football Cup'),
    'Lituanie': ('A Lyga', 'Lithuanian Football Cup'),
    'Luxembourg': ('Luxembourg National Division', 'Luxembourg Cup'),
    'Macedoine': ('Macedonian First Football League', 'Macedonian Football Cup'),
    'Malte': ('List of Maltese football champions', 'Maltese FA Trophy'),
    'Moldavie': ('Moldovan Super Liga', 'Moldovan Cup'),
    'Montenegro': ('Montenegrin First League', 'Montenegrin Cup'),
    'Norvege': ('List of Norwegian football champions', 'Norwegian Football Cup'),
    'Pays-Bas': ('List of Dutch football champions', 'KNVB Cup'),
    'Pologne': ('List of Polish football champions', 'Polish Cup'),
    'Portugal': ('List of Portuguese football champions', 'Taça de Portugal'),
    'Roumanie': ('List of Romanian football champions', 'Cupa României'),
    'Russie': ('List of Russian football champions', 'Russian Cup (football)'),
    'San Marin': ('Campionato Sammarinese di Calcio', 'Coppa Titano'),
    'Serbie': ('List of Serbian football champions', 'Serbian Cup'),
    'Slovaquie': ('Slovak First Football League', 'Slovak Cup'),
    'Slovenie': ('Slovenian PrvaLiga', 'Slovenian Football Cup'),
    'Suede': ('List of Swedish football champions', 'Svenska Cupen'),
    'Suisse': ('List of Swiss football champions', 'Swiss Cup'),
    'Tchequie': ('List of Czech football champions', 'Czech Cup'),
    'Turquie': ('List of Turkish football champions', 'fr:Coupe de Turquie de football'),
    'Ukraine': ('List of Ukrainian football champions', 'Ukrainian Cup'),
}

EUROPE = 'Europe'
EUROPEAN_CUPS = {
    'LDC': ('Ligue des Champions', 'List of European Cup and UEFA Champions League finals'),
    'EL': ('Europa League', 'List of UEFA Cup and Europa League finals'),
    'ECL': ('Conference League', 'List of UEFA Conference League finals'),
    'C2': ("Coupe des coupes", "List of UEFA Cup Winners' Cup finals"),
    # Finales de la Coupe internationale (1961-1967) puis de la Coupe Intertoto UEFA (1995-2008, plusieurs
    # vainqueurs par an : un par finale, puis le vainqueur "outright" de 2006 a 2008).
    'INTERTOTO': ('Coupe Intertoto', 'UEFA Intertoto Cup#0,3,4'),
}
MULTI_WINNER_KINDS = {'INTERTOTO'}

# Drapeau Wikipedia ("Flag_of_X.svg") -> pays de l'appli ; les pays disparus gardent un libelle francais.
NATIONS = {
    'Albania': 'Albanie', 'Germany': 'Allemagne', 'West Germany': 'Allemagne', 'Andorra': 'Andorre',
    'England': 'Angleterre', 'Armenia': 'Armenie', 'Austria': 'Autriche', 'Azerbaijan': 'Azerbaidjan',
    'Belarus': 'Belarus', 'Belgium': 'Belgique', 'Bosnia and Herzegovina': 'Bosnie', 'Bulgaria': 'Bulgarie',
    'Cyprus': 'Chypre', 'Croatia': 'Croatie', 'Denmark': 'Danemark', 'Scotland': 'Ecosse', 'Ireland': 'Eire',
    'the Republic of Ireland': 'Eire', 'Spain': 'Espagne', 'Estonia': 'Estonie', 'Finland': 'Finlande',
    'France': 'France', 'Wales': 'Galles', 'Wales (1959–present)': 'Galles', 'Georgia': 'Georgie',
    'Gibraltar': 'Gibraltar', 'Greece': 'Grece', 'Hungary': 'Hongrie', 'the Faroe Islands': 'Iles Feroe',
    'Northern Ireland': 'Irlande Nord', 'Iceland': 'Islande', 'Israel': 'Israel', 'Italy': 'Italie',
    'Kazakhstan': 'Kazakhstan', 'Kosovo': 'Kosovo', 'Latvia': 'Lettonie', 'Lithuania': 'Lituanie',
    'Luxembourg': 'Luxembourg', 'North Macedonia': 'Macedoine', 'Malta': 'Malte', 'Moldova': 'Moldavie',
    'Montenegro': 'Montenegro', 'Norway': 'Norvege', 'the Netherlands': 'Pays-Bas', 'Poland': 'Pologne',
    'Portugal': 'Portugal', 'Romania': 'Roumanie', 'Russia': 'Russie', 'San Marino': 'San Marin',
    'Serbia': 'Serbie', 'Slovakia': 'Slovaquie', 'Slovenia': 'Slovenie', 'Sweden': 'Suede',
    'Switzerland': 'Suisse', 'Switzerland (Pantone)': 'Suisse', 'the Czech Republic': 'Tchequie',
    'Turkey': 'Turquie', 'Ukraine': 'Ukraine', 'the Soviet Union': 'URSS', 'SFR Yugoslavia': 'Yougoslavie',
    'Yugoslavia (1946-1992)': 'Yougoslavie', 'Yugoslavia': 'Yougoslavie', 'Czechoslovakia': 'Tchecoslovaquie', 'East Germany': 'RDA',
}

# (pays, nom Wikipedia) -> club de l'appli, quand le rapprochement par les mots est ambigu.
TEAM_ALIASES = {
    ('Italie', 'Roma'): 'AS ROME',
    ('Italie', 'AS Roma'): 'AS ROME',
}

# Club de l'appli -> autre(s) nom(s) sous le(s)quel(s) Wikipedia le connait (abreviations, noms francais).
TEAM_SYNONYMS = {
    ('France', 'PSG'): ['Paris Saint-Germain'],
    ('Angleterre', 'MANCHESTER UTD'): ['Manchester United'],
    ('Autriche', 'AUSTRIA VIENNE'): ['Austria Wien', 'Austria Vienna'],
    ('Autriche', 'RED BULL SALZBOURG'): ['Red Bull Salzburg', 'Austria Salzburg'],
    ('Autriche', 'FIRST VIENNE'): ['First Vienna'],
    ('Ecosse', 'HEARTS OF MIDLOTHIAN'): ['Heart of Midlothian', 'Hearts'],
    ('Belgique', 'KAA LA GANTOISE'): ['Gent', 'KAA Gent'],
    ('Belgique', 'COURTRAI'): ['Kortrijk'],
    ('Belgique', 'LOUVAIN'): ['OH Leuven'],
    ('Belgique', 'SAINT-TROND'): ['Sint-Truiden', 'Sint-Truidense'],
    ('Grece', 'OLYMPIAKOS'): ['Olympiacos'],
    ('Hongrie', 'DEBRECENI VSC'): ['Debrecen'],
    ('Hongrie', 'GYORI ETO FC'): ['Győr', 'Győri ETO'],
    ('Hongrie', 'PUSKAS ACADEMY'): ['Puskás Akadémia'],
    ('Israel', 'H. HAIFA'): ['Hapoel Haifa'],
    ('Israel', 'H. TEL AVIV'): ['Hapoel Tel Aviv'],
    ('Israel', 'M. HAIFA'): ['Maccabi Haifa'],
    ('Israel', 'M. TEL AVIV'): ['Maccabi Tel Aviv'],
    ('Israel', 'M. PETACH TIKVA'): ['Maccabi Petah Tikva'],
    ('Israel', 'HAP. RAMAT GAN'): ['Hapoel Ramat Gan'],
    ('Pays-Bas', 'ALKMAAR'): ['AZ', 'AZ Alkmaar', "AZ '67"],
    ('Pays-Bas', 'WILLLEM II'): ['Willem II'],
    ('Pays-Bas', 'NIJMEGEN'): ['NEC', 'NEC Nijmegen'],
    ('Roumanie', 'D. BUCAREST'): ['Dinamo București', 'Dinamo Bucharest'],
    ('Russie', 'DIN. MOSCOU'): ['Dynamo Moscow'],
    ('Russie', 'LOK. MOSCOU'): ['Lokomotiv Moscow'],
    ('Russie', 'SP. MOSCOU'): ['Spartak Moscow'],
    ('Serbie', 'ETOILE ROUGE BELGRADE'): ['Red Star Belgrade', 'Red Star'],
    ('Slovaquie', 'DUN. STREDA'): ['DAC Dunajská Streda', 'Dunajská Streda'],
    ('Suisse', 'GRASSHOPPERS'): ['Grasshopper', 'Grasshopper Club Zürich'],
    ('Suede', 'DJUGARDEN'): ['Djurgårdens IF', 'Djurgården'],
    ('Tchequie', 'BOHEMIANS 1905'): ['Bohemians Prague', 'Bohemians Praha'],
    ('Ukraine', 'SHAKTHAR'): ['Shakhtar Donetsk'],
    ('Montenegro', 'BIUDUCNOST'): ['Budućnost', 'Budućnost Podgorica'],
    ('Bulgarie', 'LOK. PLOVDIV'): ['Lokomotiv Plovdiv'],
    ('Bulgarie', 'LOK. SOFIA'): ['Lokomotiv Sofia'],
    ('Chypre', 'OL. NICOSIA'): ['Olympiakos Nicosia'],
    ('Galles', 'TNS'): ['The New Saints', 'Total Network Solutions'],
    ('Andorre', 'FC RANGERS'): ["FC Rànger's", "Rànger's"],
    ('Islande', 'HAFNARFJORDUR'): ['FH', 'FH Hafnarfjörður'],
    ('Islande', 'THOR AKUREYRI'): ['Þór', 'Þór Akureyri'],
    ('Kazakhstan', 'ERTIS PAVLODAR'): ['Irtysh', 'Irtysh Pavlodar'],
    ('Kazakhstan', 'ZHENIS'): ['Jenis', 'Zhenis Astana'],
    ('Azerbaidjan', 'NEFCI BAKU'): ['Neftçi', 'Neftchi Baku'],
    ('Allemagne', 'FC COLOGNE'): ['1. FC Köln'],
    ('Pays-Bas', 'DEN HAAG'): ['ADO Den Haag'],
    ('Portugal', 'SPORTING'): ['Sporting CP', 'Sporting Lisbon'],
    ('Russie', 'ZENIT'): ['Zenit Saint Petersburg', 'Zenit Leningrad'],
    ('Russie', 'KAZAN'): ['Rubin Kazan'],
    ('Russie', 'SAMARA'): ['Krylia Sovetov Samara', 'Krylia Sovetov'],
    ('Ukraine', 'CH. ODESA'): ['Chornomorets Odesa'],
}

# Clubs dont le nom seul designe d'autres clubs historiques ("RC Paris", "CA Paris"...) : rapproches
# uniquement par leurs TEAM_SYNONYMS.
SYNONYMS_ONLY = {('France', 'PARIS FC'), ('Lettonie', 'RIGA FC')}


def sources():
    """[(scope, kind, libelle, page)] : kind = LEAGUE / CUP pour un pays, code de coupe d'Europe sinon."""
    out = []
    for country, (league, cup) in COUNTRIES.items():
        for page in league.split(' + '):
            out.append((country, 'LEAGUE', 'Championnat', page))
        for page in cup.split(' + '):
            out.append((country, 'CUP', 'Coupe nationale', page))
    for code, (label, page) in EUROPEAN_CUPS.items():
        out.append((EUROPE, code, label, page))
    return out


def split_page(page):
    """'Titre#0,2' -> ('Titre', {0, 2}) ; 'Titre' -> ('Titre', None)."""
    title, _, indexes = page.partition('#')
    return title, {int(i) for i in indexes.split(',')} if indexes else None


def cache_file(cache, page):
    return Path(cache) / (re.sub(r'[^A-Za-z0-9]+', '_', split_page(page)[0]) + '.html')


def fetch(cache):
    Path(cache).mkdir(parents=True, exist_ok=True)
    for _, _, _, page in sources():
        path = cache_file(cache, page)
        if path.exists():
            continue
        # "fr:Titre" : Wikipedia en francais, quand la page anglaise n'a pas de palmares exploitable.
        title = split_page(page)[0]
        lang, title = title.split(':', 1) if title.startswith('fr:') else ('en', title)
        url = f'https://{lang}.wikipedia.org/api/rest_v1/page/html/' + urllib.parse.quote(title.replace(' ', '_'), safe='')
        path.write_bytes(download(url))
        print(f'{page} : {path.stat().st_size} octets', file=sys.stderr)
        time.sleep(2)


def download(url, attempts=5):
    """Contenu de l'URL ; patiente et reessaie quand Wikipedia limite le debit (HTTP 429)."""
    req = urllib.request.Request(url, headers={'User-Agent': USER_AGENT})
    for attempt in range(attempts):
        try:
            with urllib.request.urlopen(req) as resp:
                return resp.read()
        except urllib.error.HTTPError as e:
            if e.code != 429 or attempt == attempts - 1:
                raise
            time.sleep(20 * (attempt + 1))


# --- Extraction -----------------------------------------------------------------------------

SEASON_HEADER = re.compile(r'^(season|year|edition|final|date|saison|année|édition)s?$', re.I)
WINNER_HEADER = re.compile(r'^(?!vice)(champion|winner|vainqueur|1st|first place|gold)|^[^v]*champion|winner', re.I)
SECOND_HEADER = re.compile(r'runner|second|^2nd|finalist|^silver|loser|vice|deuxième|^2e', re.I)
THIRD_HEADER = re.compile(r'third|^3rd|^bronze|troisième|^3e', re.I)
YEAR = re.compile(r'(1[89]\d\d|20\d\d)(?:\s*[–/-]\s*(\d{4}|\d{2}))?')

# Tableaux "titres par club" : colonne club, puis la liste des saisons gagnees / perdues.
CLUB_HEADER = re.compile(r'^(club|team|clubs|teams|équipe)$', re.I)
WON_YEARS_HEADER = re.compile(r'(winning|won|title|cup-winning).*(years?|seasons?)|(years?|seasons?).*(won|winning)|^years$|^années$|^saisons$', re.I)
LOST_YEARS_HEADER = re.compile(r'(runners?-up|losing|lost|finalist).*(years?|seasons?)|(years?|seasons?).*(runners?-up|lost|losing)', re.I)

# Cases sans vainqueur (saison annulee, pas de competition, finale a venir...).
NOT_HELD = re.compile(r'not (held|played|awarded|contested|finished|completed)|no (competition|champion|winner|tournament|title)|'
                      r'abandon|cancel|suspended|void|annul|unfinished|interrupted|withheld|world war|discontinued|'
                      r'^tb[ad]$|^n/?a$|^—$|^-$|^–$|^$', re.I)


def clean_team(text):
    # Buteurs, tireurs de penalty ou notes sous le club ("Sabah / Safranko 36'", "AIK / League
    # champions: ...") : seul le club precede le 1er " / ".
    parts = text.split(' / ')
    if len(parts) > 1 and any(re.search(r"\d+'|:", part) for part in parts[1:]):
        text = parts[0]
    text = re.sub(r'\(\d+(/\d+)?\)', '', text)                 # nombre de titres "(14)", "(28/28)"
    text = re.sub(r'\(\d+(st|nd|rd|th)\)', '', text)           # "(7th)"
    text = re.sub(r'\((I{1,3}|IV|V)\)|\(Level \d\)', '', text)  # division du finaliste "(III)", "(Level 2)"
    text = re.sub(r'\s*\((?:[^()]*\bawarded\b[^()]*)\)', '', text, flags=re.I)
    text = re.sub(r'\s*\((en|de|it|es|pt|nl)\)', '', text)          # lien interlangue (fr.wikipedia)
    text = re.sub(r'[*†‡§#^¤]+', '', text)
    text = text.replace(' and / ', ' / ')                       # titre partage
    text = re.sub(r'\s+', ' ', text).strip(' ,;/')
    return text


def season_of(text):
    """'1893–94' -> ('1893-1894', 1894) ; '2019' -> ('2019', 2019) ; None si aucune annee."""
    m = YEAR.search(text)
    if not m:
        return None
    start = int(m.group(1))
    if m.group(2):
        end = m.group(2)
        end = int(end) if len(end) == 4 else int(str(start)[:2] + end)
        if end < start:
            end += 100
        if end == start + 1:
            return f'{start}-{end}', end
    return str(start), start


def header_indexes(header, pattern, exclude=()):
    return [i for i, cell in enumerate(header) if pattern.search(cell['text']) and i not in exclude]


def first_or_none(indexes):
    return indexes[0] if indexes else None


def is_header(row):
    """Ligne d'en-tete : que des <th>, ou (en-tete en <td>) une 1re case qui n'est pas une saison."""
    return bool(row) and (all(c['th'] for c in row) or not season_of(row[0]['text']))


def page_tables(html, only=None):
    return [t for i, t in enumerate(wikitables(html)) if only is None or i in only]


def tables_of(html, only=None):
    """[(colonnes, lignes)] des tableaux saison par saison (colonne saison, puis vainqueur)."""
    found = []
    for t in page_tables(html, only):
        rows = t['rows']
        # En-tete sur 1 a 3 lignes : la premiere qui nomme une colonne saison et une colonne vainqueur.
        for h in range(min(3, len(rows))):
            header = rows[h]
            if not is_header(header):
                continue
            seasons = [i for i in header_indexes(header, SEASON_HEADER) if i <= 2]
            winner = first_or_none(header_indexes(header, WINNER_HEADER, exclude=seasons))
            # Saison dans les 1res colonnes, avant le vainqueur : ecarte les tableaux "titres par
            # club" (club d'abord, puis la liste de ses saisons titrees).
            if not seasons or winner is None or winner < seasons[0]:
                continue
            second = first_or_none(header_indexes(header, SECOND_HEADER, exclude=seasons + [winner]))
            third = first_or_none(header_indexes(header, THIRD_HEADER, exclude=seasons + [winner, second]))
            found.append(({'seasons': seasons, 'winner': winner, 'second': second, 'third': third},
                          rows[h + 1:]))
            break
    return found


def podium_of(texts, cols):
    """[1er, 2e, 3e] d'une ligne, None si la saison n'a pas de vainqueur."""
    winner = clean_team(texts[cols['winner']])
    # Une phrase ("Season played, but not officially recognised") n'est pas un nom de club.
    if NOT_HELD.search(winner) or not re.search(r'[^\W\d_]', winner) or len(winner.split()) >= 6:
        return None
    podium = [winner]
    for key in ('second', 'third'):
        i = cols[key]
        value = clean_team(texts[i]) if i is not None and i < len(texts) else ''
        podium.append('' if NOT_HELD.search(value) or len(value.split()) >= 6 else value)
    return podium


def flag_of(cell):
    return next((link[5:] for link in cell['links'] if link.startswith('flag:')), None)


def nation_near(row, index, step):
    """Pays d'un club (coupes d'Europe) : drapeau dans sa case, sinon dans la colonne "Nation" voisine."""
    if index is None or index >= len(row):
        return None
    for i in (index, index + step):
        if 0 <= i < len(row):
            flag = flag_of(row[i])
            if flag:
                flag = re.sub(r'\s*\(.*\)$', '', flag)       # "Portugal (official)"
                return NATIONS.get(flag, flag)
    return None


def extract_seasons(html, only=None, multi=False):
    """{cle: (saison, annee de fin, [1er, 2e, 3e], [pays du 1er, du 2e])} des tableaux saison par saison.
    Cle = saison, ou (saison, vainqueur) quand une saison a plusieurs vainqueurs (Intertoto)."""
    result = {}
    for cols, rows in tables_of(html, only):
        for row in rows:
            if len(row) <= cols['winner']:
                continue
            texts = [c['text'] for c in row]
            if len(set(texts)) == 1:          # ligne de section ("Amateur era")
                continue
            # Colonne saison parfois dedoublee (colspan) : la 1re qui contient une annee.
            season = next((season_of(texts[i]) for i in cols['seasons'] if i < len(texts) and season_of(texts[i])), None)
            podium = podium_of(texts, cols) if season else None
            if podium:
                label, end = season
                nations = [nation_near(row, cols['winner'], -1), nation_near(row, cols['second'], 1)]
                result.setdefault((label, podium[0]) if multi else label, (label, end, podium, nations))
    return result


def extract_by_club(html, only=None):
    """Repli quand la page n'a qu'un tableau "titres par club" : chaque saison listee dans la colonne
    des annees gagnees (et perdues, pour le finaliste) d'un club redevient une ligne."""
    winners, runners = {}, {}
    for t in page_tables(html, only):
        rows = t['rows']
        if not rows or not is_header(rows[0]):
            continue
        header = rows[0]
        club = first_or_none(header_indexes(header, CLUB_HEADER))
        won = first_or_none(header_indexes(header, WON_YEARS_HEADER, exclude=[club]))
        lost = first_or_none(header_indexes(header, LOST_YEARS_HEADER, exclude=[club, won]))
        if club is None or (won is None and lost is None):
            continue
        for row in rows[1:]:
            if len(row) <= max(i for i in (club, won, lost) if i is not None):
                continue
            name = clean_team(row[club]['text'])
            for index, target in ((won, winners), (lost, runners)):
                if index is None:
                    continue
                for token in re.split(r'[,;]', row[index]['text']):
                    season = season_of(token)
                    if season:
                        target.setdefault(season, name)
    return {label: (label, end, [name, runners.get((label, end), ''), ''], [None, None])
            for (label, end), name in winners.items()}


def extract(html, only=None, multi=False):
    return extract_seasons(html, only, multi) or extract_by_club(html, only)


def collect(cache):
    """{(scope, kind): (libelle, [(saison, annee, podium, pays)] trie par annee)}"""
    data = {}
    for scope, kind, label, page in sources():
        html = cache_file(cache, page).read_text(encoding='utf-8')
        entries = data.setdefault((scope, kind), (label, {}))[1]
        # Une saison deja donnee par une page precedente n'est pas reprise d'une page suivante.
        seen = {e[0] for e in entries.values()}
        for key, value in extract(html, split_page(page)[1], kind in MULTI_WINNER_KINDS).items():
            if value[0] not in seen:
                entries.setdefault(key, value)
    return {key: (label, sorted(entries.values(), key=lambda e: e[1]))
            for key, (label, entries) in data.items()}


def report(cache, only=None):
    for (scope, kind), (label, items) in collect(cache).items():
        if only and scope != only:
            continue
        if not items:
            print(f'!! {scope} / {label} : AUCUNE SAISON')
            continue
        seconds = sum(1 for e in items if e[2][1])
        thirds = sum(1 for e in items if e[2][2])
        print(f'{scope} / {label} : {len(items)} saisons ({items[0][0]} -> {items[-1][0]}), 2e {seconds}, 3e {thirds}')
        for season, _, podium, nations in items[-2:]:
            print(f'    {season} : {podium} {nations}')


# --- Clubs de la base -----------------------------------------------------------------------
# Les noms Wikipedia (anglais) sont rapproches des clubs de l'appli (noms francais, souvent abreges)
# pour afficher leur logo : memes mots une fois accents, mots generiques (FC, AC...) et exonymes
# retires, ou mots de l'un tous contenus dans l'autre quand un seul club correspond.

GENERIC_WORDS = {'FC', 'CF', 'AC', 'AS', 'SC', 'SV', 'FK', 'NK', 'KS', 'CD', 'UD', 'SD', 'RC', 'RCD', 'CA', 'SK',
                 'BK', 'IF', 'IK', 'FF', 'KV', 'KRC', 'RSC', 'SS', 'SSC', 'US', 'OGC', 'ACF', 'VFB', 'VFL', 'TSG',
                 'BSC', 'CS', 'SL', 'KF', 'PFC', 'PFK', 'FCI', 'JK', 'GNK', 'HNK', 'OFK', 'PAE', 'GS', 'TSV', 'SPVGG',
                 'SG', 'CLUB', 'DE', 'DA', 'DO', 'DI', 'LA', 'THE', 'AFC', 'CFC', 'SCO', 'FUTEBOL', 'CALCIO',
                 'FOOTBALL'}
# Mot anglais (ou local) -> mot utilise par les noms de l'appli.
EXONYMS = {'KOLN': 'COLOGNE', 'BREMEN': 'BREME', 'FRANKFURT': 'FRANCFORT', 'MAINZ': 'MAYENCE',
           'HAMBURG': 'HAMBOURG', 'HAMBURGER': 'HAMBOURG', 'FREIBURG': 'FRIBOURG', 'AUGSBURG': 'AUGSBOURG',
           'MUNCHEN': 'MUNICH', 'LISBON': 'LISBONNE', 'SEVILLA': 'SEVILLE', 'MOSCOW': 'MOSCOU',
           'WARSAW': 'VARSOVIE', 'ATHENS': 'ATHENES', 'ANTWERP': 'ANVERS', 'BUCHAREST': 'BUCAREST',
           'NAPOLI': 'NAPLES', 'ROMA': 'ROME', 'BARCELONA': 'BARCELONE', 'VALENCIA': 'VALENCE',
           'BOLOGNA': 'BOLOGNE', 'PARMA': 'PARME', 'PALERMO': 'PALERME', 'VERONA': 'VERONE',
           'BERGAMO': 'BERGAME', 'INTERNAZIONALE': 'INTER', 'UTD': 'UNITED', 'BRUGGE': 'BRUGES',
           'PRAHA': 'PRAGUE', 'BELGRADE': 'BELGRADE', 'KYIV': 'KIEV', 'KIEV': 'KIEV', 'SAINT': 'ST',
           'DYN': 'DYNAMO', 'DIN': 'DYNAMO', 'DINAMO': 'DYNAMO', 'ATLETICO': 'ATL', 'WIEN': 'VIENNE', 'SALZBURG': 'SALZBOURG', 'COPENHAGEN': 'COPENHAGUE',
           'KOBENHAVN': 'COPENHAGUE', 'TBILISI': 'TBILISSI', 'BATUMI': 'BATOUMI', 'KUTAISI': 'KOUTAISSI',
           'BUCURESTI': 'BUCAREST', 'BASEL': 'BALE', 'GENEVE': 'GENEVE', 'GENEVA': 'GENEVE', 'ATHLETIC': 'ATH', 'VILLARREAL': 'VILLAREAL', 'CORUNA': 'COROGNE'}


# Lettres sans decomposition Unicode (o barre, eszett...) : equivalent ASCII.
LETTERS = str.maketrans({'ø': 'o', 'Ø': 'O', 'æ': 'ae', 'Æ': 'AE', 'ß': 'ss', 'ł': 'l', 'Ł': 'L', 'đ': 'd',
                         'Đ': 'D', 'ı': 'i', 'þ': 'th', 'Þ': 'Th', 'ð': 'd', 'Ð': 'D', 'œ': 'oe'})


def words(name):
    plain = unicodedata.normalize('NFKD', name.translate(LETTERS)).encode('ascii', 'ignore').decode().upper()
    out = set()
    for w in re.split(r'[^A-Z0-9]+', plain):
        w = EXONYMS.get(w, w)
        if len(w) > 1 and w not in GENERIC_WORDS:
            out.add(w)
    return frozenset(out)


def load_teams(path):
    """{pays: [(nom, mots)]} des clubs de l'appli (export "nom|pays" de la table team), un couple par
    nom connu du club (son nom dans l'appli, puis ses TEAM_SYNONYMS)."""
    teams = {}
    for line in Path(path).read_text(encoding='utf-8').splitlines():
        name, _, country = line.rpartition('|')
        if name:
            own = [] if (country, name) in SYNONYMS_ONLY else [name]
            for known in [*own, *TEAM_SYNONYMS.get((country, name), [])]:
                teams.setdefault(country, []).append((name, words(known)))
    return teams


def podium_countries(scope, nations):
    """Pays du 1er, du 2e et du 3e : celui du palmares, ou leur drapeau pour les coupes d'Europe."""
    return [nations[0], nations[1], None] if scope == EUROPE else [scope] * 3


def team_matches(data, teams):
    """{(pays, nom Wikipedia): club de l'appli}. Memes mots : correspondance directe (si un seul club).
    Mots de l'un tous contenus dans l'autre ("Borussia Dortmund" / "DORTMUND") : seulement si le club
    n'a aucune correspondance directe et qu'un seul nom du palmares de son pays est dans ce cas (sinon
    "RC Paris" et "CA Paris" iraient tous deux au PARIS FC)."""
    names = {(c, p) for (scope, _), (_, items) in data.items() for e in items
             for p, c in zip(e[2], podium_countries(scope, e[3])) if p and c}
    result = {}
    subset = {}
    exact_clubs = set()
    for country, name in names:
        alias = TEAM_ALIASES.get((country, name))
        if alias:
            result[(country, name)] = alias
            exact_clubs.add((country, alias))
            continue
        wanted = words(name)
        candidates = teams.get(country, [])
        exact = {n for n, w in candidates if w == wanted}
        if len(exact) == 1:
            club = exact.pop()
            result[(country, name)] = club
            exact_clubs.add((country, club))
        elif not exact:
            found = {n for n, w in candidates if w and (w <= wanted or wanted <= w)}
            if len(found) == 1:
                subset.setdefault((country, found.pop()), []).append(name)
    for (country, club), wiki_names in subset.items():
        if len(wiki_names) == 1 and (country, club) not in exact_clubs:
            result[(country, wiki_names[0])] = club
    return result


def sql_text(value):
    return 'NULL' if not value else "'" + value.replace("'", "''") + "'"


def team_id(name, country):
    if not name:
        return 'NULL'
    return f'(SELECT id FROM team WHERE name = {sql_text(name)} AND country = {sql_text(country)} LIMIT 1)'


def write_sql(cache, teams_path, path):
    data = collect(cache)
    matches = team_matches(data, load_teams(teams_path))
    lines = ['-- Palmares historique (table honour, cf. V124) genere par import/palmares.py a partir de',
             "-- Wikipedia (listes des champions, finales de coupe, finales des coupes d'Europe) : un",
             '-- champion / vainqueur par saison, avec le 2e (finaliste) et le 3e quand la page les donne,',
             "-- le pays du club pour les coupes d'Europe et le club de l'appli quand il existe (logo).",
             '']
    total = matched = 0
    for (scope, kind), (label, items) in data.items():
        if not items:
            continue
        lines.append(f'-- {scope} : {label} ({len(items)} saisons)')
        lines.append('INSERT INTO honour (scope, kind, competition_label, season, season_end, winner, runner_up, '
                     'third, winner_country, runner_up_country, winner_team_id, runner_up_team_id, third_team_id) VALUES')
        values = []
        for season, end, podium, nations in items:
            countries = podium_countries(scope, nations)
            clubs = [matches.get((c, p)) for p, c in zip(podium, countries)]
            matched += sum(1 for c in clubs if c)
            values.append(
                f"({sql_text(scope)}, {sql_text(kind)}, {sql_text(label)}, {sql_text(season)}, {end}, "
                f"{sql_text(podium[0])}, {sql_text(podium[1])}, {sql_text(podium[2])}, "
                f"{sql_text(nations[0]) if scope == EUROPE else 'NULL'}, {sql_text(nations[1]) if scope == EUROPE else 'NULL'}, "
                f"{team_id(clubs[0], countries[0])}, {team_id(clubs[1], countries[1])}, {team_id(clubs[2], countries[2])})")
        lines.append(',\n'.join(values) + ';')
        lines.append('')
        total += len(items)
    Path(path).write_text('\n'.join(lines), encoding='utf-8')
    print(f'{path} : {total} saisons, {matched} clubs relies a l\'appli', file=sys.stderr)


def unmatched(cache, teams_path):
    """Clubs de l'appli jamais relies a un nom du palmares de leur pays (a completer dans TEAM_SYNONYMS)."""
    data = collect(cache)
    teams = load_teams(teams_path)
    matches = team_matches(data, teams)
    used = {(c, club) for (c, _), club in matches.items()}
    countries = {c for c, _ in matches}
    for country, clubs in sorted(teams.items()):
        missing = sorted({n for n, _ in clubs if (country, n) not in used})
        if missing and country in countries:
            print(f'{country} : {missing}')


if __name__ == '__main__':
    command, cache = sys.argv[1], sys.argv[2]
    if command == 'fetch':
        fetch(cache)
    elif command == 'report':
        report(cache, sys.argv[3] if len(sys.argv) > 3 else None)
    elif command == 'sql':
        write_sql(cache, sys.argv[3], sys.argv[4])
    elif command == 'unmatched':
        unmatched(cache, sys.argv[3])
    else:
        raise SystemExit(f'commande inconnue : {command}')
