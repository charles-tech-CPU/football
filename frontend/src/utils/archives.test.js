import { describe, expect, it } from 'vitest'
import { archivedSeasons, currentSeason, sortContinental } from './archives.js'

const c = (id, type, season, extra = {}) => ({ id, type, season, country: null, code: '', name: `C${id}`, ...extra })

const competitions = [
  c(1, 'CONTINENTAL_CUP', 2027, { code: 'EL' }),
  c(2, 'CONTINENTAL_CUP', 2027, { code: 'LDC' }),
  c(3, 'LEAGUE', 2027, { country: 'Finlande' }),
  c(4, 'DOMESTIC_CUP', 2027, { country: 'Finlande' }),
  c(5, 'LEAGUE', 2028, { country: 'Finlande' }),
  c(6, 'LEAGUE', 2027, { country: 'France' }),
  c(7, 'INTERNATIONAL', 1998)
]

describe('currentSeason', () => {
  it('suit les coupes d Europe, pas un championnat deja prepare', () => {
    expect(currentSeason(competitions)).toBe(2027)
    expect(currentSeason([])).toBeNull()
  })
})

describe('sortContinental', () => {
  it('range LDC, EL puis EC', () => {
    expect(sortContinental(competitions.slice(0, 2)).map(x => x.code)).toEqual(['LDC', 'EL'])
  })
})

describe('archivedSeasons', () => {
  it('archive la saison d un pays des que la suivante existe', () => {
    const archives = archivedSeasons(competitions)
    expect(archives).toHaveLength(1)
    expect(archives[0].season).toBe(2027)
    expect(archives[0].continental).toEqual([])
    expect(archives[0].countries).toEqual([{ country: 'Finlande', league: competitions[2], cup: competitions[3] }])
  })

  it('archive les coupes d Europe d une saison passee', () => {
    const archives = archivedSeasons([...competitions, c(8, 'CONTINENTAL_CUP', 2028, { code: 'LDC' })])
    expect(archives[0].continental.map(x => x.id)).toEqual([2, 1])
  })
})
