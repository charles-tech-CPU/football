// Plusieurs saisons d'un meme pays (saison suivante preparee, cf. NextSeasonPanel) : fonctions pures.

// Saisons ou le pays a un championnat ou une coupe, de la plus recente a la plus ancienne.
export function countrySeasons(competitions, country) {
  const seasons = competitions
    .filter(c => c.country === country && (c.type === 'LEAGUE' || c.type === 'DOMESTIC_CUP'))
    .map(c => c.season)
  return [...new Set(seasons)].sort((a, b) => b - a)
}

// Championnat et coupe d'un pays pour une saison (la plus recente par defaut).
export function countryCompetitions(competitions, country, season = null) {
  const target = season ?? countrySeasons(competitions, country)[0]
  const forSeason = competitions.filter(c => c.country === country && c.season === target)
  return {
    season: target ?? null,
    league: forSeason.find(c => c.type === 'LEAGUE') ?? null,
    cup: forSeason.find(c => c.type === 'DOMESTIC_CUP') ?? null
  }
}

// Zone de chaque rang du classement final : releguee (dernieres places), barrage juste au-dessus,
// maintenue sinon.
export function relegationZone(rank, size, competition) {
  const relegation = competition.relegationSlots ?? 0
  const barrage = competition.barrageSlots ?? 0
  if (rank > size - relegation) return 'relegated'
  if (rank > size - relegation - barrage) return 'barrage'
  return 'kept'
}
