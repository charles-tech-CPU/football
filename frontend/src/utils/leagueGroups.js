// Regroupements de championnats pour l'onglet Statistiques > Tous les championnats : les meilleurs
// au classement UEFA des pays (top), ou des championnats comparables (meme region, meme niveau,
// saison sur l'annee civile pour les nordiques et les baltes). Libelles pays = Competition.country.
export const LEAGUE_GROUPS = [
  { key: 'tous', label: 'Tous' },
  { key: 'top5', label: 'Top 5 UEFA', top: 5 },
  { key: 'top10', label: 'Top 10 UEFA', top: 10 },
  { key: 'ouest', label: "Europe de l'Ouest", countries: ['Allemagne', 'Espagne', 'France', 'Italie', 'Portugal', 'Belgique', 'Pays-Bas', 'Luxembourg'] },
  { key: 'britanniques', label: 'Îles britanniques', countries: ['Angleterre', 'Ecosse', 'Galles', 'Irlande Nord', 'Eire'] },
  { key: 'nordiques', label: 'Nordiques', countries: ['Suede', 'Norvege', 'Danemark', 'Finlande', 'Islande', 'Iles Feroe'] },
  { key: 'baltes', label: 'Pays baltes', countries: ['Estonie', 'Lettonie', 'Lituanie'] },
  { key: 'centrale', label: 'Europe centrale', countries: ['Autriche', 'Suisse', 'Tchequie', 'Slovaquie', 'Hongrie', 'Pologne'] },
  { key: 'balkans', label: 'Balkans', countries: ['Croatie', 'Serbie', 'Slovenie', 'Bosnie', 'Montenegro', 'Macedoine', 'Albanie', 'Kosovo', 'Bulgarie', 'Roumanie', 'Grece'] },
  { key: 'est', label: "Europe de l'Est et Caucase", countries: ['Ukraine', 'Russie', 'Belarus', 'Moldavie', 'Georgie', 'Armenie', 'Azerbaidjan', 'Kazakhstan'] },
  { key: 'mediterranee', label: 'Méditerranée orientale', countries: ['Turquie', 'Chypre', 'Israel'] },
  { key: 'petits', label: 'Petits pays', countries: ['Andorre', 'Gibraltar', 'Malte', 'San Marin'] }
]

// Pays d'un groupe ; [] = tous. countryRanking : classement UEFA des pays ({ rank, country }).
export function groupCountries(group, countryRanking) {
  if (group.top) {
    return countryRanking.toSorted((a, b) => a.rank - b.rank).slice(0, group.top).map(c => c.country)
  }
  return group.countries ?? []
}
