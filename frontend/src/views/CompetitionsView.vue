<template>
  <h1>Compétitions</h1>
  <p class="section-intro">54 championnats européens, leurs coupes nationales, et les coupes d'Europe.</p>

  <div class="filters">
    <input v-model="search" placeholder="Rechercher un pays ou une compétition..." />
  </div>

  <template v-if="filteredContinental.length">
    <h2>Coupes d'Europe</h2>
    <div class="continental-grid">
      <router-link
        v-for="c in filteredContinental"
        :key="c.id"
        class="continental-card"
        :to="`/competitions/${c.id}`"
      >
        <span class="continental-mark">★</span>
        <div class="country-card-info">
          <span class="country-card-name">{{ c.name }}</span>
          <span class="country-card-badges">
            <span class="badge badge-continental">{{ c.code }}</span>
          </span>
        </div>
      </router-link>
    </div>
  </template>

  <template v-if="filteredCountries.length">
    <h2 v-if="filteredContinental.length">Championnats</h2>
    <div class="country-grid">
      <router-link
        v-for="c in filteredCountries"
        :key="c.country"
        class="country-card"
        :to="`/pays/${encodeURIComponent(c.country)}`"
      >
        <FlagIcon :country="c.country" />
        <div class="country-card-info">
          <span class="country-card-name">{{ c.country }}</span>
          <span class="country-card-badges">
            <span v-if="c.league" class="badge badge-league">Championnat</span>
            <span v-if="c.cup" class="badge badge-cup">Coupe</span>
          </span>
        </div>
      </router-link>
    </div>
  </template>
  <p v-else-if="loaded && !filteredContinental.length" class="empty-state">Aucune compétition ne correspond à cette recherche.</p>

  <details class="add-form">
    <summary>Ajouter une compétition</summary>
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
    <p v-if="error" class="error-text">{{ error }}</p>
  </details>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import api from '../services/api'
import FlagIcon from '../components/FlagIcon.vue'

const competitions = ref([])
const loaded = ref(false)
const error = ref('')
const search = ref('')

const form = reactive({
  code: '',
  name: '',
  type: 'LEAGUE',
  country: '',
  season: new Date().getFullYear() + 1
})

const countries = computed(() => {
  const byCountry = new Map()
  for (const c of competitions.value) {
    if (!c.country) continue
    if (!byCountry.has(c.country)) byCountry.set(c.country, { country: c.country, league: null, cup: null })
    const entry = byCountry.get(c.country)
    if (c.type === 'LEAGUE') entry.league = c
    if (c.type === 'DOMESTIC_CUP') entry.cup = c
  }
  return [...byCountry.values()].sort((a, b) => a.country.localeCompare(b.country))
})

const continental = computed(() => competitions.value.filter(c => c.type === 'CONTINENTAL_CUP'))

const filteredCountries = computed(() => {
  const q = search.value.trim().toLowerCase()
  if (!q) return countries.value
  return countries.value.filter(c => c.country.toLowerCase().includes(q))
})

const filteredContinental = computed(() => {
  const q = search.value.trim().toLowerCase()
  if (!q) return continental.value
  return continental.value.filter(c => c.name.toLowerCase().includes(q) || c.code.toLowerCase().includes(q))
})

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
