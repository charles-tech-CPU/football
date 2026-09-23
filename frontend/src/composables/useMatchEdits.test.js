import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { ref } from 'vue'
import api from '../services/api'
import { useMatchEdits } from './useMatchEdits.js'

vi.mock('../services/api', () => ({ default: { updateMatch: vi.fn() } }))

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

describe('useMatchEdits', () => {
  const teams = ref([
    { id: 1, name: 'PSG' },
    { id: 2, name: 'OM' },
    { id: 3, name: 'Lens' }
  ])
  let reload
  let confirm
  let state

  beforeEach(() => {
    reload = vi.fn()
    confirm = vi.fn(() => true)
    vi.stubGlobal('window', { confirm })
    vi.mocked(api.updateMatch).mockReset().mockResolvedValue({})
    state = useMatchEdits({ teams, reload })
    state.resetEdits([MATCH])
  })

  afterEach(() => {
    vi.unstubAllGlobals()
  })

  it('prepare une edition par match et oublie les anciennes', () => {
    state.edits[99] = { date: '' }

    state.resetEdits([MATCH])

    expect(Object.keys(state.edits)).toEqual(['7'])
    expect(state.edits[7].time).toBe('21:00')
  })

  it('classe de ligne selon les valeurs en cours de saisie', () => {
    state.edits[7].status = 'POSTPONED'

    expect(state.rowClass(MATCH)).toBe('row-postponed')
    expect(state.rowClass({ id: 404 })).toBe('')
  })

  it("enregistre les valeurs editees puis recharge, sans confirmation si les equipes n'ont pas change", async () => {
    state.edits[7].score1 = 1
    state.edits[7].score2 = 1

    await state.saveMatch(MATCH)

    expect(confirm).not.toHaveBeenCalled()
    expect(api.updateMatch).toHaveBeenCalledWith(7, expect.objectContaining({ score1: 1, score2: 1, team1Id: 1 }))
    expect(reload).toHaveBeenCalled()
    expect(state.error.value).toBe('')
  })

  it("demande confirmation quand une equipe change, avec le nom des clubs", async () => {
    state.edits[7].team1Id = 3

    await state.saveMatch(MATCH)

    expect(confirm).toHaveBeenCalledWith('Confirmer la modification du match ?\nÉquipe 1 : PSG → Lens')
    expect(api.updateMatch).toHaveBeenCalledWith(7, expect.objectContaining({ team1Id: 3 }))
  })

  it("changement d'equipe refuse : on revient aux equipes d'origine sans rien enregistrer", async () => {
    confirm.mockReturnValue(false)
    state.edits[7].team1Id = 3
    state.edits[7].team2Id = 1

    await state.saveMatch(MATCH)

    expect(state.edits[7]).toMatchObject({ team1Id: 1, team2Id: 2 })
    expect(api.updateMatch).not.toHaveBeenCalled()
    expect(reload).not.toHaveBeenCalled()
  })

  it("sans liste de clubs, le nom d'une equipe inconnue s'affiche '?'", () => {
    const { confirmTeamChange } = useMatchEdits({ reload })

    confirmTeamChange(MATCH, { team1Id: 3, team2Id: 2 })

    expect(confirm).toHaveBeenCalledWith('Confirmer la modification du match ?\nÉquipe 1 : PSG → ?')
  })

  it("affiche le message d'erreur renvoye par l'API", async () => {
    vi.mocked(api.updateMatch).mockRejectedValue({ response: { data: { error: 'Match introuvable : 7' } } })

    await state.saveMatch(MATCH)

    expect(state.error.value).toBe('Match introuvable : 7')
    expect(reload).not.toHaveBeenCalled()
  })

  it("message generique si l'API ne precise rien", async () => {
    vi.mocked(api.updateMatch).mockRejectedValue(new Error('reseau'))

    await state.saveMatch(MATCH)

    expect(state.error.value).toBe("Erreur lors de l'enregistrement du match.")
  })
})
