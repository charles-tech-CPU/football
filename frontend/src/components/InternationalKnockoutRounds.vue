<template>
  <section class="knockout">
    <header class="knockout-head">
      <h3>Barrages et phases finales</h3>
      <button type="button" class="action-btn" @click="openCreation">+ Ajouter un match</button>
    </header>

    <form v-if="creating" class="knockout-form" @submit.prevent="create">
      <input v-model="form.round" list="knockout-rounds" aria-label="Tour" placeholder="Tour (ex: Barrages de promotion et relégation)" required />
      <datalist id="knockout-rounds">
        <option v-for="r in suggestions" :key="r" :value="r" />
      </datalist>
      <select v-model="form.leg" aria-label="Manche">
        <option value="Aller">Aller</option>
        <option value="Retour">Retour</option>
        <option value="">Match unique</option>
      </select>
      <TeamSelect v-model="form.team1Id" :teams="teamOptions" aria-label="Sélection à domicile" placeholder="Domicile" required />
      <TeamSelect v-model="form.team2Id" :teams="teamOptions" aria-label="Sélection à l'extérieur" placeholder="Extérieur" required />
      <input v-model="form.date" aria-label="Date" type="date" />
      <input v-model="form.time" aria-label="Heure" type="time" />
      <button type="submit" :disabled="busy">Ajouter</button>
      <button type="button" class="cell-cancel" @click="creating = false">Annuler</button>
    </form>

    <p v-if="!rounds.length" class="muted">
      Aucun barrage ni match de phase finale pour l'instant : ajoute-les avec « + Ajouter un match » dès que les
      affiches sont connues.
    </p>

    <article v-for="r in rounds" :key="r.name" class="group-card knockout-card">
      <header class="group-card-head">
        <h4>{{ r.name }}</h4>
        <span class="group-progress">{{ r.played }}/{{ r.total }} joués</span>
      </header>
      <div v-for="tie in r.ties" :key="tie.legs[0].id" class="knockout-tie">
        <div v-for="m in tie.legs" :key="m.id" class="knockout-row">
          <span class="knockout-date">{{ shortDate(m.date) }}<small v-if="tie.twoLegged"> {{ legOf(m) }}</small></span>
          <span class="knockout-team knockout-team--home" :class="{ winner: isWinner(tie, m.team1Id) }">
            <span v-if="leagueOf(m.team1Id)" class="league-tag">{{ leagueOf(m.team1Id) }}</span>
            {{ m.team1Name }}<FlagIcon :country="m.team1Country" />
          </span>
          <span v-if="editing?.id === m.id" class="knockout-edit">
            <input v-model.number="edit.score1" type="number" min="0" aria-label="Buts domicile" />
            <input v-model.number="edit.score2" type="number" min="0" aria-label="Buts extérieur" />
            <span class="muted small">tab</span>
            <input v-model.number="edit.penaltyScore1" type="number" min="0" aria-label="Tirs au but domicile" />
            <input v-model.number="edit.penaltyScore2" type="number" min="0" aria-label="Tirs au but extérieur" />
            <button type="button" :disabled="busy" @click="save(m)">✓</button>
            <button type="button" class="cell-cancel" @click="editing = null">✕</button>
          </span>
          <button v-else type="button" class="knockout-score" :title="`Saisir le score de ${m.team1Name} - ${m.team2Name}`" @click="startEdit(m)">
            <template v-if="isPlayed(m)">
              {{ m.score1 }}<span class="score-sep">-</span>{{ m.score2 }}
              <small v-if="m.penaltyScore1 != null && m.penaltyScore2 != null">tab {{ m.penaltyScore1 }}-{{ m.penaltyScore2 }}</small>
            </template>
            <span v-else class="muted">{{ m.time ? m.time.slice(0, 5) : 'à jouer' }}</span>
          </button>
          <span class="knockout-team" :class="{ winner: isWinner(tie, m.team2Id) }">
            <FlagIcon :country="m.team2Country" />{{ m.team2Name }}
            <span v-if="leagueOf(m.team2Id)" class="league-tag">{{ leagueOf(m.team2Id) }}</span>
          </span>
          <button type="button" class="knockout-delete" :title="`Supprimer ${m.team1Name} - ${m.team2Name}`" @click="remove(m)">🗑</button>
        </div>
        <p v-if="tie.twoLegged && tie.legs.every(isPlayed)" class="knockout-agg muted small">
          Cumul : {{ tie.teamAName }} {{ tie.aggA }}-{{ tie.aggB }} {{ tie.teamBName }}
          <template v-if="tie.winnerId != null"> · qualifié : <strong>{{ tie.winnerId === tie.teamAId ? tie.teamAName : tie.teamBName }}</strong></template>
        </p>
      </div>
    </article>
    <p v-if="error" class="error-text">{{ error }}</p>
  </section>
</template>

<script setup>
// Barrages de promotion / relegation, play-in, quarts et final four d'une competition de
// selections (Ligues des Nations UEFA et Concacaf...) : tous les matchs hors groupe, joues ou
// non, saisie du score (tirs au but compris), ajout et suppression d'un match. La ligue de chaque
// selection (A1, B3...) est rappelee pour lire les barrages entre ligues.
import { computed, reactive, ref } from 'vue'
import api from '../services/api'
import FlagIcon from './FlagIcon.vue'
import TeamSelect from './TeamSelect.vue'
import { isPlayed } from '../utils/matchEdit.js'
import { knockoutRounds, leagueTag, roundLabel, roundSuggestions } from '../utils/internationalRounds.js'

const props = defineProps({
  competition: { type: Object, required: true },
  // Matchs hors groupe de la competition
  matches: { type: Array, default: () => [] },
  // Lignes de classement (avec `group`) : selections engagees et leur ligue
  rows: { type: Array, default: () => [] }
})
const emit = defineEmits(['saved'])

