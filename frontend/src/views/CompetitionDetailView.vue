<template>
  <router-link class="back-link" to="/">← Toutes les compétitions</router-link>
  <div class="page-header">
    <h1>{{ competition?.name ?? '...' }}</h1>
  </div>
  <p class="section-intro" v-if="competition">
    <span class="badge badge-continental">Coupe d'Europe</span>
  </p>

  <h2>Matchs</h2>
  <CompetitionMatches v-if="competition" :competition-id="competition.id" />
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import api from '../services/api'
import CompetitionMatches from '../components/CompetitionMatches.vue'

const props = defineProps({
  id: { type: [String, Number], required: true }
})

const competition = ref(null)

async function load() {
  const competitionId = Number(props.id)
  const competitions = await api.getCompetitions()
  competition.value = competitions.find(c => c.id === competitionId) ?? null
}

watch(() => props.id, load)
onMounted(load)
</script>
