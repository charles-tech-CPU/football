import { describe, expect, it } from 'vitest'
import { compareByRoundThenDate, roundNumber, roundsByRecency } from './rounds.js'

describe('roundNumber', () => {
  it('extrait le premier nombre du libelle de journee', () => {
    expect(roundNumber({ roundLabel: 'J12' })).toBe(12)
    expect(roundNumber({ roundLabel: 'Journée 3 - retour' })).toBe(3)
  })

  it('null sans nombre ou sans libelle', () => {
    expect(roundNumber({ roundLabel: 'Finale' })).toBeNull()
    expect(roundNumber({ roundLabel: null })).toBeNull()
  })
})

describe('compareByRoundThenDate', () => {
  const sort = list => list.toSorted(compareByRoundThenDate).map(m => m.id)

  it('trie par numero de journee, meme si les dates disent le contraire', () => {
    expect(sort([
      { id: 1, roundLabel: 'J10', date: '2026-08-01' },
      { id: 2, roundLabel: 'J2', date: '2026-12-01' }
    ])).toEqual([2, 1])
  })

  it('a journee egale (ou sans numero), trie par date, les matchs sans date en premier', () => {
    expect(sort([
      { id: 1, roundLabel: 'J1', date: '2026-08-10' },
      { id: 2, roundLabel: 'J1', date: '2026-08-09' },
      { id: 3, roundLabel: 'Barrage', date: null }
    ])).toEqual([3, 2, 1])
  })

  it('en dernier recours, par id', () => {
    expect(sort([
      { id: 5, roundLabel: 'Finale', date: '2027-05-30' },
      { id: 4, roundLabel: 'Finale', date: '2027-05-30' }
    ])).toEqual([4, 5])
  })
})

describe('roundsByRecency', () => {
  it('du tour le plus recent au plus ancien, selon la premiere date du tour', () => {
    expect(roundsByRecency([
      { roundLabel: 'J1', date: '2026-08-10' },
      { roundLabel: 'J1', date: '2026-10-01' },
      { roundLabel: 'J3', date: '2026-08-24' },
      { roundLabel: 'J2', date: '2026-08-17' }
    ])).toEqual(['J3', 'J2', 'J1'])
  })

  it('tours sans date en dernier', () => {
    expect(roundsByRecency([
      { roundLabel: 'Barrage', date: null },
      { roundLabel: 'J1', date: '2026-08-10' }
    ])).toEqual(['J1', 'Barrage'])
  })
})
