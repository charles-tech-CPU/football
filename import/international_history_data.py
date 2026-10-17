"""
Donnees de international_history.py : noms francais des selections (sans accents, comme les
selections deja en base, cf. V105/V107-V109), confederations, decoupage des editions en
phases, lettres des groupes et scores des tirs au but (Wikipedia).
"""

FIRST_VERSION = 116

# Nom anglais du jeu de donnees -> nom de l'equipe en base
TEAM_NAMES = {
    'Afghanistan': 'Afghanistan', 'Albania': 'Albanie', 'Algeria': 'Algerie', 'American Samoa': 'Samoa americaines',
    'Andorra': 'Andorre', 'Angola': 'Angola', 'Anguilla': 'Anguilla', 'Antigua and Barbuda': 'Antigua-et-Barbuda',
    'Argentina': 'Argentine', 'Armenia': 'Armenie', 'Aruba': 'Aruba', 'Australia': 'Australie', 'Austria': 'Autriche',
    'Azerbaijan': 'Azerbaidjan', 'Bahamas': 'Bahamas', 'Bahrain': 'Bahrein', 'Bangladesh': 'Bangladesh',
    'Barbados': 'Barbade', 'Belarus': 'Belarus', 'Belgium': 'Belgique', 'Belize': 'Belize', 'Benin': 'Benin',
    'Bermuda': 'Bermudes', 'Bhutan': 'Bhoutan', 'Bolivia': 'Bolivie', 'Bonaire': 'Bonaire',
    'Bosnia and Herzegovina': 'Bosnie', 'Botswana': 'Botswana', 'Brazil': 'Bresil',
    'British Virgin Islands': 'Iles Vierges Britanniques', 'Brunei': 'Brunei', 'Bulgaria': 'Bulgarie',
    'Burkina Faso': 'Burkina Faso', 'Burundi': 'Burundi', 'Cambodia': 'Cambodge', 'Cameroon': 'Cameroun',
    'Canada': 'Canada', 'Cape Verde': 'Cap-Vert', 'Cayman Islands': 'Iles Caimans',
    'Central African Republic': 'Centrafrique', 'Chad': 'Tchad', 'Chile': 'Chili', 'China': 'Chine',
    'Colombia': 'Colombie', 'Comoros': 'Comores', 'Congo': 'Congo', 'Cook Islands': 'Iles Cook',
    'Costa Rica': 'Costa Rica', 'Croatia': 'Croatie', 'Cuba': 'Cuba', 'Curaçao': 'Curacao', 'Cyprus': 'Chypre',
    'Czech Republic': 'Tchequie', 'DR Congo': 'RD Congo', 'Denmark': 'Danemark', 'Djibouti': 'Djibouti',
    'Dominica': 'Dominique', 'Dominican Republic': 'Republique Dominicaine', 'Ecuador': 'Equateur',
    'Egypt': 'Egypte', 'El Salvador': 'Salvador', 'England': 'Angleterre', 'Equatorial Guinea': 'Guinee Equatoriale',
    'Eritrea': 'Erythree', 'Estonia': 'Estonie', 'Eswatini': 'Eswatini', 'Ethiopia': 'Ethiopie',
    'Faroe Islands': 'Iles Feroe', 'Fiji': 'Fidji', 'Finland': 'Finlande', 'France': 'France',
    'French Guiana': 'Guyane', 'Gabon': 'Gabon', 'Gambia': 'Gambie', 'Georgia': 'Georgie', 'Germany': 'Allemagne',
    'Ghana': 'Ghana', 'Gibraltar': 'Gibraltar', 'Greece': 'Grece', 'Grenada': 'Grenade', 'Guadeloupe': 'Guadeloupe',
    'Guam': 'Guam', 'Guatemala': 'Guatemala', 'Guinea': 'Guinee', 'Guinea-Bissau': 'Guinee-Bissau',
    'Guyana': 'Guyana', 'Haiti': 'Haiti', 'Honduras': 'Honduras', 'Hong Kong': 'Hong Kong', 'Hungary': 'Hongrie',
    'Iceland': 'Islande', 'India': 'Inde', 'Indonesia': 'Indonesie', 'Iran': 'Iran', 'Iraq': 'Irak',
    'Israel': 'Israel', 'Italy': 'Italie', 'Ivory Coast': "Cote d'Ivoire", 'Jamaica': 'Jamaique', 'Japan': 'Japon',
    'Jordan': 'Jordanie', 'Kazakhstan': 'Kazakhstan', 'Kenya': 'Kenya', 'Kosovo': 'Kosovo', 'Kuwait': 'Koweit',
    'Kyrgyzstan': 'Kirghizstan', 'Laos': 'Laos', 'Latvia': 'Lettonie', 'Lebanon': 'Liban', 'Lesotho': 'Lesotho',
    'Liberia': 'Liberia', 'Libya': 'Libye', 'Liechtenstein': 'Liechtenstein', 'Lithuania': 'Lituanie',
    'Luxembourg': 'Luxembourg', 'Macau': 'Macao', 'Madagascar': 'Madagascar', 'Malawi': 'Malawi',
    'Malaysia': 'Malaisie', 'Maldives': 'Maldives', 'Mali': 'Mali', 'Malta': 'Malte', 'Martinique': 'Martinique',
    'Mauritania': 'Mauritanie', 'Mauritius': 'Maurice', 'Mexico': 'Mexique', 'Moldova': 'Moldavie',
    'Mongolia': 'Mongolie', 'Montenegro': 'Montenegro', 'Montserrat': 'Montserrat', 'Morocco': 'Maroc',
    'Mozambique': 'Mozambique', 'Myanmar': 'Birmanie', 'Namibia': 'Namibie', 'Nepal': 'Nepal',
    'Netherlands': 'Pays-Bas', 'New Caledonia': 'Nouvelle-Caledonie', 'New Zealand': 'Nouvelle-Zelande',
    'Nicaragua': 'Nicaragua', 'Niger': 'Niger', 'Nigeria': 'Nigeria', 'North Korea': 'Coree du Nord',
    'North Macedonia': 'Macedoine', 'Northern Ireland': 'Irlande Nord', 'Norway': 'Norvege', 'Oman': 'Oman',
    'Pakistan': 'Pakistan', 'Palestine': 'Palestine', 'Panama': 'Panama',
    'Papua New Guinea': 'Papouasie-Nouvelle-Guinee', 'Paraguay': 'Paraguay', 'Peru': 'Perou',
    'Philippines': 'Philippines', 'Poland': 'Pologne', 'Portugal': 'Portugal', 'Puerto Rico': 'Porto Rico',
    'Qatar': 'Qatar', 'Republic of Ireland': 'Eire', 'Romania': 'Roumanie', 'Russia': 'Russie', 'Rwanda': 'Rwanda',
    'Saint Kitts and Nevis': 'Saint-Kitts-et-Nevis', 'Saint Lucia': 'Sainte-Lucie', 'Saint Martin': 'Saint-Martin',
    'Saint Vincent and the Grenadines': 'Saint-Vincent-et-les-Grenadines', 'Samoa': 'Samoa',
    'San Marino': 'San Marin', 'Saudi Arabia': 'Arabie Saoudite', 'Scotland': 'Ecosse', 'Senegal': 'Senegal',
    'Serbia': 'Serbie', 'Seychelles': 'Seychelles', 'Sierra Leone': 'Sierra Leone', 'Singapore': 'Singapour',
    'Sint Maarten': 'Sint Maarten', 'Slovakia': 'Slovaquie', 'Slovenia': 'Slovenie',
    'Solomon Islands': 'Iles Salomon', 'Somalia': 'Somalie', 'South Africa': 'Afrique du Sud',
    'South Korea': 'Coree du Sud', 'South Sudan': 'Soudan du Sud', 'Spain': 'Espagne', 'Sri Lanka': 'Sri Lanka',
    'Sudan': 'Soudan', 'Suriname': 'Suriname', 'Sweden': 'Suede', 'Switzerland': 'Suisse', 'Syria': 'Syrie',
    'São Tomé and Príncipe': 'Sao Tome-et-Principe', 'Tahiti': 'Tahiti', 'Taiwan': 'Taipei chinois',
    'Tajikistan': 'Tadjikistan', 'Tanzania': 'Tanzanie', 'Thailand': 'Thailande', 'Timor-Leste': 'Timor oriental',
    'Togo': 'Togo', 'Tonga': 'Tonga', 'Trinidad and Tobago': 'Trinite-et-Tobago', 'Tunisia': 'Tunisie',
    'Turkey': 'Turquie', 'Turkmenistan': 'Turkmenistan', 'Turks and Caicos Islands': 'Turks-et-Caicos',
    'Uganda': 'Ouganda', 'Ukraine': 'Ukraine', 'United Arab Emirates': 'Emirats Arabes Unis',
    'United States': 'Etats-Unis', 'United States Virgin Islands': 'Iles Vierges des Etats-Unis',
    'Uruguay': 'Uruguay', 'Uzbekistan': 'Ouzbekistan', 'Vanuatu': 'Vanuatu', 'Venezuela': 'Venezuela',
    'Vietnam': 'Vietnam', 'Wales': 'Galles', 'Yemen': 'Yemen', 'Zambia': 'Zambie', 'Zimbabwe': 'Zimbabwe',
}


