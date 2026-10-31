import { describe, expect, it } from 'vitest'
import { journeyColumnsFor, rankedRows } from './internationalBilan.js'

describe('journeyColumnsFor', () => {
  it('ne garde que les stades atteints par au moins une selection', () => {
    const journeys = [{ titles: 1, runnerUps: 0, semiFinals: 2, quarterFinals: 0, roundOf16: 0, roundOf32: 0, groupStage: 3 }]
    expect(journeyColumnsFor(journeys).map(c => c.key)).toEqual(['titles', 'semiFinals', 'groupStage'])
  })
})

describe('rankedRows', () => {
  it('numerote avant de filtrer, sans tenir compte des accents', () => {
    const rows = [{ teamName: 'Bresil' }, { teamName: 'Côte d\'Ivoire' }, { teamName: 'France' }]
    expect(rankedRows(rows, 'cote')).toEqual([{ teamName: 'Côte d\'Ivoire', rank: 2 }])
    expect(rankedRows(rows).map(r => r.rank)).toEqual([1, 2, 3])
  })
})
