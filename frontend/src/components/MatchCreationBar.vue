<template>
  <div class="action-bar">
    <button type="button" class="action-btn" @click="showMatchModal = true">+ Ajouter un match</button>
    <button type="button" class="action-btn action-btn--secondary" @click="showTeamModal = true">+ Ajouter un club</button>
  </div>

  <AppModal v-model="showMatchModal" title="Ajouter un match">
    <form class="inline" @submit.prevent="submitMatch">
      <select v-model.number="newMatch.team1Id" aria-label="Équipe domicile" required>
        <option disabled value="">Équipe 1</option>
        <option v-for="t in teams" :key="t.id" :value="t.id">{{ t.name }}</option>
      </select>
      <select v-model.number="newMatch.team2Id" aria-label="Équipe extérieur" required>
        <option disabled value="">Équipe 2</option>
        <option v-for="t in teams" :key="t.id" :value="t.id">{{ t.name }}</option>
      </select>
      <input v-model="newMatch.roundLabel" aria-label="Tour" :placeholder="roundPlaceholder" required />
      <input v-model="newMatch.date" aria-label="Date" type="date" />
      <input v-model="newMatch.time" aria-label="Heure" type="time" />
      <input v-model.number="newMatch.score1" aria-label="Buts équipe domicile" class="score-input" type="number" min="0" placeholder="B1" />
      <input v-model.number="newMatch.score2" aria-label="Buts équipe extérieur" class="score-input" type="number" min="0" placeholder="B2" />
      <button type="submit">Ajouter</button>
    </form>
    <p v-if="matchError" class="error-text">{{ matchError }}</p>
  </AppModal>

  <AppModal v-model="showTeamModal" title="Ajouter un club">
    <form class="inline" @submit.prevent="submitNewTeam">
      <input v-model="newTeam.name" aria-label="Nom du club" placeholder="Nom du club" required />
      <button type="submit">Ajouter</button>
    </form>
    <p v-if="teamError" class="error-text">{{ teamError }}</p>
  </AppModal>
</template>

<script setup>
import { reactive, ref } from 'vue'
import api from '../services/api'
import AppModal from './AppModal.vue'

// Boutons "+ Ajouter un match" / "+ Ajouter un club" d'une competition, avec leurs modales.
// Le parent recharge ses donnees sur les evenements match-created / team-created.
const props = defineProps({
  competitionId: { type: [String, Number], required: true },
  teams: { type: Array, required: true },
  // Pays attribue aux clubs crees (null pour une competition continentale)
  competitionCountry: { type: String, default: null },
  roundPlaceholder: { type: String, default: 'Round (ex: J1, 8e de finale)' },
  defaultRound: { type: String, default: '' }
})
const emit = defineEmits(['match-created', 'team-created'])

const showMatchModal = ref(false)
const showTeamModal = ref(false)
const matchError = ref('')
const teamError = ref('')
const newTeam = reactive({ name: '' })
const newMatch = reactive({
  team1Id: '',
  team2Id: '',
  roundLabel: props.defaultRound,
  date: '',
  time: '',
  score1: null,
  score2: null
})

async function submitMatch() {
  matchError.value = ''
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
    Object.assign(newMatch, { roundLabel: props.defaultRound, date: '', time: '', score1: null, score2: null })
    showMatchModal.value = false
    emit('match-created')
  } catch (e) {
    matchError.value = e.response?.data?.error ?? 'Erreur lors de la création du match.'
  }
}

async function submitNewTeam() {
  teamError.value = ''
  try {
    await api.createTeam({ name: newTeam.name.toUpperCase(), country: props.competitionCountry })
    newTeam.name = ''
    showTeamModal.value = false
    emit('team-created')
  } catch (e) {
    teamError.value = e.response?.data?.error ?? 'Erreur lors de la création du club.'
  }
}
</script>
