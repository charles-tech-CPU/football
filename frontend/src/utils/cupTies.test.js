import { describe, expect, it } from 'vitest'
import { moveTie, sortByBracketPosition, summarizeTie, tiePosition } from './cupTies.js'

const PSG = { id: 1, name: 'PSG' }
const OM = { id: 2, name: 'OM' }

function leg(date, home, away, score1, score2, penalties = {}) {
  return {
    date,
    team1Id: home.id,
    team1Name: home.name,
    team2Id: away.id,
    team2Name: away.name,
    score1,
    score2,
    penaltyScore1: penalties.home ?? null,
    penaltyScore2: penalties.away ?? null
  }
}

describe('summarizeTie', () => {
  it("un match sec : le vainqueur est celui qui marque le plus", () => {
    const tie = summarizeTie([leg('2027-05-01', PSG, OM, 2, 1)])

    expect(tie).toMatchObject({
      teamAId: 1,
      teamAName: 'PSG',
      teamBId: 2,
      teamBName: 'OM',
      aggA: 2,
      aggB: 1,
      hasAllScores: true,
      winnerId: 1,
      needsPenalty: false,
      wentToPenalties: false,
      penA: null,
      penB: null
    })
  })

  it("aller-retour : trie les manches par date et cumule les buts du point de vue de l'equipe A", () => {
    const aller = leg('2027-03-03', PSG, OM, 1, 0)
    const retour = leg('2027-03-10', OM, PSG, 3, 1)

    const tie = summarizeTie([retour, aller])

    expect(tie.legs).toEqual([aller, retour])
    expect(tie.decider).toBe(retour)
    expect(tie).toMatchObject({ teamAId: 1, aggA: 2, aggB: 3, winnerId: 2 })
  })

  it('ne modifie pas le tableau de manches recu', () => {
    const legs = [leg('2027-03-10', OM, PSG, 0, 0), leg('2027-03-03', PSG, OM, 0, 0)]
    const copy = [...legs]

    summarizeTie(legs)

    expect(legs).toEqual(copy)
  })

  it("une manche pas encore jouee : pas de vainqueur ni de tirs au but", () => {
    const tie = summarizeTie([leg('2027-03-03', PSG, OM, 3, 0), leg('2027-03-10', OM, PSG, null, null)])

    expect(tie).toMatchObject({ aggA: 3, aggB: 0, hasAllScores: false, winnerId: null, needsPenalty: false })
  })

  it('egalite a l\'aggregat sans seance saisie : tirs au but attendus', () => {
    const tie = summarizeTie([leg('2027-03-03', PSG, OM, 1, 0), leg('2027-03-10', OM, PSG, 1, 0)])

    expect(tie).toMatchObject({ winnerId: null, needsPenalty: true, wentToPenalties: false })
  })

  it("tirs au but du match retour, lus du point de vue de l'equipe A", () => {
    const retour = leg('2027-03-10', OM, PSG, 1, 0, { home: 5, away: 4 })

    const tie = summarizeTie([leg('2027-03-03', PSG, OM, 1, 0), retour])

    expect(tie).toMatchObject({ penA: 4, penB: 5, wentToPenalties: true, needsPenalty: false, winnerId: 2 })
  })

  it("tirs au but gagnes par l'equipe A sur un match sec", () => {
    const tie = summarizeTie([leg('2027-05-30', PSG, OM, 0, 0, { home: 4, away: 3 })])

    expect(tie).toMatchObject({ penA: 4, penB: 3, winnerId: 1 })
  })

  it('seance incomplete : toujours en attente des tirs au but', () => {
    const tie = summarizeTie([leg('2027-05-30', PSG, OM, 0, 0, { home: 4 })])

    expect(tie).toMatchObject({ penA: null, penB: null, needsPenalty: true, winnerId: null })
  })

  it("tirs au but ignores quand l'aggregat n'est pas a egalite", () => {
    const tie = summarizeTie([leg('2027-05-30', PSG, OM, 2, 0, { home: 1, away: 3 })])

    expect(tie).toMatchObject({ penA: null, penB: null, wentToPenalties: false, winnerId: 1 })
  })

  it('manches sans date placees en tete du tri', () => {
    const datee = leg('2027-03-10', OM, PSG, 0, 0)
    const sansDate = leg(null, PSG, OM, 0, 0)

    expect(summarizeTie([datee, sansDate]).legs).toEqual([sansDate, datee])
  })
})

function tieAt(id, ...positions) {
  return { id, legs: positions.map((bracketPosition, i) => ({ id: id * 10 + i, bracketPosition })) }
}

describe('tiePosition', () => {
  it("null tant que le tour n'a pas ete reorganise", () => {
    expect(tiePosition(tieAt(1, null, null))).toBeNull()
  })

  it('la plus petite position de ses manches', () => {
    expect(tiePosition(tieAt(1, 4, null))).toBe(4)
  })
})

describe('sortByBracketPosition', () => {
  it("sans position, garde l'ordre par defaut", () => {
    const ties = [tieAt(1, null), tieAt(2, null)]
    expect(sortByBracketPosition(ties)).toBe(ties)
  })

  it('trie par position et met les confrontations ajoutees depuis a la fin', () => {
    const ties = [tieAt(1, null), tieAt(2, 2), tieAt(3, 1), tieAt(4, null)]
    expect(sortByBracketPosition(ties).map(t => t.id)).toEqual([3, 2, 1, 4])
  })
})

describe('moveTie', () => {
  it('ecrit la position de chaque manche du tour dans le nouvel ordre', () => {
    const ties = [tieAt(1, null, null), tieAt(2, null)]
    const updates = moveTie(ties, 1, -1)
    expect(updates.map(u => [u.leg.id, u.bracketPosition])).toEqual([[20, 1], [10, 2], [11, 2]])
  })

  it("ne reecrit pas une manche deja a la bonne position", () => {
    const ties = [tieAt(1, 1), tieAt(2, 2), tieAt(3, 3)]
    expect(moveTie(ties, 2, -1).map(u => [u.leg.id, u.bracketPosition])).toEqual([[30, 2], [20, 3]])
  })

  it('ne fait rien hors des bornes', () => {
    expect(moveTie([tieAt(1, null)], 0, -1)).toEqual([])
    expect(moveTie([tieAt(1, null)], 0, 1)).toEqual([])
  })
})
