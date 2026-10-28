import { describe, expect, it } from 'vitest'
import { LEAGUE_GROUPS, groupCountries } from './leagueGroups.js'

const RANKING = [
  { rank: 3, country: 'Allemagne' },
  { rank: 1, country: 'Angleterre' },
  { rank: 2, country: 'Italie' }
]

describe('groupCountries', () => {
  it('top N d apres le classement UEFA des pays', () => {
    expect(groupCountries({ top: 2 }, RANKING)).toEqual(['Angleterre', 'Italie'])
  })

  it('liste fixe pour un groupe regional, vide pour tous', () => {
    expect(groupCountries(LEAGUE_GROUPS.find(g => g.key === 'baltes'), RANKING)).toEqual(['Estonie', 'Lettonie', 'Lituanie'])
    expect(groupCountries(LEAGUE_GROUPS[0], RANKING)).toEqual([])
  })

  it('chaque pays est dans un seul groupe regional', () => {
    const regional = LEAGUE_GROUPS.flatMap(g => g.countries ?? [])
    expect(new Set(regional).size).toBe(regional.length)
  })
})
