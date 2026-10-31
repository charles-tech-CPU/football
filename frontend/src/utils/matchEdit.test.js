import { describe, expect, it } from 'vitest'
import { applyStatusChoice, deleteConfirmMessage, editFromMatch, isDecided, isPlayed, matchPayload, penaltiesNeeded, rowClassFor, statusChoice, statusRowClass, teamChangeLines, teamOptionsFor } from './matchEdit.js'

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
      penaltyScore1: null,
      penaltyScore2: null,
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
      penaltyScore1: null,
      penaltyScore2: null,
      status: null
    })
  })

  it('conserve les tirs au but deja saisis sur un ecran sans info de confrontation', () => {
    const played = { ...MATCH, score1: 1, score2: 1, penaltyScore1: 4, penaltyScore2: 3 }

    expect(matchPayload(played, editFromMatch(played))).toMatchObject({ penaltyScore1: 4, penaltyScore2: 3 })
  })

  it("efface les tirs au but d'un match de coupe qui n'est plus a egalite", () => {
    const cup = { ...MATCH, knockout: { decider: true, otherLegsGoals1: 0, otherLegsGoals2: 0 } }
    const edit = { ...editFromMatch(cup), score1: 2, score2: 1, penaltyScore1: 4, penaltyScore2: 3 }

    expect(matchPayload(cup, edit)).toMatchObject({ penaltyScore1: null, penaltyScore2: null })
  })

  it('les champs vides partent a null', () => {
    const payload = matchPayload(MATCH, { ...editFromMatch(MATCH), date: '', time: '', status: 'FORFEIT' })

    expect(payload).toMatchObject({ date: null, time: null, status: 'FORFEIT' })
  })
})

describe('penaltiesNeeded', () => {
  const single = { ...MATCH, knockout: { decider: true, otherLegsGoals1: 0, otherLegsGoals2: 0 } }

  it('match sec a egalite', () => {
    expect(penaltiesNeeded(single, { score1: 1, score2: 1 })).toBe(true)
    expect(penaltiesNeeded(single, { score1: 2, score2: 1 })).toBe(false)
  })

  it('pas tant que le score est incomplet', () => {
    expect(penaltiesNeeded(single, { score1: 1, score2: '' })).toBe(false)
  })

  it('match retour : egalite sur l ensemble des 2 manches', () => {
    // Aller : team1 de ce match a perdu 0-2 ; retour 2-0 -> 2-2 au cumul.
    const retour = { ...MATCH, knockout: { decider: true, otherLegsGoals1: 0, otherLegsGoals2: 2 } }
    expect(penaltiesNeeded(retour, { score1: 2, score2: 0 })).toBe(true)
    expect(penaltiesNeeded(retour, { score1: 1, score2: 1 })).toBe(false)
  })

  it('jamais sur un forfait : l equipe qui n abandonne pas est qualifiee d office', () => {
    const retour = { ...MATCH, knockout: { decider: true, otherLegsGoals1: 3, otherLegsGoals2: 0 } }
    expect(penaltiesNeeded(retour, { score1: 0, score2: 3, status: 'FORFEIT' })).toBe(false)
  })

  it('jamais sur un match aller ni en championnat', () => {
    const aller = { ...MATCH, knockout: { decider: false, otherLegsGoals1: 0, otherLegsGoals2: 0 } }
    expect(penaltiesNeeded(aller, { score1: 1, score2: 1 })).toBe(false)
    expect(penaltiesNeeded(MATCH, { score1: 1, score2: 1 })).toBe(false)
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

describe('isPlayed', () => {
  it('considere un match joue des que les 2 scores sont saisis', () => {
    expect(isPlayed({ score1: 0, score2: 0 })).toBe(true)
    expect(isPlayed({ score1: 2, score2: null })).toBe(false)
    expect(isPlayed({ score1: null, score2: null })).toBe(false)
  })
})

describe('isDecided', () => {
  it('match joue ou forfait avec son score', () => {
    expect(isDecided({ status: 'COMPLETED', score1: 1, score2: 0 })).toBe(true)
    expect(isDecided({ status: 'FORFEIT', score1: 0, score2: 3 })).toBe(true)
  })

  it('ni un forfait sans score, ni un match reporte', () => {
    expect(isDecided({ status: 'FORFEIT', score1: null, score2: null })).toBe(false)
    expect(isDecided({ status: 'POSTPONED', score1: 1, score2: 0 })).toBe(false)
  })
})

describe('statusChoice / applyStatusChoice', () => {
  it("le forfait d'une equipe donne 3-0 a son adversaire", () => {
    expect(applyStatusChoice({ status: '', score1: null, score2: null }, 'FORFEIT_1'))
      .toEqual({ status: 'FORFEIT', score1: 0, score2: 3 })
    expect(applyStatusChoice({ status: '', score1: null, score2: null }, 'FORFEIT_2'))
      .toEqual({ status: 'FORFEIT', score1: 3, score2: 0 })
  })

  it("l'equipe forfait se deduit du score sur tapis vert", () => {
    expect(statusChoice({ status: 'FORFEIT', score1: 0, score2: 3 })).toBe('FORFEIT_1')
    expect(statusChoice({ status: 'FORFEIT', score1: 3, score2: 0 })).toBe('FORFEIT_2')
    expect(statusChoice({ status: 'FORFEIT', score1: null, score2: null })).toBe('FORFEIT')
    expect(statusChoice({ status: 'POSTPONED', score1: null, score2: null })).toBe('POSTPONED')
  })

  it('quitter un forfait efface le score sur tapis vert, les autres statuts gardent le score', () => {
    expect(applyStatusChoice({ status: 'FORFEIT', score1: 0, score2: 3 }, ''))
      .toEqual({ status: '', score1: null, score2: null })
    expect(applyStatusChoice({ status: '', score1: 1, score2: 1 }, 'SUSPENDED'))
      .toEqual({ status: 'SUSPENDED', score1: 1, score2: 1 })
  })
})

describe('deleteConfirmMessage', () => {
  it('rappelle le score d un match joue', () => {
    const m = { team1Name: 'KASPIJ', team2Name: 'KAIRAT', score1: 1, score2: 2, roundLabel: 'J20' }
    expect(deleteConfirmMessage(m)).toBe('Supprimer définitivement le match KASPIJ – KAIRAT (1-2), J20 ?')
  })

  it('sans score pour un match a jouer', () => {
    const m = { team1Name: 'KASPIJ', team2Name: 'KAIRAT', score1: null, score2: null, roundLabel: 'J20' }
    expect(deleteConfirmMessage(m)).toBe('Supprimer définitivement le match KASPIJ – KAIRAT, J20 ?')
  })
})
