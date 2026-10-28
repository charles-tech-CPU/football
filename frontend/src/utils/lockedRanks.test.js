import { describe, expect, it } from 'vitest'
import { lockedTeamIds, remainingByTeam } from './lockedRanks.js'

const row = (teamId, points) => ({ teamId, points })

describe('remainingByTeam', () => {
  it('compte les journees non jouees de chaque equipe, plus les journees pas encore saisies', () => {
    const matches = [
      { roundLabel: 'J1', team1Id: 1, team2Id: 2, status: 'COMPLETED' },
      { roundLabel: 'J2', team1Id: 1, team2Id: 3, status: 'SCHEDULED' },
      { roundLabel: 'J2', team1Id: 2, team2Id: 4, status: 'FORFEIT' }
    ]
    const remaining = remainingByTeam(matches, [1, 2, 3, 4], 4)
    expect(Object.fromEntries(remaining)).toEqual({ 1: 3, 2: 2, 3: 3, 4: 2 })
  })

  it('ignore les barrages a determiner, mais un mini-championnat a determiner compte pour tous', () => {
    const matches = [
      { roundLabel: 'J1', team1Id: 1, team2Id: 2, status: 'COMPLETED' },
      { roundLabel: 'Barrage (a determiner : 3E-6E)', team1Id: 90, team2Id: 91, status: 'SCHEDULED' },
      { roundLabel: 'Mini-championnat (a determiner : 1ER-2E)', team1Id: 90, team2Id: 91, status: 'SCHEDULED' }
    ]
    expect(Object.fromEntries(remainingByTeam(matches, [1, 2], 1))).toEqual({ 1: 1, 2: 1 })
  })
})

describe('lockedTeamIds', () => {
  it('le dernier ne peut plus rattraper l avant-dernier : les deux places sont definitives, pas la lutte en tete', () => {
    const rows = [row(1, 50), row(2, 48), row(3, 20), row(4, 10)]
    const remaining = new Map([[1, 2], [2, 2], [3, 2], [4, 2]])
    expect([...lockedTeamIds(rows, remaining)]).toEqual([3, 4])
  })

  it('un ecart rattrapable a la derniere victoire pres laisse la place ouverte', () => {
    const rows = [row(1, 13), row(2, 10)]
    expect(lockedTeamIds(rows, new Map([[1, 1], [2, 1]])).size).toBe(0)
  })

  it('egalite de points : definitive seulement si plus personne ne joue', () => {
    const rows = [row(1, 10), row(2, 10)]
    expect(lockedTeamIds(rows, new Map([[1, 0], [2, 0]])).size).toBe(2)
    expect(lockedTeamIds([row(1, 13), row(2, 10)], new Map([[1, 0], [2, 1]])).size).toBe(0)
  })

  it('une equipe aux matchs restants inconnus n est jamais definitive, ni les equipes qu elle peut rattraper', () => {
    const rows = [row(1, 30), row(2, 10)]
    expect(lockedTeamIds(rows, new Map([[1, 0]])).size).toBe(0)
  })
})
