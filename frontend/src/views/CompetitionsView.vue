<template>
  <h1>Compétitions</h1>

  <div class="filters">
    <input v-model="search" placeholder="Filtrer par nom ou pays..." />
    <select v-model="typeFilter">
      <option value="">Tous les types</option>
      <option value="LEAGUE">Championnats</option>
      <option value="DOMESTIC_CUP">Coupes nationales</option>
      <option value="CONTINENTAL_CUP">Coupes d'Europe</option>
    </select>
  </div>

  <table v-if="filtered.length">
    <thead>
      <tr>
        <th>Code</th>
        <th>Nom</th>
        <th>Type</th>
        <th>Pays</th>
        <th></th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="c in filtered" :key="c.id">
        <td>{{ c.code }}</td>
        <td>{{ c.name }}</td>
        <td>{{ typeLabel(c.type) }}</td>
        <td>{{ c.country ?? '—' }}</td>
        <td><router-link :to="`/competitions/${c.id}`">Voir les matchs →</router-link></td>
      </tr>
    </tbody>
  </table>
  <p v-else-if="loaded">Aucune compétition ne correspond à ce filtre.</p>

  <h2>Ajouter une compétition</h2>
  <form class="inline" @submit.prevent="submit">
    <input v-model="form.code" placeholder="Code (ex: FRANCE, LDC)" required />
    <input v-model="form.name" placeholder="Nom (ex: France - Championnat 2027)" required />
    <select v-model="form.type">
      <option value="LEAGUE">Championnat</option>
      <option value="DOMESTIC_CUP">Coupe nationale</option>
      <option value="CONTINENTAL_CUP">Coupe d'Europe</option>
    </select>
    <input v-model="form.country" placeholder="Pays (vide si continentale)" />
    <input v-model.number="form.season" type="number" placeholder="Saison" required />
    <button type="submit">Ajouter</button>
  </form>
  <p v-if="error" style="color:#ff6b6b">{{ error }}</p>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import api from '../services/api'

const competitions = ref([])
const loaded = ref(false)
const error = ref('')
const search = ref('')
const typeFilter = ref('')

const form = reactive({
  code: '',
  name: '',
  type: 'LEAGUE',
  country: '',
  season: new Date().getFullYear() + 1
})

const filtered = computed(() => {
  const q = search.value.trim().toLowerCase()
  return competitions.value.filter(c => {
    if (typeFilter.value && c.type !== typeFilter.value) return false
    if (!q) return true
    return c.name.toLowerCase().includes(q) || (c.country ?? '').toLowerCase().includes(q)
  })
})

function typeLabel(type) {
  return { LEAGUE: 'Championnat', DOMESTIC_CUP: 'Coupe nationale', CONTINENTAL_CUP: "Coupe d'Europe" }[type] ?? type
}

async function load() {
  competitions.value = await api.getCompetitions()
  loaded.value = true
}

async function submit() {
  error.value = ''
  try {
    await api.createCompetition({ ...form, country: form.country || null })
    form.code = ''
    form.name = ''
    form.country = ''
    await load()
  } catch (e) {
    error.value = e.response?.data?.error ?? "Erreur lors de la création."
  }
}

onMounted(load)
</script>
