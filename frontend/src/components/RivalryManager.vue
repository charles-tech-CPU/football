<template>
  <details class="add-form rivalries" @toggle="onToggle">
    <summary>⚔️ Derbys et rivalités ({{ rivalries.length || '…' }})</summary>
    <p class="section-intro">
      Un match entre deux clubs rivaux porte l'icône ⚔️ dans le calendrier. La liste de départ reprend les grands
      derbys et rivalités connus ; ajoute ou retire les tiens.
    </p>
    <form class="inline" @submit.prevent="add">
      <TeamSelect v-model="form.team1Id" :teams="teams" aria-label="Premier club" placeholder="Club" required />
      <TeamSelect v-model="form.team2Id" :teams="teams" aria-label="Second club" placeholder="Club rival" required />
      <input v-model.trim="form.label" maxlength="80" aria-label="Nom de la rivalité" placeholder="Nom (facultatif, ex: Le Classique)" />
      <button type="submit" :disabled="busy">Ajouter</button>
    </form>
    <input v-model="search" type="search" class="rivalry-search" aria-label="Rechercher une rivalité" placeholder="Rechercher un club, un pays..." />
    <p v-if="error" class="error-text">{{ error }}</p>
    <ul class="rivalry-list">
      <li v-for="r in filtered" :key="r.id">
        <FlagIcon v-if="r.country" :country="r.country" />
        <strong>{{ r.team1Name }}</strong> – <strong>{{ r.team2Name }}</strong>
        <span v-if="r.label" class="muted">{{ r.label }}</span>
        <button type="button" class="rivalry-delete" :title="`Retirer ${r.team1Name} – ${r.team2Name}`" @click="remove(r)">✕</button>
      </li>
    </ul>
  </details>
</template>

<script setup>
// Gestion des rivalites entre clubs (cf. RivalryService) : liste filtrable, ajout, suppression.
// Chargee a la premiere ouverture du panneau.
import { computed, reactive, ref } from 'vue'
import api from '../services/api'
import FlagIcon from './FlagIcon.vue'
import TeamSelect from './TeamSelect.vue'

defineProps({
  teams: { type: Array, default: () => [] }
})
const emit = defineEmits(['changed'])

const rivalries = ref([])
const search = ref('')
const error = ref('')
const busy = ref(false)
const form = reactive({ team1Id: '', team2Id: '', label: '' })
let loaded = false

function normalize(text) {
  return (text ?? '').normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase()
}

const filtered = computed(() => {
  const q = normalize(search.value.trim())
  if (!q) return rivalries.value
  return rivalries.value.filter(r =>
    [r.team1Name, r.team2Name, r.country, r.label].some(v => normalize(v).includes(q)))
})

async function load() {
  try {
    rivalries.value = await api.getRivalries()
    loaded = true
  } catch (e) {
    error.value = e.response?.data?.error ?? 'Erreur lors du chargement des rivalités.'
  }
}

function onToggle(event) {
  if (event.target.open && !loaded) void load()
}

async function run(action) {
  busy.value = true
  error.value = ''
  try {
    await action()
    await load()
    emit('changed')
    return true
  } catch (e) {
    error.value = e.response?.data?.error ?? "Erreur lors de l'enregistrement."
    return false
  } finally {
    busy.value = false
  }
}

async function add() {
  const ok = await run(() => api.createRivalry({ team1Id: form.team1Id, team2Id: form.team2Id, label: form.label || null }))
  if (ok) Object.assign(form, { team1Id: '', team2Id: '', label: '' })
}

async function remove(r) {
  if (!window.confirm(`Retirer la rivalité ${r.team1Name} – ${r.team2Name} ?`)) return
  await run(() => api.deleteRivalry(r.id))
}
</script>

<style scoped>
.rivalries {
  margin: 12px 0;
}
.rivalry-search {
  max-width: 280px;
  margin: 8px 0;
}
.rivalry-list {
  max-height: 320px;
  overflow-y: auto;
  padding: 0;
  list-style: none;
  columns: 2 360px;
}
.rivalry-list li {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 2px 0;
  break-inside: avoid;
}
.muted {
  color: var(--text-muted);
  font-size: 0.85em;
}
.rivalry-delete {
  margin-left: auto;
  border: none;
  background: transparent;
  color: var(--text-muted);
  cursor: pointer;
}
.rivalry-delete:hover {
  color: var(--danger);
}
</style>
