import { describe, expect, it } from 'vitest'
import { isPlayoffMatch, playoffLabel, playoffRounds } from './playoffRounds.js'

const PENDING_1 = { id: 90, name: 'A DETERMINER (1)' }
const PENDING_2 = { id: 91, name: 'A DETERMINER (2)' }
const team = (id, name) => ({ id, name })
const [CORK, BRAY, UCD, KERRY, DROGHEDA] = [
  team(1, 'CORK'), team(2, 'BRAY'), team(3, 'UCD'), team(4, 'KERRY'), team(5, 'DROGHEDA')
]

let nextId = 1
function match(roundLabel, date, home, away, score1 = null, score2 = null) {
  return {
    id: nextId++,
    roundLabel,
    date,
    team1Id: home.id,
    team1Name: home.name,
    team2Id: away.id,
    team2Name: away.name,
    score1,
    score2,
    penaltyScore1: null,
    penaltyScore2: null
  }
}

const shape = rounds => rounds.map(r => [r.label, r.ties.map(t => `${t.teamAName}-${t.teamBName}`)])

describe('isPlayoffMatch / playoffLabel', () => {
  it('reconnait les barrages quelle que soit la casse', () => {
    expect(isPlayoffMatch({ roundLabel: 'Barrage (a determiner : 2E-5E)' })).toBe(true)
    expect(isPlayoffMatch({ roundLabel: 'BARRAGES - Aller' })).toBe(true)
    expect(isPlayoffMatch({ roundLabel: 'J12' })).toBe(false)
    expect(isPlayoffMatch({ roundLabel: null })).toBe(false)
  })

  it('retire places a determiner et manche', () => {
    expect(playoffLabel('Barrage (a determiner : 2E-5E)')).toBe('Barrage')
    expect(playoffLabel('Barrages de relégation - Retour')).toBe('Barrages de relégation')
  })
})

describe('playoffRounds', () => {
  it('barrage sec : un seul tour', () => {
    const rounds = playoffRounds([match('Barrage', '2027-05-20', CORK, BRAY, 2, 1)])

    expect(shape(rounds)).toEqual([['Barrage', ['CORK-BRAY']]])
    expect(rounds[0].ties[0].winnerId).toBe(CORK.id)
  })

  it('Irlande : demi-finales aller-retour, finale, puis barrage contre le club de D1', () => {
    const rounds = playoffRounds([
      match('Barrage (a determiner : 3E-4E)', '2026-10-24', UCD, KERRY, 1, 0),
      match('Barrage (a determiner : 2E-5E)', '2026-10-24', CORK, BRAY, 2, 2),
      match('Barrage (a determiner : 5E-2E)', '2026-10-28', BRAY, CORK, 0, 1),
      match('Barrage (a determiner : 4E-3E)', '2026-10-28', KERRY, UCD, 0, 0),
      match('Barrage', '2026-11-01', CORK, UCD, 2, 0),
      match('Barrage', '2026-11-07', DROGHEDA, CORK)
    ])

    expect(shape(rounds)).toEqual([
      ['1er tour', ['UCD-KERRY', 'CORK-BRAY']],
      ['2e tour', ['CORK-UCD']],
      ['3e tour', ['DROGHEDA-CORK']]
    ])
    expect(rounds[0].ties[1].legs).toHaveLength(2)
  })

  it('places encore a determiner : manches regroupees, tours suivants par date', () => {
    const rounds = playoffRounds([
      match('Barrage (a determiner : 3E-4E)', '2026-10-24', PENDING_1, PENDING_2),
      match('Barrage (a determiner : 2E-5E)', '2026-10-24', PENDING_1, PENDING_2),
      match('Barrage (a determiner : 5E-2E)', '2026-10-28', PENDING_1, PENDING_2),
      match('Barrage (a determiner : 4E-3E)', '2026-10-28', PENDING_1, PENDING_2),
      match('Barrage (a determiner : W1-W2)', '2026-11-01', PENDING_1, PENDING_2)
    ])

    expect(rounds.map(r => r.ties.map(t => t.legs.length))).toEqual([[2, 2], [1]])
  })

  it('libelle precis commun a un tour garde tel quel', () => {
    const rounds = playoffRounds([
      match('Barrage - Demi-finale', '2027-05-10', CORK, BRAY, 1, 0),
      match('Barrage - Demi-finale', '2027-05-10', UCD, KERRY, 1, 0),
      match('Barrage - Finale', '2027-05-17', CORK, UCD)
    ])

    expect(rounds.map(r => r.label)).toEqual(['Barrage - Demi-finale', 'Barrage - Finale'])
  })
})
