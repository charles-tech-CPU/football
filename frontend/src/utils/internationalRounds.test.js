import { describe, expect, it } from 'vitest'
import { knockoutRounds, leagueTag, matchGroupResolver, roundLabel, roundName, roundSuggestions } from './internationalRounds.js'

const match = (id, team1Id, team2Id, roundLabel, date, score1 = null, score2 = null) => ({
  id, team1Id, team2Id, team1Name: `T${team1Id}`, team2Name: `T${team2Id}`, roundLabel, date, score1, score2, status: score1 == null ? 'SCHEDULED' : 'COMPLETED'
})

describe('matchGroupResolver', () => {
  it('classe hors groupe un match entre equipes de groupes differents', () => {
    const rows = [{ teamId: 1, group: 'Ligue A - Groupe 1' }, { teamId: 2, group: 'Ligue A - Groupe 1' }, { teamId: 3, group: 'Ligue B - Groupe 2' }]
    const groupOf = matchGroupResolver([], rows)
    expect(groupOf(match(1, 1, 2, 'J1'))).toBe('Ligue A - Groupe 1')
    expect(groupOf(match(2, 1, 3, 'Barrages de promotion et relégation - Aller'))).toBeUndefined()
  })
})

describe('knockoutRounds', () => {
  it('regroupe aller et retour en une confrontation avec le score cumule', () => {
    const rounds = knockoutRounds([
      match(2, 3, 1, 'Barrages de promotion et relégation - Retour', '2027-03-28', 1, 1),
      match(1, 1, 3, 'Barrages de promotion et relégation - Aller', '2027-03-25', 2, 0),
      match(3, 5, 6, 'Quarts de finale - Aller', '2027-03-20')
    ])
    expect(rounds.map(r => r.name)).toEqual(['Quarts de finale', 'Barrages de promotion et relégation'])
    const tie = rounds[1].ties[0]
    expect(tie.twoLegged).toBe(true)
    expect([tie.aggA, tie.aggB]).toEqual([3, 1])
    expect(tie.winnerId).toBe(1)
    expect(rounds[0]).toMatchObject({ played: 0, total: 1 })
  })
})

describe('libelles', () => {
  it('construit et decompose le libelle du tour', () => {
    expect(roundLabel(' Play-in ', 'Aller')).toBe('Play-in - Aller')
    expect(roundLabel('Finale', '')).toBe('Finale')
    expect(roundName('Quarts de finale - Retour')).toBe('Quarts de finale')
  })

  it('propose le play-in pour la Concacaf seulement', () => {
    expect(roundSuggestions('CONCACAF_NL_2627')[0]).toBe('Play-in')
    expect(roundSuggestions('UEFA_NL_2627')).not.toContain('Play-in')
  })

  it('abrege la ligue d\'une selection', () => {
    expect(leagueTag('Ligue A - Groupe 3')).toBe('A3')
    expect(leagueTag('Ligue C')).toBe('C')
    expect(leagueTag(null)).toBe('')
  })
})
