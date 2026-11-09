<template>
  <button
    v-if="canSwapHomeAway(match)"
    type="button"
    class="swap-btn"
    title="Inverser domicile / extérieur"
    aria-label="Inverser domicile / extérieur"
    @click="openDialog"
  >⇄</button>

  <AppModal v-model="dialogOpen" :title="`Inverser ${match.team1Name} – ${match.team2Name}`">
    <p>
      Ce match devient <strong>{{ match.team2Name }} – {{ match.team1Name }}</strong>
      <template v-if="match.score1 != null && match.score2 != null"> (score retourné : {{ match.score2 }}-{{ match.score1 }})</template>.
    </p>
    <p v-if="loading">Recherche des matchs retour…</p>
    <template v-else-if="candidates.length">
      <p>Inverser aussi le(s) match(s) retour ({{ match.team2Name }} – {{ match.team1Name }}) :</p>
      <label v-for="c in candidates" :key="c.id" class="swap-candidate">
        <input v-model="selected" type="checkbox" :value="c.id" />
        {{ c.roundLabel }} · {{ c.date ?? 'sans date' }} · {{ c.team1Name }} – {{ c.team2Name }}
        <template v-if="c.score1 != null && c.score2 != null"> ({{ c.score1 }}-{{ c.score2 }})</template>
      </label>
    </template>
    <p v-else>Aucun match retour trouvé dans ce championnat.</p>
    <p v-if="error" class="error-text">{{ error }}</p>
    <div class="swap-actions">
      <button type="button" :disabled="loading || saving" @click="confirm">Inverser</button>
      <button type="button" @click="dialogOpen = false">Annuler</button>
    </div>
  </AppModal>
</template>

<script setup>
// Icone d'inversion domicile / exterieur d'un match de championnat : propose aussi d'inverser
// le(s) match(s) retour (le plus proche en date coche par defaut). Les scores sont retournes.
import { ref } from 'vue'
import api from '../services/api'
import AppModal from './AppModal.vue'
import { canSwapHomeAway, reverseFixtures } from '../utils/swapHomeAway.js'

const props = defineProps({
  match: { type: Object, required: true }
})
const emit = defineEmits(['swapped'])

const dialogOpen = ref(false)
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const candidates = ref([])
const selected = ref([])

async function openDialog() {
  dialogOpen.value = true
  loading.value = true
  error.value = ''
  candidates.value = []
  selected.value = []
  try {
    const matches = await api.getMatchesByCompetition(props.match.competitionId)
    candidates.value = reverseFixtures(matches, props.match)
    if (candidates.value.length) selected.value = [candidates.value[0].id]
  } catch (e) {
    error.value = e.response?.data?.error ?? 'Impossible de charger les matchs du championnat.'
  } finally {
    loading.value = false
  }
}

async function confirm() {
  saving.value = true
  error.value = ''
  try {
    const swapped = await api.swapHomeAway(props.match.id, selected.value)
    dialogOpen.value = false
    emit('swapped', swapped)
  } catch (e) {
    error.value = e.response?.data?.error ?? "Erreur lors de l'inversion."
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.swap-btn {
  margin-left: 6px;
  padding: 6px 9px;
  background: var(--surface);
  border: 1px solid var(--border);
  color: var(--primary-dark);
}
.swap-btn:hover {
  border-color: var(--primary);
  background: var(--primary-soft);
}
.swap-candidate {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 6px 0;
  font-weight: 400;
  cursor: pointer;
}
.swap-actions {
  display: flex;
  gap: 8px;
  margin-top: 16px;
}
</style>
