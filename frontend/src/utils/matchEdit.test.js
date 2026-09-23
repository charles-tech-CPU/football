import { describe, expect, it } from 'vitest'
import { editFromMatch, matchPayload, rowClassFor, statusRowClass, teamChangeLines, teamOptionsFor } from './matchEdit.js'

const MATCH = {
  id: 7,
  competitionId: 3,
  roundLabel: 'J5',
  date: '2026-10-04',
  time: '21:00:00',
  team1Id: 1,
  team1Name: 'PSG',
  team2Id: 2,
  team2Name: 'OM',
  score1: null,
  score2: null,
  status: 'SCHEDULED'
}

describe('statusRowClass', () => {
  it('une classe de ligne par statut force, rien sinon', () => {
    expect(statusRowClass('POSTPONED')).toBe('row-postponed')
    expect(statusRowClass('SUSPENDED')).toBe('row-suspended')
    expect(statusRowClass('FORFEIT')).toBe('row-forfeit')
    expect(statusRowClass('')).toBe('')
    expect(statusRowClass(undefined)).toBe('')
  })
})

describe('rowClassFor', () => {
  const today = '2026-09-23'

  it('le statut force passe avant la date', () => {
    expect(rowClassFor('POSTPONED', '2026-01-01', today)).toBe('row-postponed')
  })

  it('match en retard, du jour, a venir ou sans date', () => {
    expect(rowClassFor('', '2026-09-22', today)).toBe('row-overdue')
    expect(rowClassFor('', today, today)).toBe('row-today')
    expect(rowClassFor('', '2026-09-24', today)).toBe('')
    expect(rowClassFor('', '', today)).toBe('')
  })
})

describe('editFromMatch', () => {
  it('prepare les champs du formulaire (heure HH:mm, chaines vides plutot que null)', () => {
    expect(editFromMatch(MATCH)).toEqual({
      team1Id: 1,
      team2Id: 2,
      date: '2026-10-04',
      time: '21:00',
      score1: null,
      score2: null,
      status: ''
    })
  })

  it('garde les statuts forces et vide les champs absents', () => {
    const edit = editFromMatch({ ...MATCH, date: null, time: null, status: 'SUSPENDED' })

    expect(edit).toMatchObject({ date: '', time: '', status: 'SUSPENDED' })
  })

  it("un match joue n'a pas de statut force", () => {
    expect(editFromMatch({ ...MATCH, status: 'COMPLETED' }).status).toBe('')
  })
})

describe('matchPayload', () => {
  it('reprend competition et journee du match, le reste des valeurs editees', () => {
    const edit = { ...editFromMatch(MATCH), score1: 2, score2: 0, team2Id: 9 }

    expect(matchPayload(MATCH, edit)).toEqual({
      competitionId: 3,
      roundLabel: 'J5',
      date: '2026-10-04',
      time: '21:00',
      team1Id: 1,
      team2Id: 9,
      score1: 2,
      score2: 0,
      status: null
    })
  })

  it('les champs vides partent a null', () => {
    const payload = matchPayload(MATCH, { ...editFromMatch(MATCH), date: '', time: '', status: 'FORFEIT' })

    expect(payload).toMatchObject({ date: null, time: null, status: 'FORFEIT' })
  })
})

describe('teamChangeLines', () => {
  const nameById = id => ({ 1: 'PSG', 2: 'OM', 3: 'Lens' })[id]

  it("rien quand aucune equipe ne change", () => {
    expect(teamChangeLines(MATCH, editFromMatch(MATCH), nameById)).toEqual([])
  })

  it('une ligne par equipe modifiee', () => {
    const edit = { ...editFromMatch(MATCH), team1Id: 3, team2Id: 1 }

    expect(teamChangeLines(MATCH, edit, nameById)).toEqual(['Équipe 1 : PSG → Lens', 'Équipe 2 : OM → PSG'])
  })
})

describe('teamOptionsFor', () => {
  const teams = [
    { id: 1, name: 'PSG', country: 'France' },
    { id: 2, name: 'Inter', country: 'Italie' },
    { id: 3, name: 'Lens', country: 'FRANCE' },
    { id: 4, name: 'Sans pays', country: null }
  ]

  it('les clubs du pays de la competition, sans tenir compte de la casse, tries par nom', () => {
    expect(teamOptionsFor(teams, 'france', 1).map(t => t.name)).toEqual(['Lens', 'PSG'])
  })

  it("l'equipe actuelle reste proposee meme d'un autre pays", () => {
    expect(teamOptionsFor(teams, 'France', 2).map(t => t.name)).toEqual(['Inter', 'Lens', 'PSG'])
  })

  it('tous les clubs pour une competition sans pays', () => {
    expect(teamOptionsFor(teams, null, null)).toHaveLength(4)
  })

  it('une equipe actuelle inconnue est ignoree', () => {
    expect(teamOptionsFor(teams, 'Italie', 99).map(t => t.name)).toEqual(['Inter'])
  })

  it('ne modifie pas la liste recue', () => {
    const copy = [...teams]

    teamOptionsFor(teams, null, null)

    expect(teams).toEqual(copy)
  })
})
