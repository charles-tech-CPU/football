<template>
  <h1>Reportés / Suspendus</h1>
  <p class="section-intro">Tous les matchs reportés ou suspendus, toutes compétitions confondues.</p>

  <div class="table-scroll" v-if="matches.length">
  <table>
    <thead>
      <tr>
        <th>Date</th>
        <th>Heure</th>
        <th>Compétition</th>
        <th>Journée</th>
        <th>Équipe 1</th>
        <th></th>
        <th></th>
        <th>Équipe 2</th>
        <th>Statut</th>
        <th></th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="m in matches" :key="m.id" :class="rowClass(m)">
        <td><input class="date-input" type="date" v-model="edits[m.id].date" /></td>
        <td><input class="time-input" type="time" v-model="edits[m.id].time" /></td>
        <td>
          <span class="team-cell" :class="competitionBadgeClass(m)">
            <FlagIcon v-if="m.competitionCountry" :country="m.competitionCountry" />
            {{ m.competitionName }}
          </span>
        </td>
        <td>{{ m.roundLabel }}</td>
        <td>
          <select v-model.number="edits[m.id].team1Id">
            <option v-for="t in teamOptionsFor(m, edits[m.id].team1Id)" :key="t.id" :value="t.id">{{ t.name }}</option>
          </select>
        </td>
        <td>
          <input class="score-input" type="number" min="0" v-model.number="edits[m.id].score1" />
        </td>
        <td>
          <input class="score-input" type="number" min="0" v-model.number="edits[m.id].score2" />
        </td>
        <td>
          <select v-model.number="edits[m.id].team2Id">
            <option v-for="t in teamOptionsFor(m, edits[m.id].team2Id)" :key="t.id" :value="t.id">{{ t.name }}</option>
          </select>
        </td>
        <td>
          <select v-model="edits[m.id].status">
            <option value="">À venir</option>
            <option value="POSTPONED">Reporté</option>
            <option value="SUSPENDED">Suspendu</option>
            <option value="FORFEIT">Forfait</option>
          </select>
        </td>
        <td>
          <button @click="saveMatch(m)">Enregistrer</button>
        </td>
      </tr>
    </tbody>
  </table>
  </div>
  <p v-else-if="loaded" class="empty-state">Aucun match reporté ou suspendu pour l'instant.</p>
  <p v-if="error" class="error-text">{{ error }}</p>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import api from '../services/api'
import FlagIcon from '../components/FlagIcon.vue'
import { formatTime } from '../utils/format'
import { competitionBadgeClass } from '../utils/competitionBadge'

const matches = ref([])
const teams = ref([])
const loaded = ref(false)
const error = ref('')
const edits = reactive({})

// Restreint la liste proposee aux clubs du pays de la competition du match (championnats/coupes
// nationales) ; pour les coupes d'Europe (pas de pays), la liste complete reste proposee. On
// garde toujours l'equipe actuellement selectionnee meme si son pays ne correspond pas exactement
// (libelles de pays en texte libre, cf README).
function teamOptionsFor(m, currentId) {
  const country = m.competitionCountry
  let list = country
    ? teams.value.filter(t => t.country && t.country.toLowerCase() === country.toLowerCase())
    : teams.value
  if (currentId != null && !list.some(t => t.id === currentId)) {
    const current = teams.value.find(t => t.id === currentId)
    if (current) list = [...list, current]
  }
  return list.slice().sort((a, b) => a.name.localeCompare(b.name))
}

function statusRowClass(status) {
  if (status === 'POSTPONED') return 'row-postponed'
  if (status === 'SUSPENDED') return 'row-suspended'
  if (status === 'FORFEIT') return 'row-forfeit'
  return ''
}

const today = new Date().toISOString().slice(0, 10)

function rowClass(m) {
  const status = edits[m.id].status
  const cls = statusRowClass(status)
  if (cls) return cls
  const date = edits[m.id].date
  if (!date) return ''
  if (date < today) return 'row-overdue'
  if (date === today) return 'row-today'
  return ''
}

async function load() {
  loaded.value = false
  const [matchList, teamList] = await Promise.all([
    api.getPostponedMatches(),
    api.getTeams()
  ])
  matches.value = matchList
  teams.value = teamList

  for (const key of Object.keys(edits)) delete edits[key]
  for (const m of matches.value) {
    edits[m.id] = {
      team1Id: m.team1Id,
      team2Id: m.team2Id,
      date: m.date ?? '',
      time: formatTime(m.time),
      score1: m.score1,
      score2: m.score2,
      status: ['POSTPONED', 'SUSPENDED', 'FORFEIT'].includes(m.status) ? m.status : ''
    }
  }
  loaded.value = true
}

function teamNameById(id) {
  return teams.value.find(t => t.id === id)?.name ?? '?'
}

function confirmTeamChangeIfNeeded(match, edit) {
  const changed1 = edit.team1Id !== match.team1Id
  const changed2 = edit.team2Id !== match.team2Id
  if (!changed1 && !changed2) return true
  const lines = []
  if (changed1) lines.push(`Équipe 1 : ${match.team1Name} → ${teamNameById(edit.team1Id)}`)
  if (changed2) lines.push(`Équipe 2 : ${match.team2Name} → ${teamNameById(edit.team2Id)}`)
  return window.confirm(`Confirmer la modification du match ?\n${lines.join('\n')}`)
}

async function saveMatch(match) {
  error.value = ''
  const edit = edits[match.id]
  if (!confirmTeamChangeIfNeeded(match, edit)) {
    edit.team1Id = match.team1Id
    edit.team2Id = match.team2Id
    return
  }
  try {
    await api.updateMatch(match.id, {
      competitionId: match.competitionId,
      roundLabel: match.roundLabel,
      date: edit.date || null,
      time: edit.time || null,
      team1Id: edit.team1Id,
      team2Id: edit.team2Id,
      score1: edit.score1,
      score2: edit.score2,
      status: edit.status || null
    })
    await load()
  } catch (e) {
    error.value = e.response?.data?.error ?? "Erreur lors de l'enregistrement du match."
  }
}

onMounted(load)
</script>

<style scoped>
.table-scroll {
  overflow-x: auto;
  width: 100vw;
  position: relative;
  left: 50%;
  right: 50%;
  margin-left: -50vw;
  margin-right: -50vw;
  padding: 0 20px;
}

.date-input {
  width: 140px;
}

.time-input {
  width: 120px;
}
</style>
