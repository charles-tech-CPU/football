import { describe, expect, it } from 'vitest'
import { canSwapHomeAway, reverseFixtures } from './swapHomeAway.js'

const m = (id, team1Id, team2Id, date, extra = {}) => ({ id, team1Id, team2Id, date, roundLabel: 'J1', competitionType: 'LEAGUE', ...extra })

describe('canSwapHomeAway', () => {
  it('accepte un match de championnat', () => {
    expect(canSwapHomeAway(m(1, 1, 2, null))).toBe(true)
  })
  it('refuse les barrages et les coupes', () => {
    expect(canSwapHomeAway(m(1, 1, 2, null, { roundLabel: 'Barrage - Aller' }))).toBe(false)
    expect(canSwapHomeAway(m(1, 1, 2, null, { competitionType: 'DOMESTIC_CUP' }))).toBe(false)
  })
})

describe('reverseFixtures', () => {
  it('garde les matchs a l envers, le plus proche en date d abord, sans date en dernier', () => {
    const match = m(1, 10, 20, '2026-08-15')
    const list = [
      match,
      m(2, 20, 10, '2027-03-01'),
      m(3, 20, 10, null),
      m(4, 20, 10, '2026-11-20'),
      m(5, 10, 20, '2026-12-01'),
      m(6, 20, 30, '2026-08-20')
    ]
    expect(reverseFixtures(list, match).map(x => x.id)).toEqual([4, 2, 3])
  })
})
