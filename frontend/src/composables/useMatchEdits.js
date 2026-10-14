import { computed, reactive, ref } from 'vue'
import api from '../services/api'
import { deleteConfirmMessage, editFromMatch, matchPayload, penaltiesNeeded, rowClassFor, teamChangeLines } from '../utils/matchEdit.js'

// Etat d'edition en ligne d'une liste de matchs : valeurs editees par match, message d'erreur,
// enregistrement (avec confirmation si une equipe change) puis rechargement de la liste.
// Saisie rapide : les lignes modifiees sont reperees (isDirty) et saveAll les enregistre toutes
// d'un coup, avec une seule confirmation et un seul rechargement.
//   teams  : ref des clubs selectionnables (pour afficher le nom dans la confirmation)
//   reload : fonction qui recharge la liste apres un enregistrement reussi
export function useMatchEdits({ teams, reload }) {
  const edits = reactive({})
  const error = ref('')
  const saving = ref(false)
  const today = new Date().toISOString().slice(0, 10)
  // Matchs affiches et valeurs de depart de leur edition, pour reperer les lignes modifiees.
  const matchesById = new Map()
  const initial = {}

  function resetEdits(matches) {
    for (const key of Object.keys(edits)) delete edits[key]
    matchesById.clear()
    for (const m of matches) {
      edits[m.id] = editFromMatch(m)
      initial[m.id] = JSON.stringify(edits[m.id])
      matchesById.set(m.id, m)
    }
  }

  function isDirty(m) {
    const edit = edits[m.id]
    return edit != null && JSON.stringify(edit) !== initial[m.id]
  }

  const dirtyMatches = computed(() => [...matchesById.values()].filter(isDirty))

  function rowClass(m) {
    const edit = edits[m.id]
    const cls = rowClassFor(edit?.status, edit?.date, today)
    return isDirty(m) ? `${cls} row-dirty`.trim() : cls
  }

  function teamNameById(id) {
    return teams?.value.find(t => t.id === id)?.name ?? '?'
  }

  function confirmTeamChange(match, edit) {
    const lines = teamChangeLines(match, edit, teamNameById)
    return lines.length === 0 || window.confirm(`Confirmer la modification du match ?\n${lines.join('\n')}`)
  }

  // Une confrontation de coupe a egalite doit etre departagee aux tirs au but (cf. penaltiesNeeded).
  function penaltyError(match, edit) {
    if (!penaltiesNeeded(match, edit)) return null
    const p1 = edit.penaltyScore1
    const p2 = edit.penaltyScore2
    if (typeof p1 === 'number' && typeof p2 === 'number' && p1 !== p2) return null
    return `${match.team1Name} – ${match.team2Name} : égalité, saisis les tirs au but (avec un vainqueur).`
  }

  async function saveMatch(match) {
    error.value = ''
    const edit = edits[match.id]
    const invalid = penaltyError(match, edit)
    if (invalid) {
      error.value = invalid
      return
    }
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

  // Suppression d'un match (ex: doublon saisi deux fois), apres confirmation.
  async function deleteMatch(match) {
    error.value = ''
    if (!window.confirm(deleteConfirmMessage(match))) return
    try {
      await api.deleteMatch(match.id)
      await reload()
    } catch (e) {
      error.value = e.response?.data?.error ?? 'Erreur lors de la suppression du match.'
    }
  }

  // Enregistre toutes les lignes modifiees. Rien n'est envoye si l'une d'elles est invalide ;
  // les changements d'equipe sont confirmes en une seule fois. En cas d'echec en cours de route,
  // la saisie est conservee (pas de rechargement) : un nouvel essai renvoie aussi les matchs
  // deja enregistres, sans effet puisque l'enregistrement remplace les valeurs.
  async function saveAll() {
    error.value = ''
    const toSave = dirtyMatches.value
    if (!toSave.length) return
    const invalid = toSave.map(m => penaltyError(m, edits[m.id])).filter(Boolean)
    if (invalid.length) {
      error.value = invalid.join(' ')
      return
    }
    const teamLines = toSave.flatMap(m => {
      const lines = teamChangeLines(m, edits[m.id], teamNameById)
      return lines.length ? [`${m.team1Name} – ${m.team2Name} :`, ...lines.map(l => `  ${l}`)] : []
    })
    if (teamLines.length && !window.confirm(`Confirmer les changements d'équipe ?\n${teamLines.join('\n')}`)) {
      return
    }
    saving.value = true
    let saved = 0
    try {
      for (const m of toSave) {
        await api.updateMatch(m.id, matchPayload(m, edits[m.id]))
        saved++
      }
    } catch (e) {
      const reason = e.response?.data?.error ?? "erreur lors de l'enregistrement"
      error.value = `${saved} match(s) enregistré(s) sur ${toSave.length}, puis : ${reason}`
      return
    } finally {
      saving.value = false
    }
    await reload()
  }

  return { edits, error, saving, resetEdits, isDirty, dirtyMatches, rowClass, confirmTeamChange, saveMatch, saveAll, deleteMatch }
}
