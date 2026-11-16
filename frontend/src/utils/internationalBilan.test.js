import { describe, expect, it } from 'vitest'
import { journeyColumnsFor, mergedRecords, rankedRows } from './internationalBilan.js'

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

describe('mergedRecords', () => {
  const finals = [{ teamId: 1, teamName: 'Bresil', points: 30 }, { teamId: 2, teamName: 'France', points: 20 }]
  const qualifiers = [{ teamId: 2, teamName: 'France', points: 50 }, { teamId: 3, teamName: 'Malte', points: 5 }, { teamId: 1, teamName: 'Bresil', points: 4 }]

  it('regroupe phase finale et qualifications sur une ligne par selection', () => {
    const rows = mergedRecords(finals, qualifiers)
    expect(rows.map(r => r.teamName)).toEqual(['Bresil', 'France', 'Malte'])
    expect(rows[2].finals).toBeNull()
    expect(rows[1].qualifiers.points).toBe(50)
  })

  it('peut trier sur les qualifications', () => {
    expect(mergedRecords(finals, qualifiers, 'qualifiers').map(r => r.teamName)).toEqual(['France', 'Malte', 'Bresil'])
  })
})