def _members(text):
    return frozenset(text.split('|'))


CONFEDERATIONS = {
    'UEFA': _members(
        'Albania|Andorra|Armenia|Austria|Azerbaijan|Belarus|Belgium|Bosnia and Herzegovina|Bulgaria|Croatia|'
        'Cyprus|Czech Republic|Denmark|England|Estonia|Faroe Islands|Finland|France|Georgia|Germany|Gibraltar|'
        'Greece|Hungary|Iceland|Israel|Italy|Kazakhstan|Kosovo|Latvia|Liechtenstein|Lithuania|Luxembourg|Malta|'
        'Moldova|Montenegro|Netherlands|North Macedonia|Northern Ireland|Norway|Poland|Portugal|'
        'Republic of Ireland|Romania|Russia|San Marino|Scotland|Serbia|Slovakia|Slovenia|Spain|Sweden|'
        'Switzerland|Turkey|Ukraine|Wales'),
    'CONMEBOL': _members('Argentina|Bolivia|Brazil|Chile|Colombia|Ecuador|Paraguay|Peru|Uruguay|Venezuela'),
    'CONCACAF': _members(
        'Anguilla|Antigua and Barbuda|Aruba|Bahamas|Barbados|Belize|Bermuda|Bonaire|British Virgin Islands|'
        'Canada|Cayman Islands|Costa Rica|Cuba|Curaçao|Dominica|Dominican Republic|El Salvador|French Guiana|'
        'Grenada|Guadeloupe|Guatemala|Guyana|Haiti|Honduras|Jamaica|Martinique|Mexico|Montserrat|Nicaragua|'
        'Panama|Puerto Rico|Saint Kitts and Nevis|Saint Lucia|Saint Martin|Saint Vincent and the Grenadines|'
        'Sint Maarten|Suriname|Trinidad and Tobago|Turks and Caicos Islands|United States|'
        'United States Virgin Islands'),
    'OFC': _members(
        'American Samoa|Cook Islands|Fiji|New Caledonia|New Zealand|Papua New Guinea|Samoa|Solomon Islands|'
        'Tahiti|Tonga|Vanuatu'),
    'CAF': _members(
        'Algeria|Angola|Benin|Botswana|Burkina Faso|Burundi|Cameroon|Cape Verde|Central African Republic|Chad|'
        'Comoros|Congo|DR Congo|Djibouti|Egypt|Equatorial Guinea|Eritrea|Eswatini|Ethiopia|Gabon|Gambia|Ghana|'
        'Guinea|Guinea-Bissau|Ivory Coast|Kenya|Lesotho|Liberia|Libya|Madagascar|Malawi|Mali|Mauritania|'
        'Mauritius|Morocco|Mozambique|Namibia|Niger|Nigeria|Rwanda|São Tomé and Príncipe|Senegal|Seychelles|'
        'Sierra Leone|Somalia|South Africa|South Sudan|Sudan|Tanzania|Togo|Tunisia|Uganda|Zambia|Zimbabwe'),
    'AFC': _members(
        'Afghanistan|Australia|Bahrain|Bangladesh|Bhutan|Brunei|Cambodia|China|Guam|Hong Kong|India|Indonesia|'
        'Iran|Iraq|Japan|Jordan|Kuwait|Kyrgyzstan|Laos|Lebanon|Macau|Malaysia|Maldives|Mongolia|Myanmar|Nepal|'
        'North Korea|Oman|Pakistan|Palestine|Philippines|Qatar|Saudi Arabia|Singapore|South Korea|Sri Lanka|'
        'Syria|Taiwan|Tajikistan|Thailand|Timor-Leste|Turkmenistan|United Arab Emirates|Uzbekistan|Vietnam|'
        'Yemen'),
}


def groups(label, start, end, **extra):
    return {'label': label, 'kind': 'groups', 'start': start, 'end': end, **extra}


def ko(label, start, end=None, **extra):
    return {'label': label, 'kind': 'ko', 'start': start, 'end': end or start, **extra}


def pairs(*couples):
    return {frozenset(c) for c in couples}


# Phases a elimination directe d'une phase finale, du premier tour au match pour le titre.
def finals_ko(*rounds):
    return [ko(label, start, end, expected=n) for label, start, end, n in rounds]


WC = 'FIFA World Cup'
WCQ = 'FIFA World Cup qualification'
CONF_LABELS = {'UEFA': 'Europe', 'CAF': 'Afrique', 'AFC': 'Asie', 'CONCACAF': 'Amérique du Nord',
               'CONMEBOL': 'Amérique du Sud', 'OFC': 'Océanie'}


# Fenetre de chaque campagne de qualification (les tours de deux campagnes ne se chevauchent pas).
WCQ_WINDOWS = {2018: ('2015-01-01', '2017-12-31'), 2022: ('2019-01-01', '2022-06-30'),
               2026: ('2023-01-01', '2026-03-31')}


def wcq(year, conf, phases, tournaments=(WCQ,)):
    start, end = WCQ_WINDOWS[year]
    return {'code': f'{conf}_WCQ_{year}', 'name': f'Qualifications Coupe du monde {year} - {CONF_LABELS[conf]}',
            'season': year, 'tournaments': set(tournaments), 'confederation': conf,
            'start': start, 'end': end, 'phases': phases}


