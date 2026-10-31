import { describe, expect, it } from 'vitest'
import { countryCompetitions, countrySeasons, relegationZone } from './seasons.js'

const competitions = [
  { id: 1, type: 'LEAGUE', country: 'Finlande', season: 2027 },
  { id: 2, type: 'DOMESTIC_CUP', country: 'Finlande', season: 2027 },
  { id: 3, type: 'LEAGUE', country: 'Finlande', season: 2028 },
  { id: 4, type: 'LEAGUE', country: 'France', season: 2027 },
  { id: 5, type: 'CONTINENTAL_CUP', country: null, season: 2027 }
]

describe('countrySeasons', () => {
  it('liste les saisons du pays, la plus recente d abord', () => {
    expect(countrySeasons(competitions, 'Finlande')).toEqual([2028, 2027])
    expect(countrySeasons(competitions, 'Suede')).toEqual([])
  })
})

describe('countryCompetitions', () => {
  it('prend la saison la plus recente par defaut', () => {
    const { season, league, cup } = countryCompetitions(competitions, 'Finlande')
    expect(season).toBe(2028)
    expect(league.id).toBe(3)
    expect(cup).toBeNull()
  })

  it('donne le championnat et la coupe d une saison choisie', () => {
    const { league, cup } = countryCompetitions(competitions, 'Finlande', 2027)
    expect([league.id, cup.id]).toEqual([1, 2])
  })
})

describe('relegationZone', () => {
  it('separe releges, barragistes et maintenus', () => {
    const competition = { relegationSlots: 1, barrageSlots: 1 }
    expect([10, 11, 12].map(r => relegationZone(r, 12, competition))).toEqual(['kept', 'barrage', 'relegated'])
  })
})
