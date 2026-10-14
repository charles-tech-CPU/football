import { describe, expect, it } from 'vitest'
import { competitionBadgeClass, competitionLink } from './competitionBadge.js'

describe('competitionLink', () => {
  it('championnat : page du pays (onglet Classement)', () => {
    expect(competitionLink({ competitionId: 9, competitionType: 'LEAGUE', competitionCountry: 'Kazakhstan' })).toBe('/pays/Kazakhstan')
  })

  it('coupe nationale : page du pays, onglet Coupe', () => {
    expect(competitionLink({ competitionId: 9, competitionType: 'DOMESTIC_CUP', competitionCountry: 'Côte d\'Ivoire' }))
      .toEqual({ path: "/pays/C%C3%B4te%20d'Ivoire", query: { onglet: 'coupe' } })
  })

  it('coupe d Europe : page de la competition', () => {
    expect(competitionLink({ competitionId: 68, competitionType: 'CONTINENTAL_CUP', competitionCountry: null })).toBe('/competitions/68')
  })
})

describe('competitionBadgeClass', () => {
  it('couleur par type de competition', () => {
    expect(competitionBadgeClass({ competitionCode: 'LDC' })).toBe('comp-badge comp-ldc')
    expect(competitionBadgeClass({ competitionType: 'DOMESTIC_CUP' })).toBe('comp-badge comp-cup')
    expect(competitionBadgeClass({ competitionType: 'INTERNATIONAL' })).toBe('')
  })
})
