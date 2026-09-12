<template>
  <h1>Équipes</h1>

  <div class="filters">
    <input v-model="search" placeholder="Filtrer par nom ou pays..." />
  </div>

  <table v-if="filtered.length">
    <thead>
      <tr>
        <th>Nom</th>
        <th>Pays</th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="t in filtered" :key="t.id">
        <td>
          <span class="team-cell">
            <TeamLogo :name="t.name" :country="t.country" :logo-path="t.logoPath" />
            <span class="team-chip" :style="chipStyle(t.country)">{{ t.name }}</span>
          </span>
        </td>
        <td>
          <span class="team-country" v-if="t.country">
            <FlagIcon :country="t.country" />
            {{ canonicalCountry(t.country) }}
          </span>
          <span v-else>—</span>
        </td>
      </tr>
    </tbody>
  </table>
  <p v-else-if="loaded" class="empty-state">Aucune équipe ne correspond à ce filtre.</p>
  <p v-if="loaded">{{ teams.length }} équipes au total.</p>

  <h2>Ajouter une équipe</h2>
  <form class="inline" @submit.prevent="submit">
    <input v-model="form.name" placeholder="Nom du club" required />
    <input v-model="form.country" placeholder="Pays" />
    <button type="submit">Ajouter</button>
  </form>
  <p v-if="error" class="error-text">{{ error }}</p>

  <details class="add-form">
    <summary>Fusionner deux équipes en doublon</summary>
    <p class="section-intro">
      Le fichier source utilise parfois un libellé légèrement différent pour le même club
      (ex : "VLLAZNIA" / "VLLAZINA"). Choisis l'équipe à supprimer et celle à conserver :
      tous ses matchs sont réaffectés avant suppression.
    </p>
    <form class="inline" @submit.prevent="submitMerge">
      <select v-model.number="mergeForm.sourceId" required>
        <option disabled value="">Équipe à supprimer (doublon)</option>
        <option v-for="t in teams" :key="t.id" :value="t.id">{{ t.name }} ({{ canonicalCountry(t.country) ?? '—' }})</option>
      </select>
      <select v-model.number="mergeForm.targetId" required>
        <option disabled value="">Équipe à conserver</option>
        <option v-for="t in teams" :key="t.id" :value="t.id">{{ t.name }} ({{ canonicalCountry(t.country) ?? '—' }})</option>
      </select>
      <button type="submit">Fusionner</button>
    </form>
    <p v-if="mergeError" class="error-text">{{ mergeError }}</p>
    <p v-if="mergeSuccess">{{ mergeSuccess }}</p>
  </details>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import api from '../services/api'
import FlagIcon from '../components/FlagIcon.vue'
import TeamLogo from '../components/TeamLogo.vue'
import { canonicalCountry } from '../utils/countryFlags'
import { teamCountryStyle } from '../utils/teamColors'

const teams = ref([])
const loaded = ref(false)
const error = ref('')
const search = ref('')
const form = reactive({ name: '', country: '' })

const mergeForm = reactive({ sourceId: '', targetId: '' })
const mergeError = ref('')
const mergeSuccess = ref('')

const filtered = computed(() => {
  const q = search.value.trim().toLowerCase()
  if (!q) return teams.value
  return teams.value.filter(t => t.name.toLowerCase().includes(q) || (t.country ?? '').toLowerCase().includes(q))
})

function chipStyle(country) {
  const { bg, fg } = teamCountryStyle(country)
  return { backgroundColor: bg, color: fg }
}

async function load() {
  teams.value = await api.getTeams()
  loaded.value = true
}

async function submit() {
  error.value = ''
  try {
    await api.createTeam({ ...form, country: form.country || null })
    form.name = ''
    form.country = ''
    await load()
  } catch (e) {
    error.value = e.response?.data?.error ?? "Erreur lors de la création."
  }
}

async function submitMerge() {
  mergeError.value = ''
  mergeSuccess.value = ''
  if (mergeForm.sourceId === mergeForm.targetId) {
    mergeError.value = 'Choisis deux équipes différentes.'
    return
  }
  const source = teams.value.find(t => t.id === mergeForm.sourceId)
  const target = teams.value.find(t => t.id === mergeForm.targetId)
  try {
    await api.mergeTeam(mergeForm.sourceId, mergeForm.targetId)
    mergeSuccess.value = `"${source?.name}" a été fusionnée dans "${target?.name}".`
    mergeForm.sourceId = ''
    mergeForm.targetId = ''
    await load()
  } catch (e) {
    mergeError.value = e.response?.data?.error ?? 'Erreur lors de la fusion.'
  }
}

onMounted(load)
</script>