EDITIONS = [
    {
        'title': 'Coupe du monde 2018, 2022 et 2026 (phases finales et qualifications)',
        'file': 'historique_coupe_du_monde',
        'editions': [
            # ---------------------------------------------------------------- 2018
            {'code': 'FIFA_WC_2018', 'name': 'Coupe du monde 2018', 'season': 2018, 'tournaments': {WC},
             'start': '2018-06-01', 'end': '2018-07-31', 'expected': 64, 'phases': [
                groups('Phase de groupes', '2018-06-14', '2018-06-28', expected=48),
                *finals_ko(('8es de finale', '2018-06-30', '2018-07-03', 8),
                           ('Quarts de finale', '2018-07-06', '2018-07-07', 4),
                           ('Demi-finales', '2018-07-10', '2018-07-11', 2),
                           ('Match pour la 3e place', '2018-07-14', None, 1),
                           ('Finale', '2018-07-15', None, 1))]},
            wcq(2018, 'UEFA', [groups('Phase de groupes', '2016-09-01', '2017-10-31'),
                               ko('Barrages', '2017-11-01', '2017-11-30')]),
            wcq(2018, 'CONMEBOL', [groups('Phase de groupes', '2015-10-01', '2017-10-31', single_table=True,
                                          group='Groupe unique')]),
            wcq(2018, 'CONCACAF', [ko('1er tour', '2015-03-01', '2015-04-30'),
                                   ko('2e tour', '2015-06-01', '2015-06-30'),
                                   ko('3e tour', '2015-08-01', '2015-09-30'),
                                   groups('4e tour', '2015-11-01', '2016-09-30', group_prefix='4e tour - '),
                                   groups('Hexagonal', '2016-11-01', '2017-10-31', single_table=True,
                                          group='Hexagonal')]),
            wcq(2018, 'CAF', [ko('1er tour', '2015-10-01', '2015-10-31'),
                              ko('2e tour', '2015-11-01', '2015-11-30'),
                              groups('3e tour', '2016-10-01', '2017-11-30')]),
            wcq(2018, 'AFC', [ko('1er tour', '2015-03-01', '2015-03-31'),
                              groups('2e tour', '2015-05-01', '2016-03-31', group_prefix='2e tour - '),
                              groups('3e tour', '2016-09-01', '2017-09-30', group_prefix='3e tour - '),
                              ko('4e tour', '2017-10-01', '2017-10-31')]),
            wcq(2018, 'OFC', [groups('1er tour', '2015-08-01', '2015-09-30', single_table=True,
                                     group='1er tour'),
                              groups('2e tour', '2016-05-28', '2016-06-05', group_prefix='2e tour - '),
                              groups('3e tour', '2016-11-01', '2017-06-30', group_prefix='3e tour - '),
                              ko('Finale', '2017-09-01', '2017-09-30')],
                tournaments=(WCQ, 'Oceania Nations Cup')) | {
                # 2e tour = phase de groupes de la Coupe d'Oceanie 2016 (demies et finale hors qualifs)
                'tournament_windows': {'Oceania Nations Cup': ('2016-05-28', '2016-06-05')}},
            {'code': 'FIFA_WCQ_2018', 'name': 'Barrages intercontinentaux Coupe du monde 2018', 'season': 2018,
             'tournaments': {WCQ}, 'confederation': 'INTERCONF', 'start': '2017-01-01', 'end': '2017-12-31',
             'expected': 4, 'phases': [ko('Barrages intercontinentaux', '2017-11-01', '2017-11-30')]},
            # ---------------------------------------------------------------- 2022
            {'code': 'FIFA_WC_2022', 'name': 'Coupe du monde 2022', 'season': 2022, 'tournaments': {WC},
             'start': '2022-11-01', 'end': '2022-12-31', 'expected': 64, 'phases': [
                groups('Phase de groupes', '2022-11-20', '2022-12-02', expected=48),
                *finals_ko(('8es de finale', '2022-12-03', '2022-12-06', 8),
                           ('Quarts de finale', '2022-12-09', '2022-12-10', 4),
                           ('Demi-finales', '2022-12-13', '2022-12-14', 2),
                           ('Match pour la 3e place', '2022-12-17', None, 1),
                           ('Finale', '2022-12-18', None, 1))]},
            wcq(2022, 'UEFA', [groups('Phase de groupes', '2021-03-01', '2021-11-30'),
                               ko('Barrages - demi-finales', None, dates={'2022-03-24', '2022-06-01'}),
                               ko('Barrages - finales', None, dates={'2022-03-29', '2022-06-05'})]),
            wcq(2022, 'CONMEBOL', [groups('Phase de groupes', '2020-10-01', '2022-03-31', single_table=True,
                                          group='Groupe unique')]),
            wcq(2022, 'CONCACAF', [groups('1er tour', '2021-03-01', '2021-06-08', group_prefix='1er tour - '),
                                   ko('2e tour', '2021-06-09', '2021-06-30'),
                                   groups('Octogonal', '2021-09-01', '2022-03-31', single_table=True,
                                          group='Octogonal')]),
            wcq(2022, 'CAF', [ko('1er tour', '2019-09-01', '2019-09-30'),
                              groups('2e tour', '2021-09-01', '2021-11-30'),
                              ko('3e tour', '2022-03-01', '2022-03-31')]),
            wcq(2022, 'AFC', [ko('1er tour', '2019-06-01', '2019-06-30'),
                              groups('2e tour', '2019-09-01', '2021-06-30', group_prefix='2e tour - '),
                              groups('3e tour', '2021-09-01', '2022-03-31', group_prefix='3e tour - '),
                              ko('4e tour', '2022-06-01', '2022-06-30')]),
            wcq(2022, 'OFC', [groups('Phase de groupes', '2022-03-01', '2022-03-24',
                                     forfeits=[('Cook Islands', 'Tahiti')]),
                              ko('Phase finale', '2022-03-25', '2022-03-31')]),
            {'code': 'FIFA_WCQ_2022', 'name': 'Barrages intercontinentaux Coupe du monde 2022', 'season': 2022,
             'tournaments': {WCQ}, 'confederation': 'INTERCONF', 'start': '2022-01-01', 'end': '2022-12-31',
             'expected': 2, 'phases': [ko('Barrages intercontinentaux', '2022-06-01', '2022-06-30')]},
            # ---------------------------------------------------------------- 2026
            {'code': 'FIFA_WC_2026', 'name': 'Coupe du monde 2026', 'season': 2026, 'tournaments': {WC},
             'start': '2026-06-01', 'end': '2026-07-31', 'expected': 104, 'phases': [
                groups('Phase de groupes', '2026-06-11', '2026-06-27', expected=72),
                *finals_ko(('16es de finale', '2026-06-28', '2026-07-03', 16),
                           ('8es de finale', '2026-07-04', '2026-07-07', 8),
                           ('Quarts de finale', '2026-07-09', '2026-07-11', 4),
                           ('Demi-finales', '2026-07-14', '2026-07-15', 2),
                           ('Match pour la 3e place', '2026-07-18', None, 1),
                           ('Finale', '2026-07-19', None, 1))]},
            wcq(2026, 'UEFA', [groups('Phase de groupes', '2025-03-01', '2025-11-30'),
                               ko('Barrages - demi-finales', '2026-03-26'),
                               ko('Barrages - finales', '2026-03-31')]),
            wcq(2026, 'CONMEBOL', [groups('Phase de groupes', '2023-09-01', '2025-09-30', single_table=True,
                                          group='Groupe unique')]),
            wcq(2026, 'CONCACAF', [ko('1er tour', '2024-03-01', '2024-03-31'),
                                   groups('2e tour', '2024-06-01', '2025-06-30', group_prefix='2e tour - '),
                                   groups('3e tour', '2025-09-01', '2025-11-30', group_prefix='3e tour - ')]),
            wcq(2026, 'CAF', [groups('Phase de groupes', '2023-11-01', '2025-10-31'),
                              ko('Barrages', '2025-11-01', '2025-11-30')]),
            wcq(2026, 'AFC', [ko('1er tour', '2023-10-01', '2023-10-31'),
                              groups('2e tour', '2023-11-01', '2024-06-30', group_prefix='2e tour - '),
                              groups('3e tour', '2024-09-01', '2025-06-30', group_prefix='3e tour - '),
                              groups('4e tour', '2025-10-01', '2025-10-31', group_prefix='4e tour - '),
                              ko('5e tour', '2025-11-01', '2025-11-30')]),
            wcq(2026, 'OFC', [ko('1er tour', '2024-08-01', '2024-09-30'),
                              groups('2e tour', '2024-10-01', '2024-11-30', group_prefix='2e tour - '),
                              ko('3e tour', '2025-03-01', '2025-03-31')]),
            {'code': 'FIFA_WCQ_2026', 'name': 'Barrages intercontinentaux Coupe du monde 2026', 'season': 2026,
             'tournaments': {WCQ}, 'confederation': 'INTERCONF', 'start': '2026-01-01', 'end': '2026-06-30',
             'expected': 4, 'phases': [ko('Barrages intercontinentaux', '2026-03-01', '2026-03-31')]},
        ],
    },
    {
        'title': 'Euro 2016, 2020 et 2024 (phases finales et qualifications)',
        'file': 'historique_euro',
        'editions': [
            {'code': 'UEFA_EURO_2016', 'name': 'Euro 2016', 'season': 2016, 'tournaments': {'UEFA Euro'},
             'start': '2016-06-01', 'end': '2016-07-31', 'expected': 51, 'phases': [
                groups('Phase de groupes', '2016-06-10', '2016-06-22', expected=36),
                *finals_ko(('8es de finale', '2016-06-25', '2016-06-27', 8),
                           ('Quarts de finale', '2016-06-30', '2016-07-03', 4),
                           ('Demi-finales', '2016-07-06', '2016-07-07', 2),
                           ('Finale', '2016-07-10', None, 1))]},
            {'code': 'UEFA_EUROQ_2016', 'name': 'Qualifications Euro 2016', 'season': 2016,
             'tournaments': {'UEFA Euro qualification'}, 'start': '2014-09-01', 'end': '2015-11-30', 'phases': [
                groups('Phase de groupes', '2014-09-01', '2015-10-31'),
                ko('Barrages', '2015-11-01', '2015-11-30', expected=8)]},
            {'code': 'UEFA_EURO_2020', 'name': 'Euro 2020', 'season': 2021, 'tournaments': {'UEFA Euro'},
             'start': '2021-06-01', 'end': '2021-07-31', 'expected': 51, 'phases': [
                groups('Phase de groupes', '2021-06-11', '2021-06-23', expected=36),
                *finals_ko(('8es de finale', '2021-06-26', '2021-06-29', 8),
                           ('Quarts de finale', '2021-07-02', '2021-07-03', 4),
                           ('Demi-finales', '2021-07-06', '2021-07-07', 2),
                           ('Finale', '2021-07-11', None, 1))]},
            {'code': 'UEFA_EUROQ_2020', 'name': 'Qualifications Euro 2020', 'season': 2021,
             'tournaments': {'UEFA Euro qualification'}, 'start': '2019-03-01', 'end': '2020-11-30', 'phases': [
                groups('Phase de groupes', '2019-03-01', '2019-11-30'),
                ko('Barrages - demi-finales', '2020-10-01', '2020-10-31', expected=8),
                ko('Barrages - finales', '2020-11-01', '2020-11-30', expected=4)]},
            {'code': 'UEFA_EURO_2024', 'name': 'Euro 2024', 'season': 2024, 'tournaments': {'UEFA Euro'},
             'start': '2024-06-01', 'end': '2024-07-31', 'expected': 51, 'phases': [
                groups('Phase de groupes', '2024-06-14', '2024-06-26', expected=36),
                *finals_ko(('8es de finale', '2024-06-29', '2024-07-02', 8),
                           ('Quarts de finale', '2024-07-05', '2024-07-06', 4),
                           ('Demi-finales', '2024-07-09', '2024-07-10', 2),
                           ('Finale', '2024-07-14', None, 1))]},
            {'code': 'UEFA_EUROQ_2024', 'name': 'Qualifications Euro 2024', 'season': 2024,
             'tournaments': {'UEFA Euro qualification'}, 'start': '2023-03-01', 'end': '2024-03-31', 'phases': [
                groups('Phase de groupes', '2023-03-01', '2023-11-30'),
                ko('Barrages - demi-finales', '2024-03-21', expected=6),
                ko('Barrages - finales', '2024-03-26', expected=3)]},
        ],
    },
    {
        'title': 'Copa América 2019, 2021 et 2024',
        'file': 'historique_copa_america',
        'editions': [
            {'code': 'CONMEBOL_COPA_2019', 'name': 'Copa América 2019', 'season': 2019,
             'tournaments': {'Copa América'}, 'start': '2019-06-01', 'end': '2019-07-31', 'expected': 26, 'phases': [
                groups('Phase de groupes', '2019-06-14', '2019-06-24', expected=18),
                *finals_ko(('Quarts de finale', '2019-06-27', '2019-06-29', 4),
                           ('Demi-finales', '2019-07-02', '2019-07-03', 2),
                           ('Match pour la 3e place', '2019-07-06', None, 1),
                           ('Finale', '2019-07-07', None, 1))]},
            {'code': 'CONMEBOL_COPA_2021', 'name': 'Copa América 2021', 'season': 2021,
             'tournaments': {'Copa América'}, 'start': '2021-06-01', 'end': '2021-07-31', 'expected': 28, 'phases': [
                groups('Phase de groupes', '2021-06-13', '2021-06-28', expected=20),
                *finals_ko(('Quarts de finale', '2021-07-02', '2021-07-03', 4),
                           ('Demi-finales', '2021-07-05', '2021-07-06', 2),
                           ('Match pour la 3e place', '2021-07-09', None, 1),
                           ('Finale', '2021-07-10', None, 1))]},
            {'code': 'CONMEBOL_COPA_2024', 'name': 'Copa América 2024', 'season': 2024,
             'tournaments': {'Copa América'}, 'start': '2024-06-01', 'end': '2024-07-31', 'expected': 32, 'phases': [
                groups('Phase de groupes', '2024-06-20', '2024-07-02', expected=24),
                *finals_ko(('Quarts de finale', '2024-07-04', '2024-07-06', 4),
                           ('Demi-finales', '2024-07-09', '2024-07-10', 2),
                           ('Match pour la 3e place', '2024-07-13', None, 1),
                           ('Finale', '2024-07-14', None, 1))]},
        ],
    },
    {
        'title': "Coupe d'Afrique des nations 2021, 2023 et 2025 (phases finales et qualifications)",
        'file': 'historique_can',
        'editions': [
            {'code': 'CAF_AFCON_2021', 'name': 'CAN 2021', 'season': 2022,
             'tournaments': {'African Cup of Nations'}, 'start': '2022-01-01', 'end': '2022-02-28', 'expected': 52,
             'phases': [
                groups('Phase de groupes', '2022-01-09', '2022-01-20', expected=36),
                *finals_ko(('8es de finale', '2022-01-23', '2022-01-26', 8),
                           ('Quarts de finale', '2022-01-29', '2022-01-30', 4),
                           ('Demi-finales', '2022-02-02', '2022-02-03', 2),
                           ('Match pour la 3e place', '2022-02-05', None, 1),
                           ('Finale', '2022-02-06', None, 1))]},
            {'code': 'CAF_AFCONQ_2021', 'name': 'Qualifications CAN 2021', 'season': 2021,
             'tournaments': {'African Cup of Nations qualification'}, 'start': '2019-10-01', 'end': '2021-06-30',
             'phases': [ko('Tour préliminaire', '2019-10-01', '2019-10-31'),
                        groups('Phase de groupes', '2019-11-01', '2021-06-30')]},
            {'code': 'CAF_AFCON_2023', 'name': 'CAN 2023', 'season': 2024,
             'tournaments': {'African Cup of Nations'}, 'start': '2024-01-01', 'end': '2024-02-29', 'expected': 52,
             'phases': [
                groups('Phase de groupes', '2024-01-13', '2024-01-24', expected=36),
                *finals_ko(('8es de finale', '2024-01-27', '2024-01-30', 8),
                           ('Quarts de finale', '2024-02-02', '2024-02-03', 4),
                           ('Demi-finales', '2024-02-07', None, 2),
                           ('Match pour la 3e place', '2024-02-10', None, 1),
                           ('Finale', '2024-02-11', None, 1))]},
            {'code': 'CAF_AFCONQ_2023', 'name': 'Qualifications CAN 2023', 'season': 2023,
             'tournaments': {'African Cup of Nations qualification'}, 'start': '2022-03-01', 'end': '2023-10-31',
             'phases': [ko('Tour préliminaire', '2022-03-01', '2022-03-31'),
                        groups('Phase de groupes', '2022-06-01', '2023-10-31')]},
            {'code': 'CAF_AFCON_2025', 'name': 'CAN 2025', 'season': 2026,
             'tournaments': {'African Cup of Nations'}, 'start': '2025-12-01', 'end': '2026-01-31', 'expected': 52,
             'phases': [
                groups('Phase de groupes', '2025-12-21', '2025-12-31', expected=36),
                *finals_ko(('8es de finale', '2026-01-03', '2026-01-06', 8),
                           ('Quarts de finale', '2026-01-09', '2026-01-10', 4),
                           ('Demi-finales', '2026-01-14', None, 2),
                           ('Match pour la 3e place', '2026-01-17', None, 1),
                           ('Finale', '2026-01-18', None, 1))]},
            {'code': 'CAF_AFCONQ_2025', 'name': 'Qualifications CAN 2025', 'season': 2025,
             'tournaments': {'African Cup of Nations qualification'}, 'start': '2024-03-01', 'end': '2024-11-30',
             'phases': [ko('Tour préliminaire', '2024-03-01', '2024-03-31'),
                        groups('Phase de groupes', '2024-09-01', '2024-11-30')]},
        ],
    },
    {
        'title': "Coupe d'Asie 2019 et 2023 (phases finales) et qualifications 2019, 2023 et 2027",
        'file': 'historique_coupe_asie',
        'editions': [
            {'code': 'AFC_ASIANCUP_2019', 'name': "Coupe d'Asie 2019", 'season': 2019,
             'tournaments': {'AFC Asian Cup'}, 'start': '2019-01-01', 'end': '2019-02-28', 'expected': 51, 'phases': [
                groups('Phase de groupes', '2019-01-05', '2019-01-17', expected=36),
                *finals_ko(('8es de finale', '2019-01-20', '2019-01-22', 8),
                           ('Quarts de finale', '2019-01-24', '2019-01-25', 4),
                           ('Demi-finales', '2019-01-28', '2019-01-29', 2),
                           ('Finale', '2019-02-01', None, 1))]},
            # Le 2e tour des qualifications est commun avec celui de la Coupe du monde (AFC_WCQ_*).
            {'code': 'AFC_ASIANCUPQ_2019', 'name': "Qualifications Coupe d'Asie 2019", 'season': 2018,
             'tournaments': {'AFC Asian Cup qualification'}, 'start': '2016-06-01', 'end': '2018-03-31', 'phases': [
                ko('Barrages - 1er tour', '2016-06-01', '2016-06-30'),
                ko('Barrages - 2e tour', '2016-09-01', '2016-10-31'),
                groups('3e tour', '2017-03-01', '2018-03-31')]},
            {'code': 'AFC_ASIANCUP_2023', 'name': "Coupe d'Asie 2023", 'season': 2024,
             'tournaments': {'AFC Asian Cup'}, 'start': '2024-01-01', 'end': '2024-02-29', 'expected': 51, 'phases': [
                groups('Phase de groupes', '2024-01-12', '2024-01-25', expected=36),
                *finals_ko(('8es de finale', '2024-01-28', '2024-01-31', 8),
                           ('Quarts de finale', '2024-02-02', '2024-02-03', 4),
                           ('Demi-finales', '2024-02-06', '2024-02-07', 2),
                           ('Finale', '2024-02-10', None, 1))]},
            {'code': 'AFC_ASIANCUPQ_2023', 'name': "Qualifications Coupe d'Asie 2023", 'season': 2022,
             'tournaments': {'AFC Asian Cup qualification'}, 'start': '2021-10-01', 'end': '2022-06-30', 'phases': [
                ko('Barrages', '2021-10-01', '2021-10-31'),
                groups('3e tour', '2022-06-01', '2022-06-30')]},
            {'code': 'AFC_ASIANCUPQ_2027', 'name': "Qualifications Coupe d'Asie 2027", 'season': 2026,
             'tournaments': {'AFC Asian Cup qualification'}, 'start': '2024-09-01', 'end': '2026-06-30', 'phases': [
                ko('Barrages', '2024-09-01', '2024-09-30'),
                groups('3e tour', '2025-03-01', '2026-06-30')]},
        ],
    },
    {
        'title': 'Gold Cup 2021, 2023 et 2025 (tour préliminaire et phase finale)',
        'file': 'historique_gold_cup',
        'editions': [
            {'code': 'CONCACAF_GOLDCUP_2021', 'name': 'Gold Cup 2021', 'season': 2021,
             'tournaments': {'Gold Cup', 'Gold Cup qualification'}, 'start': '2021-07-01', 'end': '2021-08-31',
             'expected': 40, 'phases': [
                ko('Tour préliminaire', '2021-07-01', '2021-07-06', expected=9),
                groups('Phase de groupes', '2021-07-10', '2021-07-20', expected=24),
                *finals_ko(('Quarts de finale', '2021-07-24', '2021-07-25', 4),
                           ('Demi-finales', '2021-07-29', None, 2),
                           ('Finale', '2021-08-01', None, 1))]},
            {'code': 'CONCACAF_GOLDCUP_2023', 'name': 'Gold Cup 2023', 'season': 2023,
             'tournaments': {'Gold Cup', 'Gold Cup qualification'}, 'start': '2023-06-01', 'end': '2023-07-31',
             'expected': 40, 'phases': [
                ko('Tour préliminaire', '2023-06-15', '2023-06-21', expected=9),
                groups('Phase de groupes', '2023-06-24', '2023-07-04', expected=24),
                *finals_ko(('Quarts de finale', '2023-07-08', '2023-07-09', 4),
                           ('Demi-finales', '2023-07-12', None, 2),
                           ('Finale', '2023-07-16', None, 1))]},
            {'code': 'CONCACAF_GOLDCUP_2025', 'name': 'Gold Cup 2025', 'season': 2025,
             'tournaments': {'Gold Cup', 'Gold Cup qualification'}, 'start': '2025-03-01', 'end': '2025-07-31',
             'expected': 45, 'phases': [
                ko('Tour préliminaire', '2025-03-01', '2025-03-31', expected=14),
                groups('Phase de groupes', '2025-06-14', '2025-06-24', expected=24),
                *finals_ko(('Quarts de finale', '2025-06-28', '2025-06-29', 4),
                           ('Demi-finales', '2025-07-02', None, 2),
                           ('Finale', '2025-07-06', None, 1))]},
        ],
    },
    {
        'title': 'Ligue des Nations UEFA 2018-2019 a 2024-2025',
        'file': 'historique_ligue_des_nations_uefa',
        'editions': [
            {'code': 'UEFA_NL_1819', 'name': 'Ligue des Nations UEFA 2018-2019', 'season': 2019,
             'tournaments': {'UEFA Nations League'}, 'start': '2018-09-01', 'end': '2019-06-30', 'phases': [
                groups('Phase de ligue', '2018-09-01', '2018-11-30', raw_names=True),
                ko('Demi-finales', '2019-06-05', '2019-06-06', expected=2),
                ko('Match pour la 3e place', '2019-06-09', pairs=pairs(('England', 'Switzerland')), expected=1),
                ko('Finale', '2019-06-09', pairs=pairs(('Portugal', 'Netherlands')), expected=1)]},
            {'code': 'UEFA_NL_2021', 'name': 'Ligue des Nations UEFA 2020-2021', 'season': 2021,
             'tournaments': {'UEFA Nations League'}, 'start': '2020-09-01', 'end': '2022-03-31', 'phases': [
                groups('Phase de ligue', '2020-09-01', '2020-11-30', raw_names=True),
                ko('Demi-finales', '2021-10-06', '2021-10-07', expected=2),
                ko('Match pour la 3e place', '2021-10-10', pairs=pairs(('Italy', 'Belgium')), expected=1),
                ko('Finale', '2021-10-10', pairs=pairs(('Spain', 'France')), expected=1),
                ko('Barrages de relégation', '2022-03-01', '2022-03-31', expected=4)]},
            {'code': 'UEFA_NL_2223', 'name': 'Ligue des Nations UEFA 2022-2023', 'season': 2023,
             'tournaments': {'UEFA Nations League'}, 'start': '2022-06-01', 'end': '2024-03-31', 'phases': [
                groups('Phase de ligue', '2022-06-01', '2022-09-30', raw_names=True),
                ko('Demi-finales', '2023-06-14', '2023-06-15', expected=2),
                ko('Match pour la 3e place', '2023-06-18', pairs=pairs(('Netherlands', 'Italy')), expected=1),
                ko('Finale', '2023-06-18', pairs=pairs(('Croatia', 'Spain')), expected=1),
                ko('Barrages de relégation', '2024-03-01', '2024-03-31', expected=2)]},
            {'code': 'UEFA_NL_2425', 'name': 'Ligue des Nations UEFA 2024-2025', 'season': 2025,
             'tournaments': {'UEFA Nations League'}, 'start': '2024-09-01', 'end': '2025-06-30', 'phases': [
                groups('Phase de ligue', '2024-09-01', '2024-11-30', raw_names=True),
                ko('Quarts de finale', '2025-03-01', '2025-03-31', expected=8,
                   pairs=pairs(('Netherlands', 'Spain'), ('Croatia', 'France'), ('Denmark', 'Portugal'),
                               ('Italy', 'Germany'))),
                ko('Barrages de promotion et relégation', '2025-03-01', '2025-03-31', expected=16),
                ko('Demi-finales', '2025-06-04', '2025-06-05', expected=2),
                ko('Match pour la 3e place', '2025-06-08', pairs=pairs(('Germany', 'France')), expected=1),
                ko('Finale', '2025-06-08', pairs=pairs(('Portugal', 'Spain')), expected=1)]},
        ],
    },
    {
        'title': 'Ligue des Nations Concacaf 2019-2020 a 2024-2025',
        'file': 'historique_ligue_des_nations_concacaf',
        'editions': [
            {'code': 'CONCACAF_NL_1920', 'name': 'Ligue des Nations Concacaf 2019-2020', 'season': 2021,
             'tournaments': {'CONCACAF Nations League', 'CONCACAF Nations League qualification'},
             'start': '2018-09-01', 'end': '2021-06-30', 'phases': [
                # Qualifications : classement unique de 34 equipes jouant 4 matchs chacune (pas un
                # mini-championnat complet).
                groups('Qualifications', '2018-09-01', '2019-03-31', single_table=True, no_round_robin=True,
                       group='Qualifications'),
                groups('Phase de ligue', '2019-09-01', '2019-11-30', raw_names=True),
                ko('Demi-finales', '2021-06-03', expected=2),
                ko('Match pour la 3e place', '2021-06-06', pairs=pairs(('Honduras', 'Costa Rica')), expected=1),
                ko('Finale', '2021-06-06', pairs=pairs(('United States', 'Mexico')), expected=1)]},
            {'code': 'CONCACAF_NL_2223', 'name': 'Ligue des Nations Concacaf 2022-2023', 'season': 2023,
             'tournaments': {'CONCACAF Nations League'}, 'start': '2022-06-01', 'end': '2023-06-30', 'phases': [
                groups('Phase de ligue', '2022-06-01', '2023-03-31', raw_names=True),
                ko('Demi-finales', '2023-06-15', expected=2),
                ko('Match pour la 3e place', '2023-06-18', pairs=pairs(('Panama', 'Mexico')), expected=1),
                ko('Finale', '2023-06-18', pairs=pairs(('United States', 'Canada')), expected=1)]},
            {'code': 'CONCACAF_NL_2324', 'name': 'Ligue des Nations Concacaf 2023-2024', 'season': 2024,
             'tournaments': {'CONCACAF Nations League'}, 'start': '2023-09-01', 'end': '2024-03-31', 'phases': [
                ko('Quarts de finale', '2023-11-01', '2023-11-30', expected=8,
                   pairs=pairs(('United States', 'Trinidad and Tobago'), ('Mexico', 'Honduras'),
                               ('Panama', 'Costa Rica'), ('Jamaica', 'Canada'))),
                # Ligue A : groupes de 6 ou chaque equipe ne joue que 4 adversaires.
                groups('Phase de ligue', '2023-09-01', '2023-11-30', raw_names=True, no_round_robin=True),
                ko('Demi-finales', '2024-03-21', expected=2),
                ko('Match pour la 3e place', '2024-03-24', pairs=pairs(('Jamaica', 'Panama')), expected=1),
                ko('Finale', '2024-03-24', pairs=pairs(('United States', 'Mexico')), expected=1)]},
            {'code': 'CONCACAF_NL_2425', 'name': 'Ligue des Nations Concacaf 2024-2025', 'season': 2025,
             'tournaments': {'CONCACAF Nations League'}, 'start': '2024-09-01', 'end': '2025-03-31', 'phases': [
                ko('Quarts de finale', '2024-11-01', '2024-11-30', expected=8,
                   pairs=pairs(('United States', 'Jamaica'), ('Mexico', 'Honduras'), ('Panama', 'Costa Rica'),
                               ('Canada', 'Suriname'))),
                ko('Barrages de promotion et relégation', '2024-11-01', '2024-11-30', expected=8,
                   pairs=pairs(('Belize', 'French Guiana'), ('Saint Kitts and Nevis', 'Cuba'), ('Barbados', 'Guyana'),
                               ('Cayman Islands', 'Guadeloupe'))),
                # Ligue A : groupes de 6 ou chaque equipe ne joue que 4 adversaires.
                groups('Phase de ligue', '2024-09-01', '2024-11-30', raw_names=True, no_round_robin=True),
                ko('Demi-finales', '2025-03-20', expected=2),
                ko('Match pour la 3e place', '2025-03-23', pairs=pairs(('Canada', 'United States')), expected=1),
                ko('Finale', '2025-03-23', pairs=pairs(('Mexico', 'Panama')), expected=1)]},
        ],
    },
]

