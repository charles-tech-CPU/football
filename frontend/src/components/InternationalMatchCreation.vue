<template>
  <button type="button" class="action-btn" @click="open">+ Ajouter un match</button>

  <AppModal v-model="showModal" title="Ajouter un match international">
    <form class="inline" @submit.prevent="submit">
      <select v-model="form.competitionId" aria-label="Compétition" required>
        <option value="" disabled>Compétition</option>
        <option v-for="c in competitions" :key="c.id" :value="c.id">{{ c.name }}</option>
      </select>
      <TeamSelect v-model="form.team1Id" :teams="teams" aria-label="Sélection à domicile" placeholder="Domicile" required />
      <TeamSelect v-model="form.team2Id" :teams="teams" aria-label="Sélection à l'extérieur" placeholder="Extérieur" required />
      <input v-model="form.roundLabel" aria-label="Tour" placeholder="Tour (ex: Play-in aller)" required />
      <input v-model="form.date" aria-label="Date" type="date" />
      <input v-model="form.time" aria-label="Heure" type="time" />
      <button type="submit">Ajouter</button>
    </form>
    <p v-if="error" class="error-text">{{ error }}</p>
  </AppModal>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import api from '../services/api'
import AppModal from './AppModal.vue'
import TeamSelect from './TeamSelect.vue'

// Bouton "+ Ajouter un match" du calendrier international (barrages, play-in, phases finales
// non importees). Competitions et selections proposees : celles de la saison en cours, deduites
// des matchs internationaux deja connus (toutes confederations, joues ou non).
const props = defineProps({
  matches: { type: Array, required: true }
})
const emit = defineEmits(['match-created'])

const showModal = ref(false)
const error = ref('')
const form = reactive({ competitionId: '', team1Id: '', team2Id: '', roundLabel: '', date: '', time: '' })

function uniqueSorted(entries) {
  return [...new Map(entries.map(e => [e.id, e])).values()].sort((a, b) => a.name.localeCompare(b.name, 'fr'))
}

const competitions = computed(() => uniqueSorted(props.matches.map(m => ({ id: m.competitionId, name: m.competitionName }))))

const teams = computed(() => uniqueSorted(props.matches.flatMap(m => [
  { id: m.team1Id, name: m.team1Name },
  { id: m.team2Id, name: m.team2Name }
])))

function open() {
  error.value = ''
  showModal.value = true
}

async function submit() {
  error.value = ''
  try {
    await api.createMatch({
      competitionId: form.competitionId,
      roundLabel: form.roundLabel,
      date: form.date || null,
      time: form.time || null,
      team1Id: form.team1Id,
      team2Id: form.team2Id,
      score1: null,
      score2: null
    })
    Object.assign(form, { team1Id: '', team2Id: '', date: '', time: '' })
    showModal.value = false
    emit('match-created')
  } catch (e) {
    error.value = e.response?.data?.error ?? 'Erreur lors de la création du match.'
  }
}
</script>
