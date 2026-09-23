import { reactive, ref } from 'vue'
import api from '../services/api'
import { editFromMatch, matchPayload, rowClassFor, teamChangeLines } from '../utils/matchEdit.js'

// Etat d'edition en ligne d'une liste de matchs : valeurs editees par match, message d'erreur,
// enregistrement (avec confirmation si une equipe change) puis rechargement de la liste.
//   teams  : ref des clubs selectionnables (pour afficher le nom dans la confirmation)
//   reload : fonction qui recharge la liste apres un enregistrement reussi
export function useMatchEdits({ teams, reload }) {
  const edits = reactive({})
  const error = ref('')
  const today = new Date().toISOString().slice(0, 10)

  function resetEdits(matches) {
    for (const key of Object.keys(edits)) delete edits[key]
    for (const m of matches) edits[m.id] = editFromMatch(m)
  }

  function rowClass(m) {
    const edit = edits[m.id]
    return rowClassFor(edit?.status, edit?.date, today)
  }

  function teamNameById(id) {
    return teams?.value.find(t => t.id === id)?.name ?? '?'
  }

  function confirmTeamChange(match, edit) {
    const lines = teamChangeLines(match, edit, teamNameById)
    return lines.length === 0 || window.confirm(`Confirmer la modification du match ?\n${lines.join('\n')}`)
  }

  async function saveMatch(match) {
    error.value = ''
    const edit = edits[match.id]
    if (!confirmTeamChange(match, edit)) {
      edit.team1Id = match.team1Id
      edit.team2Id = match.team2Id
      return
    }
    try {
      await api.updateMatch(match.id, matchPayload(match, edit))
      await reload()
    } catch (e) {
      error.value = e.response?.data?.error ?? "Erreur lors de l'enregistrement du match."
    }
  }

  return { edits, error, resetEdits, rowClass, confirmTeamChange, saveMatch }
}
