import { describe, expect, it } from 'vitest'
import { cupFinal, moveRow, pendingCount, proposedRanking, teamsOf } from './closure.js'

const row = (teamId, points) => ({ teamId, points })

function match(team1Id, team2Id, score1, score2, extra = {}) {
  return {
    team1Id, team2Id, team1Name: `T${team1Id}`, team2Name: `T${team2Id}`,
    score1, score2, status: score1 == null ? 'SCHEDULED' : 'COMPLETED', date: '2026-10-01', roundLabel: 'J1', ...extra
  }
}

describe('proposedRanking', () => {
  it('reprend le classement general sans mini-championnat', () => {
    const standings = [row(1, 30), row(2, 20)]
    expect(proposedRanking(standings).map(r => r.teamId)).toEqual([1, 2])
  })

  it('met les groupes dans l ordre puis les equipes sans 2e phase', () => {
    const standings = [row(1, 30), row(2, 29), row(3, 28), row(4, 10)]
    const groups = [{ rows: [row(2, 40), row(1, 39)] }, { rows: [row(3, 30)] }]
    expect(proposedRanking(standings, groups).map(r => r.teamId)).toEqual([2, 1, 3, 4])
  })
})

describe('moveRow', () => {
  it('echange deux lignes voisines et reste dans les bornes', () => {
    expect(moveRow([1, 2, 3], 1, -1)).toEqual([2, 1, 3])
    expect(moveRow([1, 2, 3], 2, 1)).toEqual([1, 2, 3])
  })
})

describe('pendingCount', () => {
  it('compte les matchs a jouer, sans les places a determiner', () => {
    const matches = [
      match(1, 2, 1, 0),
      match(1, 3, null, null),
      { ...match(1, 4, null, null), team2Name: 'A DETERMINER (barrage)' }
    ]
    expect(pendingCount(matches)).toBe(1)
  })
})

describe('teamsOf', () => {
  it('liste les clubs une fois, par ordre alphabetique', () => {
    expect(teamsOf([match(2, 1, 1, 0), match(1, 3, 0, 0)])).toEqual([
      { id: 1, name: 'T1' }, { id: 2, name: 'T2' }, { id: 3, name: 'T3' }
    ])
  })
})

describe('cupFinal', () => {
  it('donne le vainqueur et le finaliste du dernier tour', () => {
    const matches = [
      match(1, 2, 2, 1, { roundLabel: 'Demi-finale', date: '2026-08-01' }),
      match(3, 4, 0, 1, { roundLabel: 'Demi-finale', date: '2026-08-02' }),
      match(1, 4, 1, 1, { roundLabel: 'Finale', date: '2026-09-20', penaltyScore1: 3, penaltyScore2: 4 })
    ]
    expect(cupFinal(matches)).toEqual({ winnerId: 4, runnerUpId: 1 })
  })

  it('ne propose rien tant que la finale n est pas jouee', () => {
    expect(cupFinal([match(1, 4, null, null, { roundLabel: 'Finale' })])).toEqual({ winnerId: null, runnerUpId: null })
    expect(cupFinal([])).toEqual({ winnerId: null, runnerUpId: null })
  })
})
