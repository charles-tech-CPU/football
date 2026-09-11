<template>
  <p class="section-intro">Matchs pas encore joués (à venir, reportés, suspendus ou forfait).</p>

  <table v-if="upcoming.length">
    <thead>
      <tr>
        <th>Round</th>
        <th>Date</th>
        <th>Heure</th>
        <th>Équipe 1</th>
        <th></th>
        <th></th>
        <th>Équipe 2</th>
        <th>Statut</th>
        <th></th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="m in upcoming" :key="m.id" :class="statusRowClass(edits[m.id].status)">
        <td>{{ m.roundLabel }}</td>
        <td><input class="date-input" type="date" v-model="edits[m.id].date" /></td>
        <td><input class="time-input" type="time" v-model="edits[m.id].time" /></td>
        <td><span class="team-cell"><TeamLogo :name="m.team1Name" /> {{ m.team1Name }}</span></td>
        <td>
          <input class="score-input" type="number" min="0" v-model.number="edits[m.id].score1" />
        </td>
        <td>
          <input class="score-input" type="number" min="0" v-model.number="edits[m.id].score2" />
        </td>
        <td><span class="team-cell"><TeamLogo :name="m.team2Name" /> {{ m.team2Name }}</span></td>
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
  <p v-else-if="loaded" class="empty-state">Tous les matchs de cette compétition ont été joués.</p>
  <p v-if="error" class="error-text">{{ error }}</p>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import api from '../services/api'
import TeamLogo from './TeamLogo.vue'

const props = defineProps({
  competitionId: { type: [String, Number], required: true }
})

const matches = ref([])
const loaded = ref(false)
const error = ref('')
const edits = reactive({})

const upcoming = computed(() => matches.value.filter(m => m.status !== 'COMPLETED'))

function statusRowClass(status) {
  if (status === 'POSTPONED') return 'row-postponed'
  if (status === 'SUSPENDED') return 'row-suspended'
  if (status === 'FORFEIT') return 'row-forfeit'
  return ''
}

async function load() {
  loaded.value = false
  const competitionId = Number(props.competitionId)
  matches.value = await api.getMatchesByCompetition(competitionId)

  for (const key of Object.keys(edits)) delete edits[key]
  for (const m of matches.value) {
    edits[m.id] = {
      date: m.date ?? '',
      time: m.time ? m.time.slice(0, 5) : '',
      score1: m.score1,
      score2: m.score2,
      status: ['POSTPONED', 'SUSPENDED', 'FORFEIT'].includes(m.status) ? m.status : ''
    }
  }
  loaded.value = true
}

async function saveMatch(match) {
  error.value = ''
  const edit = edits[match.id]
  try {
    await api.updateMatch(match.id, {
      competitionId: match.competitionId,
      roundLabel: match.roundLabel,
      date: edit.date || null,
      time: edit.time || null,
      team1Id: match.team1Id,
      team2Id: match.team2Id,
      score1: edit.score1,
      score2: edit.score2,
      status: edit.status || null
    })
    await load()
  } catch (e) {
    error.value = e.response?.data?.error ?? "Erreur lors de l'enregistrement du match."
  }
}

watch(() => props.competitionId, load)
onMounted(load)
</script>

<style scoped>
.date-input {
  width: 140px;
}

.time-input {
  width: 120px;
}

.row-postponed td {
  background: #e0ebff;
}

.row-suspended td {
  background: #fbe0de;
}

.row-forfeit td {
  background: #fdecd2;
}
</style>