const error = ref('')
const busy = ref(false)
const editing = ref(null)
const edit = reactive({ score1: null, score2: null, penaltyScore1: null, penaltyScore2: null })
const creating = ref(false)
const form = reactive({ round: '', leg: 'Aller', team1Id: '', team2Id: '', date: '', time: '' })

const rounds = computed(() => knockoutRounds(props.matches))
const suggestions = computed(() => roundSuggestions(props.competition.code))
const groupByTeam = computed(() => new Map(props.rows.map(r => [r.teamId, r.group])))

const teamOptions = computed(() => {
  const byId = new Map(props.rows.map(r => [r.teamId, { id: r.teamId, name: r.teamName }]))
  for (const m of props.matches) {
    byId.set(m.team1Id, { id: m.team1Id, name: m.team1Name })
    byId.set(m.team2Id, { id: m.team2Id, name: m.team2Name })
  }
  return [...byId.values()].sort((a, b) => a.name.localeCompare(b.name, 'fr'))
})

function leagueOf(teamId) {
  return leagueTag(groupByTeam.value.get(teamId))
}

function legOf(m) {
  return / - Retour$/.test(m.roundLabel ?? '') ? 'retour' : 'aller'
}

function isWinner(tie, teamId) {
  return tie.winnerId != null && tie.winnerId === teamId
}

// "2027-03-25" -> "25/03"
function shortDate(date) {
  if (!date) return '—'
  const [, month, day] = date.split('-')
  return `${day}/${month}`
}

function openCreation() {
  error.value = ''
  creating.value = !creating.value
}

function payload(m, overrides) {
  return {
    competitionId: m.competitionId,
    roundLabel: m.roundLabel,
    date: m.date,
    time: m.time,
    team1Id: m.team1Id,
    team2Id: m.team2Id,
    score1: m.score1,
    score2: m.score2,
    penaltyScore1: m.penaltyScore1,
    penaltyScore2: m.penaltyScore2,
    ...overrides
  }
}

function startEdit(m) {
  error.value = ''
  editing.value = m
  Object.assign(edit, {
    score1: m.score1,
    score2: m.score2,
    penaltyScore1: m.penaltyScore1,
    penaltyScore2: m.penaltyScore2
  })
}

const asScore = v => (typeof v === 'number' && Number.isInteger(v) && v >= 0 ? v : null)

async function run(action) {
  busy.value = true
  error.value = ''
  try {
    await action()
    emit('saved')
    return true
  } catch (e) {
    error.value = e.response?.data?.error ?? "Erreur lors de l'enregistrement."
    return false
  } finally {
    busy.value = false
  }
}

async function save(m) {
  const score1 = asScore(edit.score1)
  const score2 = asScore(edit.score2)
  const penaltyScore1 = asScore(edit.penaltyScore1)
  const penaltyScore2 = asScore(edit.penaltyScore2)
  // Un score vide remet le match "a jouer" ; les tirs au but vont par deux, a egalite au score.
  const withPenalties = score1 != null && score1 === score2 && penaltyScore1 != null && penaltyScore2 != null
  const ok = await run(() => api.updateMatch(m.id, payload(m, {
    score1: score1 != null && score2 != null ? score1 : null,
    score2: score1 != null && score2 != null ? score2 : null,
    penaltyScore1: withPenalties ? penaltyScore1 : null,
    penaltyScore2: withPenalties ? penaltyScore2 : null
  })))
  if (ok) editing.value = null
}

async function remove(m) {
  if (!window.confirm(`Supprimer le match ${m.team1Name} - ${m.team2Name} (${m.roundLabel}) ?`)) return
  await run(() => api.deleteMatch(m.id))
}

async function create() {
  const ok = await run(() => api.createMatch({
    competitionId: props.competition.id,
    roundLabel: roundLabel(form.round, form.leg),
    date: form.date || null,
    time: form.time || null,
    team1Id: form.team1Id,
    team2Id: form.team2Id,
    score1: null,
    score2: null
  }))
  if (ok) Object.assign(form, { team1Id: '', team2Id: '', date: '', time: '' })
}
</script>

<style scoped>
.knockout {
  margin-top: 18px;
}
.knockout-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.knockout-form {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin: 8px 0 12px;
}
.knockout-card {
  margin-bottom: 12px;
}
.knockout-tie + .knockout-tie {
  border-top: 1px solid var(--border);
  margin-top: 6px;
  padding-top: 6px;
}
.knockout-row {
  display: grid;
  grid-template-columns: 90px 1fr auto 1fr 28px;
  align-items: center;
  gap: 8px;
  padding: 2px 0;
}
.knockout-date {
  color: var(--text-muted);
  font-size: 13px;
}
.knockout-team {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.knockout-team--home {
  justify-content: flex-end;
}
.knockout-team.winner {
  font-weight: 700;
}
.knockout-score {
  min-width: 70px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--surface-muted);
  font-weight: 700;
  cursor: pointer;
}
.knockout-edit {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.knockout-edit input {
  width: 48px;
}
.knockout-delete {
  border: none;
  background: transparent;
  cursor: pointer;
  opacity: 0.5;
}
.knockout-delete:hover {
  opacity: 1;
}
.knockout-agg {
  margin: 2px 0 0 98px;
}
.league-tag {
  padding: 0 5px;
  border-radius: 6px;
  background: var(--primary-soft);
  color: var(--primary-dark);
  font-size: 11px;
  font-weight: 700;
}
.muted {
  color: var(--text-muted);
}
.small {
  font-size: 0.8em;
}
.score-sep {
  margin: 0 2px;
}
</style>