# Matchs donnes sur tapis vert dont le jeu de donnees garde le score du terrain : score officiel.
SCORE_OVERRIDES = {
    # Roumanie-Kosovo arrete a 0-0 (Kosovo quitte le terrain), donne 3-0 a la Roumanie par l'UEFA
    ('2024-11-15', 'Romania', 'Kosovo'): (3, 0),
    # Malaisie : joueurs non qualifiables, matchs donnes 0-3 par l'AFC
    ('2025-03-25', 'Malaysia', 'Nepal'): (0, 3),
    ('2025-06-10', 'Malaysia', 'Vietnam'): (0, 3),
}

# (code, phase) -> {nom du groupe: equipe(s) representative(s)} ; extrait des pages Wikipedia de chaque
# competition (section "Group X" qui cite le plus les equipes du groupe), cf. en-tete de international_history.py.
GROUP_LETTERS = {
    ('FIFA_WC_2018', 'Phase de groupes'): {'A': 'Uruguay', 'B': 'Portugal', 'C': 'Denmark', 'D': 'Argentina', 'E': 'Serbia', 'F': 'Mexico', 'G': 'Panama', 'H': 'Japan'},
    ('UEFA_WCQ_2018', 'Phase de groupes'): {'A': 'Belarus', 'B': 'Hungary', 'C': 'Northern Ireland', 'D': 'Republic of Ireland', 'E': 'Armenia', 'F': 'Scotland', 'G': 'Italy', 'H': 'Greece', 'I': 'Ukraine'},
    ('CONCACAF_WCQ_2018', '4e tour'): {'A': 'Mexico', 'B': 'Costa Rica', 'C': 'Trinidad and Tobago'},
    ('CAF_WCQ_2018', '3e tour'): {'A': 'Tunisia', 'B': 'Nigeria', 'C': 'Gabon', 'D': 'South Africa', 'E': 'Congo'},
    ('AFC_WCQ_2018', '2e tour'): {'A': 'Palestine', 'B': 'Jordan', 'C': 'Hong Kong', 'D': 'Turkmenistan', 'E': 'Singapore', 'F': 'Thailand', 'G': 'Laos', 'H': 'Uzbekistan'},
    ('AFC_WCQ_2018', '3e tour'): {'A': 'China', 'B': 'Iraq'},
    ('OFC_WCQ_2018', '2e tour'): {'A': 'Papua New Guinea', 'B': 'Solomon Islands'},
    ('OFC_WCQ_2018', '3e tour'): {'A': 'Fiji', 'B': 'Papua New Guinea'},
    ('FIFA_WC_2022', 'Phase de groupes'): {'A': 'Ecuador', 'B': 'England', 'C': 'Argentina', 'D': 'France', 'E': 'Japan', 'F': 'Croatia', 'G': 'Serbia', 'H': 'Portugal'},
    ('UEFA_WCQ_2022', 'Phase de groupes'): {'A': 'Republic of Ireland', 'B': 'Greece', 'C': 'Italy', 'D': 'Finland', 'E': 'Belarus', 'F': 'Scotland', 'G': 'Latvia', 'H': 'Russia', 'I': 'England', 'J': 'Armenia'},
    ('CONCACAF_WCQ_2022', '1er tour'): {'A': 'Montserrat', 'B': 'Cayman Islands', 'C': 'Guatemala', 'D': 'Dominican Republic', 'E': 'Nicaragua', 'F': 'Trinidad and Tobago'},
    ('CAF_WCQ_2022', '2e tour'): {'A': 'Djibouti', 'B': 'Mauritania', 'C': 'Nigeria', 'D': 'Malawi', 'E': 'Uganda', 'F': 'Gabon', 'G': 'South Africa', 'H': 'Congo', 'I': 'Sudan', 'J': 'Benin'},
    ('AFC_WCQ_2022', '2e tour'): {'A': 'China', 'B': 'Kuwait', 'C': 'Iran', 'D': 'Singapore', 'E': 'Oman', 'F': 'Japan', 'G': 'United Arab Emirates', 'H': 'South Korea'},
    ('AFC_WCQ_2022', '3e tour'): {'A': 'Iraq', 'B': 'China'},
    ('OFC_WCQ_2022', 'Phase de groupes'): {'A': 'Solomon Islands', 'B': 'Papua New Guinea'},
    ('FIFA_WC_2026', 'Phase de groupes'): {'A': 'South Africa', 'B': 'Qatar', 'C': 'Scotland', 'D': 'Turkey', 'E': 'Ecuador', 'F': 'Japan', 'G': 'Iran', 'H': 'Uruguay', 'I': 'Senegal', 'J': 'Argentina', 'K': 'Portugal', 'L': 'Croatia'},
    ('UEFA_WCQ_2026', 'Phase de groupes'): {'A': 'Northern Ireland', 'B': 'Slovenia', 'C': 'Belarus', 'D': 'Azerbaijan', 'E': 'Turkey', 'F': 'Republic of Ireland', 'G': 'Finland', 'H': 'Romania', 'I': 'Italy', 'J': 'Kazakhstan', 'K': 'Latvia', 'L': 'Gibraltar'},
    ('CONCACAF_WCQ_2026', '2e tour'): {'A': 'Cayman Islands', 'B': 'Grenada', 'C': 'Saint Lucia', 'D': 'Belize', 'E': 'Guatemala', 'F': 'Suriname'},
    ('CONCACAF_WCQ_2026', '3e tour'): {'A': 'Guatemala', 'B': 'Trinidad and Tobago', 'C': 'Nicaragua'},
    ('CAF_WCQ_2026', 'Phase de groupes'): {'A': 'Djibouti', 'B': 'Mauritania', 'C': 'South Africa', 'D': 'Eswatini', 'E': 'Congo', 'F': 'Ivory Coast', 'G': 'Uganda', 'H': 'Tunisia', 'I': 'Comoros'},
    ('AFC_WCQ_2026', '2e tour'): {'A': 'Kuwait', 'B': 'Japan', 'C': 'Singapore', 'D': 'Oman', 'E': 'Hong Kong', 'F': 'Vietnam', 'G': 'Pakistan', 'H': 'United Arab Emirates', 'I': 'Palestine'},
    ('AFC_WCQ_2026', '3e tour'): {'A': 'Kyrgyzstan', 'B': 'Palestine', 'C': 'China'},
    ('AFC_WCQ_2026', '4e tour'): {'A': 'Oman', 'B': 'Iraq'},
    ('OFC_WCQ_2026', '2e tour'): {'A': 'Papua New Guinea', 'B': 'Samoa'},
    ('UEFA_EURO_2016', 'Phase de groupes'): {'A': 'Switzerland', 'B': 'Russia', 'C': 'Poland', 'D': 'Turkey', 'E': 'Republic of Ireland', 'F': 'Portugal'},
    ('UEFA_EUROQ_2016', 'Phase de groupes'): {'A': 'Latvia', 'B': 'Bosnia and Herzegovina', 'C': 'Belarus', 'D': 'Republic of Ireland', 'E': 'England', 'F': 'Northern Ireland', 'G': 'Russia', 'H': 'Italy', 'I': 'Portugal'},
    ('UEFA_EURO_2020', 'Phase de groupes'): {'A': 'Turkey', 'B': 'Russia', 'C': 'North Macedonia', 'D': 'Croatia', 'E': 'Slovakia', 'F': 'Germany'},
    ('UEFA_EUROQ_2020', 'Phase de groupes'): {'A': 'England', 'B': 'Portugal', 'C': 'Belarus', 'D': 'Republic of Ireland', 'E': 'Slovakia', 'F': 'Romania', 'G': 'Latvia', 'H': 'Turkey', 'I': 'Russia', 'J': 'Armenia'},
    ('UEFA_EURO_2024', 'Phase de groupes'): {'A': 'Switzerland', 'B': 'Croatia', 'C': 'Slovenia', 'D': 'France', 'E': 'Belgium', 'F': 'Turkey'},
    ('UEFA_EUROQ_2024', 'Phase de groupes'): {'A': 'Scotland', 'B': 'Republic of Ireland', 'C': 'Italy', 'D': 'Armenia', 'E': 'Poland', 'F': 'Azerbaijan', 'G': 'Lithuania', 'H': 'Northern Ireland', 'I': 'Belarus', 'J': 'Portugal'},
    ('CONMEBOL_COPA_2019', 'Phase de groupes'): {'A': 'Venezuela', 'B': 'Argentina', 'C': 'Japan'},
    ('CONMEBOL_COPA_2021', 'Phase de groupes'): {'A': 'Chile', 'B': 'Venezuela'},
    ('CONMEBOL_COPA_2024', 'Phase de groupes'): {'A': 'Argentina', 'B': 'Ecuador', 'C': 'Panama', 'D': 'Costa Rica'},
    ('CAF_AFCON_2021', 'Phase de groupes'): {'A': 'Cape Verde', 'B': 'Malawi', 'C': 'Gabon', 'D': 'Nigeria', 'E': 'Sierra Leone', 'F': 'Mauritania'},
    ('CAF_AFCONQ_2021', 'Phase de groupes'): {'A': 'Chad', 'B': 'Malawi', 'C': 'South Africa', 'D': 'Gabon', 'E': 'Central African Republic', 'F': 'Cape Verde', 'G': 'Comoros', 'H': 'Zambia', 'I': 'Congo', 'J': 'Libya', 'K': 'Madagascar', 'L': 'Nigeria'},
    ('CAF_AFCON_2023', 'Phase de groupes'): {'A': 'Nigeria', 'B': 'Cape Verde', 'C': 'Guinea', 'D': 'Angola', 'E': 'South Africa', 'F': 'DR Congo'},
    ('CAF_AFCONQ_2023', 'Phase de groupes'): {'A': 'Nigeria', 'B': 'Burkina Faso', 'C': 'Namibia', 'D': 'Malawi', 'E': 'Angola', 'F': 'Uganda', 'G': 'Congo', 'H': 'Comoros', 'I': 'Gabon', 'J': 'Libya', 'K': 'South Africa', 'L': 'Benin'},
    ('CAF_AFCON_2025', 'Phase de groupes'): {'A': 'Comoros', 'B': 'Angola', 'C': 'Nigeria', 'D': 'Benin', 'E': 'Burkina Faso', 'F': 'Gabon'},
    ('CAF_AFCONQ_2025', 'Phase de groupes'): {'A': 'Comoros', 'B': 'Gabon', 'C': 'Mauritania', 'D': 'Nigeria', 'E': 'Liberia', 'F': 'Angola', 'G': 'Zambia', 'H': 'DR Congo', 'I': 'Mozambique', 'J': 'Namibia', 'K': 'South Africa', 'L': 'Malawi'},
    ('AFC_ASIANCUP_2019', 'Phase de groupes'): {'A': 'United Arab Emirates', 'B': 'Syria', 'C': 'China', 'D': 'Yemen', 'E': 'Qatar', 'F': 'Japan'},
    ('AFC_ASIANCUPQ_2019', '3e tour'): {'A': 'Macau', 'B': 'Hong Kong', 'C': 'Jordan', 'D': 'Oman', 'E': 'Singapore', 'F': 'Yemen'},
    ('AFC_ASIANCUP_2023', 'Phase de groupes'): {'A': 'China', 'B': 'Syria', 'C': 'United Arab Emirates', 'D': 'Japan', 'E': 'Malaysia', 'F': 'Thailand'},
    ('AFC_ASIANCUPQ_2023', '3e tour'): {'A': 'Kuwait', 'B': 'Mongolia', 'C': 'Thailand', 'D': 'Cambodia', 'E': 'Turkmenistan', 'F': 'Kyrgyzstan'},
    ('AFC_ASIANCUPQ_2027', '3e tour'): {'A': 'Maldives', 'B': 'Brunei', 'C': 'Singapore', 'D': 'Turkmenistan', 'E': 'Syria', 'F': 'Laos'},
    ('CONCACAF_GOLDCUP_2021', 'Phase de groupes'): {'A': 'Trinidad and Tobago', 'B': 'Martinique', 'C': 'Costa Rica', 'D': 'Grenada'},
    ('CONCACAF_GOLDCUP_2023', 'Phase de groupes'): {'A': 'Saint Kitts and Nevis', 'B': 'Qatar', 'C': 'Martinique', 'D': 'Guatemala'},
    ('CONCACAF_GOLDCUP_2025', 'Phase de groupes'): {'A': 'Dominican Republic', 'B': 'Curaçao', 'C': 'Guatemala', 'D': 'Trinidad and Tobago'},
    ('UEFA_NL_1819', 'Phase de ligue'): {'Ligue A - Groupe 1': 'France', 'Ligue A - Groupe 2': 'Switzerland', 'Ligue A - Groupe 3': 'Portugal', 'Ligue A - Groupe 4': 'Croatia', 'Ligue B - Groupe 1': 'Slovakia', 'Ligue B - Groupe 2': 'Turkey', 'Ligue B - Groupe 3': 'Northern Ireland', 'Ligue B - Groupe 4': 'Republic of Ireland', 'Ligue C - Groupe 1': 'Scotland', 'Ligue C - Groupe 2': 'Estonia', 'Ligue C - Groupe 3': 'Slovenia', 'Ligue C - Groupe 4': 'Serbia', 'Ligue D - Groupe 1': 'Kazakhstan', 'Ligue D - Groupe 2': 'Belarus', 'Ligue D - Groupe 3': 'Kosovo', 'Ligue D - Groupe 4': 'Gibraltar'},
    ('UEFA_NL_2021', 'Phase de ligue'): {'Ligue A - Groupe 1': 'Italy', 'Ligue A - Groupe 2': 'Denmark', 'Ligue A - Groupe 3': 'Croatia', 'Ligue A - Groupe 4': 'Switzerland', 'Ligue B - Groupe 1': 'Northern Ireland', 'Ligue B - Groupe 2': 'Scotland', 'Ligue B - Groupe 3': 'Turkey', 'Ligue B - Groupe 4': 'Republic of Ireland', 'Ligue C - Groupe 1': 'Montenegro', 'Ligue C - Groupe 2': 'Armenia', 'Ligue C - Groupe 3': 'Kosovo', 'Ligue C - Groupe 4': 'Belarus', 'Ligue D - Groupe 1': 'Faroe Islands', 'Ligue D - Groupe 2': 'Gibraltar'},
    ('UEFA_NL_2223', 'Phase de ligue'): {'Ligue A - Groupe 1': 'Croatia', 'Ligue A - Groupe 2': 'Portugal', 'Ligue A - Groupe 3': 'England', 'Ligue A - Groupe 4': 'Netherlands', 'Ligue B - Groupe 1': 'Republic of Ireland', 'Ligue B - Groupe 2': 'Albania', 'Ligue B - Groupe 3': 'Montenegro', 'Ligue B - Groupe 4': 'Slovenia', 'Ligue C - Groupe 1': 'Luxembourg', 'Ligue C - Groupe 2': 'Kosovo', 'Ligue C - Groupe 3': 'Belarus', 'Ligue C - Groupe 4': 'Gibraltar', 'Ligue D - Groupe 1': 'Liechtenstein', 'Ligue D - Groupe 2': 'Estonia'},
    ('UEFA_NL_2425', 'Phase de ligue'): {'Ligue A - Groupe 1': 'Croatia', 'Ligue A - Groupe 2': 'Israel', 'Ligue A - Groupe 3': 'Hungary', 'Ligue A - Groupe 4': 'Switzerland', 'Ligue B - Groupe 1': 'Albania', 'Ligue B - Groupe 2': 'Republic of Ireland', 'Ligue B - Groupe 3': 'Slovenia', 'Ligue B - Groupe 4': 'Turkey', 'Ligue C - Groupe 1': 'Estonia', 'Ligue C - Groupe 2': 'Kosovo', 'Ligue C - Groupe 3': 'Luxembourg', 'Ligue C - Groupe 4': 'North Macedonia', 'Ligue D - Groupe 1': 'Liechtenstein', 'Ligue D - Groupe 2': 'Malta'},
    ('CONCACAF_NL_1920', 'Phase de ligue'): {'Ligue A - Groupe A': 'Cuba', 'Ligue A - Groupe B': 'Mexico', 'Ligue A - Groupe C': 'Martinique', 'Ligue A - Groupe D': 'Costa Rica', 'Ligue B - Groupe A': 'Saint Kitts and Nevis', 'Ligue B - Groupe B': 'El Salvador', 'Ligue B - Groupe C': 'Jamaica', 'Ligue B - Groupe D': 'Nicaragua', 'Ligue C - Groupe A': 'Barbados', 'Ligue C - Groupe B': 'Bahamas', 'Ligue C - Groupe C': 'Guatemala', 'Ligue C - Groupe D': 'Turks and Caicos Islands'},
    ('CONCACAF_NL_2223', 'Phase de ligue'): {'Ligue A - Groupe A': 'Mexico', 'Ligue A - Groupe B': 'Martinique', 'Ligue A - Groupe C': 'Curaçao', 'Ligue A - Groupe D': 'Grenada', 'Ligue B - Groupe A': 'Guadeloupe', 'Ligue B - Groupe B': 'Montserrat', 'Ligue B - Groupe C': 'Nicaragua', 'Ligue B - Groupe D': 'Guatemala', 'Ligue C - Groupe A': 'Sint Maarten', 'Ligue C - Groupe B': 'Saint Kitts and Nevis', 'Ligue C - Groupe C': 'Saint Lucia', 'Ligue C - Groupe D': 'Cayman Islands'},
    ('CONCACAF_NL_2324', 'Phase de ligue'): {'Ligue A - Groupe A': 'El Salvador', 'Ligue A - Groupe B': 'Jamaica', 'Ligue B - Groupe A': 'Saint Lucia', 'Ligue B - Groupe B': 'Nicaragua', 'Ligue B - Groupe C': 'Saint Vincent and the Grenadines', 'Ligue B - Groupe D': 'Bahamas', 'Ligue C - Groupe A': 'Anguilla', 'Ligue C - Groupe B': 'Cayman Islands', 'Ligue C - Groupe C': 'Turks and Caicos Islands'},
    ('CONCACAF_NL_2425', 'Phase de ligue'): {'Ligue A - Groupe A': 'Guatemala', 'Ligue A - Groupe B': 'Trinidad and Tobago', 'Ligue B - Groupe A': 'Montserrat', 'Ligue B - Groupe B': 'Saint Lucia', 'Ligue B - Groupe C': 'Sint Maarten', 'Ligue B - Groupe D': 'Dominican Republic', 'Ligue C - Groupe A': 'Bahamas', 'Ligue C - Groupe B': 'Turks and Caicos Islands', 'Ligue C - Groupe C': 'Saint Kitts and Nevis'},
}

