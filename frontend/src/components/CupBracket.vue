<template>
  <div class="bracket-scroll" v-if="rounds.length">
    <div class="bracket">
      <div class="bracket-round" v-for="r in rounds" :key="r.round">
        <h3>{{ r.label }}</h3>
        <div class="tie-box" v-for="tie in r.ties" :key="tieKey(tie)">
          <div class="tie-leg" v-for="leg in tie.legs" :key="leg.id">
            <span class="tie-team" :class="{ 'tie-winner': tie.winnerId === leg.team1Id }">
              <TeamLogo :name="leg.team1Name" :logo-path="leg.team1LogoPath" />
              <FlagIcon v-if="showFlags" :country="leg.team1Country" />
              {{ leg.team1Name }}
            </span>
            <template v-if="editingId === leg.id">
              <span class="grid-edit">
                <input class="score-input" type="number" min="0" v-model.number="editScore1" @click.stop />
                <input class="score-input" type="number" min="0" v-model.number="editScore2" @click.stop />
                <button type="button" @click.stop="confirmEdit(leg)">✓</button>
              </span>
            </template>
            <span v-else class="tie-score" @click="startEdit(leg)">
              {{ leg.score1 ?? '-' }} — {{ leg.score2 ?? '-' }}
            </span>
            <span class="tie-team tie-team--right" :class="{ 'tie-winner': tie.winnerId === leg.team2Id }">
              {{ leg.team2Name }}
              <FlagIcon v-if="showFlags" :country="leg.team2Country" />
              <TeamLogo :name="leg.team2Name" :logo-path="leg.team2LogoPath" />
            </span>
          </div>
          <p class="tie-aggregate" v-if="tie.legs.length > 1">
            Agrégat : {{ tie.teamAName }} {{ tie.aggA }} - {{ tie.aggB }} {{ tie.teamBName }}
          </p>
        </div>
      </div>
    </div>
  </div>
  <p v-else-if="loaded" class="empty-state">Aucun match dans cette coupe pour l'instant.</p>

  <details class="add-form">
    <summary>Ajouter un match</summary>
    <form class="inline" @submit.prevent="submitMatch">
      <select v-model.number="newMatch.team1Id" required>
        <option disabled value="">Équipe 1</option>
        <option v-for="t in teams" :key="t.id" :value="t.id">{{ t.name }}</option>
      </select>
      <select v-model.number="newMatch.team2Id" required>
        <option disabled value="">Équipe 2</option>
        <option v-for="t in teams" :key="t.id" :value="t.id">{{ t.name }}</option>
      </select>
      <input v-model="newMatch.roundLabel" placeholder="Round (ex: Coupe nationale)" required />
      <input v-model="newMatch.date" type="date" />
      <input v-model="newMatch.time" type="time" />
      <input class="score-input" type="number" min="0" v-model.number="newMatch.score1" placeholder="B1" />
      <input class="score-input" type="number" min="0" v-model.number="newMatch.score2" placeholder="B2" />
      <button type="submit">Ajouter</button>
    </form>
  </details>
  <p v-if="error" class="error-text">{{ error }}</p>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import api from '../services/api'
import TeamLogo from './TeamLogo.vue'
import FlagIcon from './FlagIcon.vue'

const props = defineProps({
  competitionId: { type: [String, Number], required: true },
  // Fragments (insensibles a la casse) : un match n'est garde que si son round_label
  // contient au moins un de ces fragments. Pas de filtre => tous les matchs.
  roundIncludes: { type: Array, default: null },
  // Quand fourni, remplace l'inference automatique des tours (par date/vainqueur) par
  // un regroupement explicite base sur le round_label reel des matchs : [{ frag, label }, ...],
  // dans l'ordre d'affichage voulu. Chaque tie est classe dans le premier groupe dont le
  // fragment (insensible a la casse) apparait dans le round_label de son premier match.
  explicitRounds: { type: Array, default: null },
  showFlags: { type: Boolean, default: false }
})

const matches = ref([])
const teams = ref([])
const loaded = ref(false)
const error = ref('')
const editingId = ref(null)
const editScore1 = ref(null)
const editScore2 = ref(null)

const newMatch = reactive({
  team1Id: '', team2Id: '', roundLabel: 'Coupe nationale', date: '', time: '', score1: null, score2: null
})

