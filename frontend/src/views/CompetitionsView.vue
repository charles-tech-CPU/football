<template>
  <section class="hero">
    <span class="hero-eyebrow">Football européen</span>
    <h1>Compétitions</h1>
    <p class="section-intro">Championnats européens, leurs coupes nationales, et les coupes d'Europe.</p>

    <div v-if="loaded" class="hero-stats">
      <div class="hero-stat">
        <span class="hero-stat-value">{{ countries.length }}</span>
        <span class="hero-stat-label">Pays</span>
      </div>
      <div class="hero-stat">
        <span class="hero-stat-value">{{ leagueCount }}</span>
        <span class="hero-stat-label">Championnats</span>
      </div>
      <div class="hero-stat">
        <span class="hero-stat-value">{{ cupCount }}</span>
        <span class="hero-stat-label">Coupes nationales</span>
      </div>
      <div class="hero-stat">
        <span class="hero-stat-value">{{ continental.length }}</span>
        <span class="hero-stat-label">Coupes d'Europe</span>
      </div>
    </div>

    <div class="hero-search">
      <input v-model="search" aria-label="Rechercher un pays ou une compétition" placeholder="Rechercher un pays ou une compétition..." />
    </div>
  </section>

  <template v-if="filteredContinental.length">
    <h2>Coupes d'Europe</h2>
    <div class="continental-grid">
      <router-link
        v-for="c in filteredContinental"
        :key="c.id"
        class="continental-card"
        :class="`continental-card--${c.code.toLowerCase()}`"
        :to="`/competitions/${c.id}`"
      >
        <span class="continental-mark">
          ★
          <LateBadge :count="stats[c.id]?.late" />
        </span>
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
        <span class="card-flag">
          <FlagIcon :country="c.country" />
          <LateBadge :count="c.league && stats[c.league.id]?.late" />
        </span>
        <div class="country-card-info">
          <span class="country-card-name">{{ c.country }}</span>
          <span class="country-card-badges">
            <span v-if="c.league" class="badge badge-league">Championnat</span>
            <span v-if="c.cup" class="badge badge-cup">Coupe</span>
          </span>
          <CalendarProgress v-if="c.league && stats[c.league.id]?.calendar.total" :progress="stats[c.league.id].calendar" />
        </div>
      </router-link>
    </div>
  </template>
  <p v-else-if="loaded && !filteredContinental.length" class="empty-state">Aucune compétition ne correspond à cette recherche.</p>

  <details class="add-form">
    <summary>Ajouter une compétition</summary>
    <form class="inline" @submit.prevent="submit">
      <input v-model="form.code" aria-label="Code" placeholder="Code (ex: FRANCE, LDC)" required />
      <input v-model="form.name" aria-label="Nom" placeholder="Nom (ex: France - Championnat 2027)" required />
      <select v-model="form.type" aria-label="Type">
        <option value="LEAGUE">Championnat</option>
        <option value="DOMESTIC_CUP">Coupe nationale</option>
        <option value="CONTINENTAL_CUP">Coupe d'Europe</option>
      </select>
      <input v-model="form.country" aria-label="Pays" placeholder="Pays (vide si continentale)" />
      <input v-model.number="form.season" aria-label="Saison" type="number" placeholder="Saison" required />
      <button type="submit">Ajouter</button>
    </form>
    <p v-if="error" class="error-text">{{ error }}</p>
  </details>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import api from '../services/api'
import FlagIcon from '../components/FlagIcon.vue'
import LateBadge from '../components/LateBadge.vue'
import CalendarProgress from '../components/CalendarProgress.vue'
import { calendarProgress, lateCount } from '../utils/competitionProgress'

const competitions = ref([])
const loaded = ref(false)
const error = ref('')
const search = ref('')
const stats = reactive({})

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

// LDC > EL > EC, puis le reste par ordre alphabetique.
const CONTINENTAL_ORDER = ['LDC', 'EL', 'EC']
const continentalRank = c => {
  const i = CONTINENTAL_ORDER.indexOf(c.code)
  return i === -1 ? CONTINENTAL_ORDER.length : i
}
const continental = computed(() =>
  competitions.value
    .filter(c => c.type === 'CONTINENTAL_CUP')
    .sort((a, b) => continentalRank(a) - continentalRank(b) || a.name.localeCompare(b.name))
)
const leagueCount = computed(() => countries.value.filter(c => c.league).length)
const cupCount = computed(() => countries.value.filter(c => c.cup).length)

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

// Matchs en retard des championnats et coupes d'Europe, avancement du calendrier des championnats
// (charge en tache de fond, la grille s'affiche sans attendre).
async function loadStats() {
  const today = new Date().toISOString().slice(0, 10)
  const tracked = competitions.value.filter(c => c.type !== 'DOMESTIC_CUP')
  await Promise.all(tracked.map(async c => {
    const matches = await api.getMatchesByCompetition(c.id)
    stats[c.id] = { late: lateCount(matches, today), calendar: calendarProgress(matches, c.totalRounds) }
  }))
}

async function load() {
  competitions.value = await api.getCompetitions()
  loaded.value = true
  loadStats()
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