# (date, domicile, exterieur) du jeu de donnees -> (tirs au but domicile, exterieur), d'apres les feuilles
# de match Wikipedia.
SHOOTOUT_SCORES = {
    # Cherches a la main dans les tableaux des tours preliminaires (score cumule, tirs au but)
    ('2019-10-13', 'Chad', 'Liberia'): (5, 4),
    ('2019-10-13', 'Gambia', 'Djibouti'): (3, 2),
    ('2024-03-26', 'British Virgin Islands', 'United States Virgin Islands'): (4, 2),
    ('2016-06-25', 'Switzerland', 'Poland'): (4, 5),
    ('2016-06-30', 'Poland', 'Portugal'): (3, 5),
    ('2016-07-02', 'Germany', 'Italy'): (6, 5),
    ('2018-07-01', 'Croatia', 'Denmark'): (3, 2),
    ('2018-07-01', 'Russia', 'Spain'): (4, 3),
    ('2018-07-03', 'Colombia', 'England'): (3, 4),
    ('2018-07-07', 'Russia', 'Croatia'): (3, 4),
    ('2019-01-20', 'Jordan', 'Vietnam'): (2, 4),
    ('2019-01-21', 'Australia', 'Uzbekistan'): (4, 2),
    ('2019-06-09', 'Switzerland', 'England'): (5, 6),
    ('2019-06-27', 'Brazil', 'Paraguay'): (4, 3),
    ('2019-06-28', 'Colombia', 'Chile'): (4, 5),
    ('2019-06-29', 'Uruguay', 'Peru'): (4, 5),
    ('2019-09-08', 'Tanzania', 'Burundi'): (3, 0),
    ('2020-10-08', 'Bosnia and Herzegovina', 'Northern Ireland'): (3, 4),
    ('2020-10-08', 'Scotland', 'Israel'): (5, 3),
    ('2020-10-08', 'Slovakia', 'Republic of Ireland'): (4, 2),
    ('2020-11-12', 'Serbia', 'Scotland'): (4, 5),
    ('2021-06-03', 'Mexico', 'Costa Rica'): (5, 4),
    ('2021-06-06', 'Honduras', 'Costa Rica'): (5, 4),
    ('2021-06-28', 'France', 'Switzerland'): (4, 5),
    ('2021-07-02', 'Peru', 'Paraguay'): (4, 3),
    ('2021-07-02', 'Switzerland', 'Spain'): (1, 3),
    ('2021-07-03', 'Uruguay', 'Colombia'): (2, 4),
    ('2021-07-06', 'Argentina', 'Colombia'): (3, 2),
    ('2021-07-06', 'Guatemala', 'Guadeloupe'): (9, 10),
    ('2021-07-06', 'Italy', 'Spain'): (4, 2),
    ('2021-07-06', 'Trinidad and Tobago', 'French Guiana'): (8, 7),
    ('2021-07-11', 'England', 'Italy'): (2, 3),
    ('2022-01-23', 'Burkina Faso', 'Gabon'): (7, 6),
    ('2022-01-26', 'Ivory Coast', 'Egypt'): (4, 5),
    ('2022-01-26', 'Mali', 'Equatorial Guinea'): (5, 6),
    ('2022-02-03', 'Cameroon', 'Egypt'): (1, 3),
    ('2022-02-05', 'Cameroon', 'Burkina Faso'): (5, 3),
    ('2022-02-06', 'Senegal', 'Egypt'): (4, 2),
    ('2022-03-29', 'Kazakhstan', 'Moldova'): (5, 4),
    ('2022-03-29', 'Senegal', 'Egypt'): (3, 1),
    ('2022-06-13', 'Australia', 'Peru'): (5, 4),
    ('2022-12-05', 'Japan', 'Croatia'): (1, 3),
    ('2022-12-06', 'Morocco', 'Spain'): (3, 0),
    ('2022-12-09', 'Croatia', 'Brazil'): (4, 2),
    ('2022-12-09', 'Netherlands', 'Argentina'): (3, 4),
    ('2022-12-18', 'Argentina', 'France'): (4, 2),
    ('2023-06-16', 'Curaçao', 'Saint Kitts and Nevis'): (2, 3),
    ('2023-06-17', 'Guyana', 'Grenada'): (5, 3),
    ('2023-06-17', 'Suriname', 'Puerto Rico'): (3, 4),
    ('2023-06-18', 'Croatia', 'Spain'): (4, 5),
    ('2023-06-20', 'Saint Kitts and Nevis', 'French Guiana'): (4, 2),
    ('2023-07-09', 'United States', 'Canada'): (3, 2),
    ('2023-07-12', 'United States', 'Panama'): (4, 5),
    ('2023-11-21', 'Mexico', 'Honduras'): (4, 2),
    ('2024-01-28', 'Egypt', 'DR Congo'): (7, 8),
    ('2024-01-28', 'Tajikistan', 'United Arab Emirates'): (5, 3),
    ('2024-01-29', 'Ivory Coast', 'Senegal'): (5, 4),
    ('2024-01-30', 'Saudi Arabia', 'South Korea'): (2, 4),
    ('2024-01-31', 'Iran', 'Syria'): (5, 3),
    ('2024-02-03', 'Cape Verde', 'South Africa'): (1, 2),
    ('2024-02-03', 'Qatar', 'Uzbekistan'): (3, 2),
    ('2024-02-07', 'Nigeria', 'South Africa'): (4, 2),
    ('2024-03-26', 'Georgia', 'Greece'): (4, 2),
    ('2024-03-26', 'Turks and Caicos Islands', 'Anguilla'): (3, 4),
    ('2024-03-26', 'Wales', 'Poland'): (4, 5),
    ('2024-07-01', 'Portugal', 'Slovenia'): (3, 0),
    ('2024-07-04', 'Argentina', 'Ecuador'): (4, 2),
    ('2024-07-05', 'Portugal', 'France'): (3, 5),
    ('2024-07-05', 'Venezuela', 'Canada'): (3, 4),
    ('2024-07-06', 'England', 'Switzerland'): (5, 3),
    ('2024-07-06', 'Uruguay', 'Brazil'): (4, 2),
    ('2024-07-13', 'Canada', 'Uruguay'): (3, 4),
    ('2024-09-10', 'Cambodia', 'Sri Lanka'): (2, 4),
    ('2025-06-08', 'Portugal', 'Spain'): (5, 3),
    ('2025-06-28', 'Panama', 'Honduras'): (4, 5),
    ('2025-06-29', 'Canada', 'Guatemala'): (5, 6),
    ('2025-06-29', 'United States', 'Costa Rica'): (4, 3),
    ('2025-11-16', 'Nigeria', 'DR Congo'): (3, 4),
    ('2026-01-03', 'Mali', 'Tunisia'): (3, 2),
    ('2026-01-14', 'Morocco', 'Nigeria'): (4, 2),
    ('2026-01-17', 'Egypt', 'Nigeria'): (2, 4),
    ('2026-03-26', 'Czech Republic', 'Republic of Ireland'): (4, 3),
    ('2026-03-26', 'Wales', 'Bosnia and Herzegovina'): (2, 4),
    ('2026-03-31', 'Bosnia and Herzegovina', 'Italy'): (4, 1),
    ('2026-03-31', 'Czech Republic', 'Denmark'): (3, 1),
    ('2026-06-29', 'Germany', 'Paraguay'): (3, 4),
    ('2026-06-29', 'Netherlands', 'Morocco'): (2, 3),
    ('2026-07-03', 'Australia', 'Egypt'): (2, 4),
    ('2026-07-07', 'Switzerland', 'Colombia'): (4, 3),
}