function roundLabelFor(numTies) {
  if (numTies === 1) return 'Finale'
  if (numTies === 2) return 'Demi-finales'
  if (numTies === 4) return 'Quarts de finale'
  if (numTies === 8) return '8es de finale'
  if (numTies === 16) return '16es de finale'
  if (numTies === 32) return '32es de finale'
  return `Tour à ${numTies * 2} équipes`
}

function tieKey(tie) {
  return `${tie.teamAId}-${tie.teamBId}`
}

const filteredMatches = computed(() => {
  if (!props.roundIncludes) return matches.value
  const fragments = props.roundIncludes.map(f => f.toUpperCase())
  return matches.value.filter(m => fragments.some(f => (m.roundLabel ?? '').toUpperCase().includes(f)))
})

const rounds = computed(() => {
  if (!filteredMatches.value.length) return []

  const tieMap = new Map()
  for (const m of filteredMatches.value) {
    const key = [m.team1Id, m.team2Id].sort((a, b) => a - b).join('-')
    if (!tieMap.has(key)) tieMap.set(key, [])
    tieMap.get(key).push(m)
  }

  const ties = [...tieMap.values()].map(legs => {
    const sorted = legs.slice().sort((a, b) => (a.date ?? '').localeCompare(b.date ?? ''))
    const first = sorted[0]
    const teamAId = first.team1Id
    const teamAName = first.team1Name
    const teamBId = first.team2Id
    const teamBName = first.team2Name
    let aggA = 0, aggB = 0, hasAllScores = true
    for (const leg of sorted) {
      if (leg.score1 == null || leg.score2 == null) { hasAllScores = false; continue }
      if (leg.team1Id === teamAId) { aggA += leg.score1; aggB += leg.score2 }
      else { aggA += leg.score2; aggB += leg.score1 }
    }
    const firstDate = sorted[0].date ?? ''
    const lastDate = sorted[sorted.length - 1].date ?? ''
    return { teamAId, teamAName, teamBId, teamBName, legs: sorted, aggA, aggB, hasAllScores, firstDate, lastDate, winnerId: null, round: null }
  })

  for (const tie of ties) {
    if (tie.hasAllScores && tie.aggA !== tie.aggB) {
      tie.winnerId = tie.aggA > tie.aggB ? tie.teamAId : tie.teamBId
    }
  }

  if (props.explicitRounds) {
    const byGroup = new Map()
    for (const tie of ties) {
      const label = tie.legs[0].roundLabel ?? ''
      const upper = label.toUpperCase()
      const group = props.explicitRounds.find(g => upper.includes(g.frag.toUpperCase()))
      const key = group ? group.label : 'Autre'
      if (!byGroup.has(key)) byGroup.set(key, [])
      byGroup.get(key).push(tie)
    }
    const orderedLabels = [...props.explicitRounds.map(g => g.label), 'Autre'].filter(l => byGroup.has(l))
    return orderedLabels.map(label => ({
      round: label,
      label,
      ties: byGroup.get(label).sort((a, b) => (a.legs[0].date ?? '').localeCompare(b.legs[0].date ?? ''))
    }))
  }

  for (const tie of ties) {
    if (tie.winnerId) continue
    const laterA = ties.some(o => o !== tie && (o.teamAId === tie.teamAId || o.teamBId === tie.teamAId) && o.lastDate > tie.lastDate)
    const laterB = ties.some(o => o !== tie && (o.teamAId === tie.teamBId || o.teamBId === tie.teamBId) && o.lastDate > tie.lastDate)
    if (laterA && !laterB) tie.winnerId = tie.teamAId
    else if (laterB && !laterA) tie.winnerId = tie.teamBId
  }

  // Un meme club peut gagner plusieurs tours successifs : on cherche, pour
  // chaque tie et chaque equipe, le tie le plus RECENT (parmi ceux
  // chronologiquement anterieurs) dont elle est ressortie gagnante - pas une
  // simple map globale teamId -> tie (ecrasee a chaque victoire suivante).
  function predecessorOf(tie, teamId) {
    let best = null
    for (const other of ties) {
      if (other === tie || other.winnerId !== teamId) continue
      if (!other.lastDate || !tie.firstDate || other.lastDate < tie.firstDate) {
        if (!best || (other.lastDate ?? '') > (best.lastDate ?? '')) best = other
      }
    }
    return best
  }

  function roundOf(tie, visiting) {
    if (tie.round != null) return tie.round
    if (visiting.has(tie)) return 1
    visiting.add(tie)
    let maxPred = 0
    for (const teamId of [tie.teamAId, tie.teamBId]) {
      const predTie = predecessorOf(tie, teamId)
      if (predTie) {
        maxPred = Math.max(maxPred, roundOf(predTie, visiting))
      }
    }
    tie.round = maxPred + 1
    return tie.round
  }
  for (const tie of ties) roundOf(tie, new Set())

  const byRound = new Map()
  for (const tie of ties) {
    if (!byRound.has(tie.round)) byRound.set(tie.round, [])
    byRound.get(tie.round).push(tie)
  }
  const roundNumbers = [...byRound.keys()].sort((a, b) => a - b)
  return roundNumbers.map(r => {
    const roundTies = byRound.get(r).sort((a, b) => (a.legs[0].date ?? '').localeCompare(b.legs[0].date ?? ''))
    return { round: r, label: roundLabelFor(roundTies.length), ties: roundTies }
  })
})

