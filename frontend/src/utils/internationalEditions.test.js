import { describe, expect, it } from 'vitest'
import { internationalFamilies, locateCompetition } from './internationalEditions.js'

const comp = (id, code, season) => ({ id, code, season, name: code })

const COMPETITIONS = [
  comp(1, 'UEFA_WCQ_2022', 2022),
  comp(2, 'FIFA_WC_2022', 2022),
  comp(3, 'FIFA_WCQ_2022', 2022),
  comp(4, 'CAF_WCQ_2022', 2022),
  comp(5, 'FIFA_WC_2026', 2026),
  comp(6, 'UEFA_NL_2627', 2027),
  comp(7, 'UEFA_NL_1819', 2019),
  comp(8, 'UEFA_EUROQ_2024', 2024),
  comp(9, 'UEFA_EURO_2024', 2024),
  comp(10, 'CAF_CAN_2027', 2027),
  comp(11, 'INCONNU_2020', 2020),
  comp(12, 'CONMEBOL_COPA_1959', 1959),
  comp(13, 'CONMEBOL_COPA_1959B', 1959),
  comp(14, 'CONCACAF_GOLDCUP_2019', 2019),
  comp(15, 'CONCACAF_GOLDCUPQ_2019', 2019)
]

describe('internationalFamilies', () => {
  const families = internationalFamilies(COMPETITIONS, 2027)

  it('range les competitions par famille, dans un ordre fixe, sans les codes inconnus', () => {
    expect(families.map(f => f.label)).toEqual(['Coupe du monde', 'Euro', 'Copa América', 'CAN', 'Gold Cup', 'Ligue des Nations UEFA'])
  })

  it('editions les plus recentes d abord, Ligue des Nations libellee sur deux annees', () => {
    expect(families[0].editions.map(e => e.label)).toEqual(['2026', '2022'])
    expect(families[5].editions.map(e => e.label)).toEqual(['2026-2027', '2018-2019'])
    expect(families[2].editions.map(e => e.label)).toEqual(['1959 (bis)', '1959'])
  })

  it('phase finale puis qualifications par confederation puis barrages intercontinentaux', () => {
    expect(families[0].editions[1].phases.map(p => p.label))
      .toEqual(['Phase finale', 'Qualifications Europe', 'Qualifications Afrique', 'Barrages intercontinentaux'])
    expect(families[1].editions[0].phases.map(p => p.label)).toEqual(['Phase finale', 'Qualifications'])
    expect(families[3].editions[0].phases.map(p => p.label)).toEqual(['Qualifications'])
    expect(families[4].editions[0].phases.map(p => p.label)).toEqual(['Phase finale', 'Qualifications'])
  })

  it('signale les editions et competitions de la saison en cours', () => {
    expect(families.map(f => f.current)).toEqual([false, false, false, true, false, true])
    expect(families[5].editions.map(e => e.current)).toEqual([true, false])
  })
})

describe('locateCompetition', () => {
  it('retrouve famille, edition et phase d une competition', () => {
    const found = locateCompetition(internationalFamilies(COMPETITIONS, 2027), 4)
    expect([found.family.key, found.edition.key, found.phase.label]).toEqual(['WC', '2022', 'Qualifications Afrique'])
  })

  it('null pour une competition absente', () => {
    expect(locateCompetition(internationalFamilies(COMPETITIONS, 2027), 99)).toBeNull()
  })
})
