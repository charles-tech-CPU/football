// Saisons archivees (vue ArchivesView) : fonctions pures, sans etat Vue. Rien n'est jamais supprime :
// une saison est archivee des que la suivante existe (championnat ou coupe du meme pays, ou coupe
// d'Europe de la saison suivante).

// Saison en cours = celle des coupes d'Europe (a defaut, la plus recente des competitions).
export function currentSeason(competitions) {
  const continental = competitions.filter(c => c.type === 'CONTINENTAL_CUP').map(c => c.season)
  const all = continental.length ? continental : competitions.map(c => c.season)
  return all.length ? Math.max(...all) : null
}

// Coupes d'Europe de la saison en cours, LDC > EL > EC.
const CONTINENTAL_ORDER = ['LDC', 'EL', 'EC']
export function sortContinental(list) {
  const rank = c => {
    const i = CONTINENTAL_ORDER.indexOf(c.code)
    return i === -1 ? CONTINENTAL_ORDER.length : i
  }
  return list.toSorted((a, b) => rank(a) - rank(b) || a.name.localeCompare(b.name))
}

// Saisons archivees, de la plus recente a la plus ancienne : coupes d'Europe d'une saison passee,
// championnats / coupes d'un pays dont une saison plus recente existe.
export function archivedSeasons(competitions) {
  const current = currentSeason(competitions)
  const latestByCountry = new Map()
  for (const c of competitions) {
    if (!c.country || (c.type !== 'LEAGUE' && c.type !== 'DOMESTIC_CUP')) continue
    latestByCountry.set(c.country, Math.max(latestByCountry.get(c.country) ?? c.season, c.season))
  }
  const bySeason = new Map()
  const entry = season => {
    if (!bySeason.has(season)) bySeason.set(season, { season, continental: [], countries: new Map() })
    return bySeason.get(season)
  }
  for (const c of competitions) {
    if (c.type === 'CONTINENTAL_CUP' && c.season < current) {
      entry(c.season).continental.push(c)
    } else if (c.country && (c.type === 'LEAGUE' || c.type === 'DOMESTIC_CUP') && c.season < latestByCountry.get(c.country)) {
      const countries = entry(c.season).countries
      if (!countries.has(c.country)) countries.set(c.country, { country: c.country, league: null, cup: null })
      countries.get(c.country)[c.type === 'LEAGUE' ? 'league' : 'cup'] = c
    }
  }
  return [...bySeason.values()]
    .sort((a, b) => b.season - a.season)
    .map(s => ({
      season: s.season,
      continental: sortContinental(s.continental),
      countries: [...s.countries.values()].sort((a, b) => a.country.localeCompare(b.country))
    }))
}