async function startEdit(leg) {
  editingId.value = leg.id
  editScore1.value = leg.score1
  editScore2.value = leg.score2
}

async function confirmEdit(leg) {
  error.value = ''
  try {
    await api.updateMatch(leg.id, {
      competitionId: leg.competitionId,
      roundLabel: leg.roundLabel,
      date: leg.date,
      time: leg.time,
      team1Id: leg.team1Id,
      team2Id: leg.team2Id,
      score1: editScore1.value,
      score2: editScore2.value
    })
    editingId.value = null
    await load()
  } catch (e) {
    error.value = e.response?.data?.error ?? "Erreur lors de l'enregistrement du score."
  }
}

async function submitMatch() {
  error.value = ''
  try {
    await api.createMatch({
      competitionId: Number(props.competitionId),
      roundLabel: newMatch.roundLabel,
      date: newMatch.date || null,
      time: newMatch.time || null,
      team1Id: newMatch.team1Id,
      team2Id: newMatch.team2Id,
      score1: newMatch.score1,
      score2: newMatch.score2
    })
    newMatch.date = ''
    newMatch.time = ''
    newMatch.score1 = null
    newMatch.score2 = null
    await load()
  } catch (e) {
    error.value = e.response?.data?.error ?? "Erreur lors de la création du match."
  }
}

async function load() {
  loaded.value = false
  const competitionId = Number(props.competitionId)
  const [matchList, teamList] = await Promise.all([
    api.getMatchesByCompetition(competitionId),
    api.getTeams({ competitionId })
  ])
  matches.value = matchList
  teams.value = teamList
  loaded.value = true
}

watch(() => props.competitionId, load)
onMounted(load)
</script>

<style scoped>
.bracket-scroll {
  overflow-x: auto;
  margin-bottom: 24px;
}

.bracket {
  display: flex;
  gap: 24px;
  min-width: max-content;
  padding-bottom: 8px;
}

.bracket-round {
  display: flex;
  flex-direction: column;
  gap: 14px;
  min-width: 260px;
}

.bracket-round h3 {
  margin: 0 0 4px;
  font-size: 0.85em;
  text-transform: uppercase;
  letter-spacing: 0.03em;
  color: var(--text-muted);
}

.tie-box {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 10px 12px;
  box-shadow: var(--shadow);
}

.tie-leg {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 6px;
  font-size: 0.88em;
  padding: 3px 0;
}

.tie-team {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tie-team--right {
  justify-content: flex-end;
  text-align: right;
}

.tie-winner {
  font-weight: 700;
  color: var(--primary-dark);
}

.tie-score {
  cursor: pointer;
  font-variant-numeric: tabular-nums;
  color: var(--text-muted);
  padding: 1px 6px;
  border-radius: 6px;
}

.tie-score:hover {
  background: var(--surface-muted);
}

.tie-aggregate {
  margin: 6px 0 0;
  padding-top: 6px;
  border-top: 1px dashed var(--border);
  font-size: 0.78em;
  color: var(--text-muted);
}

.grid-edit {
  display: inline-flex;
  gap: 2px;
  align-items: center;
}

.grid-edit .score-input {
  width: 32px;
  padding: 2px;
}

.grid-edit button {
  padding: 2px 6px;
}
</style>
