<template>
  <section class="hero">
    <router-link class="back-link" to="/">← Toutes les compétitions</router-link>
    <h1>Archives</h1>
    <p class="section-intro">
      Les saisons terminées restent consultables telles quelles : classement figé, résultats et tableaux de
      coupe. Une saison passe aux archives dès que la suivante est créée.
    </p>
  </section>

  <template v-if="loaded">
    <p v-if="!archives.length" class="empty-state">
      Aucune saison archivée pour l'instant : clôture un championnat puis prépare sa saison suivante.
    </p>
    <section v-for="a in archives" :key="a.season" class="archive-season">
      <h2>Saison {{ a.season }}</h2>

      <div v-if="a.continental.length" class="continental-grid">
        <router-link
          v-for="c in a.continental"
          :key="c.id"
          class="continental-card"
          :class="`continental-card--${c.code.toLowerCase()}`"
          :to="`/competitions/${c.id}`"
        >
          <span class="continental-mark">★</span>
          <div class="country-card-info">
            <span class="country-card-name">{{ c.name }}</span>
            <span class="country-card-badges">
              <span v-if="c.closedAt" class="badge badge-finished">Clôturée 🔒</span>
            </span>
          </div>
        </router-link>
      </div>

      <div v-if="a.countries.length" class="country-grid">
        <router-link
          v-for="c in a.countries"
          :key="c.country"
          class="country-card"
          :to="{ path: `/pays/${encodeURIComponent(c.country)}`, query: { saison: a.season } }"
        >
          <span class="card-flag"><FlagIcon :country="c.country" /></span>
          <div class="country-card-info">
            <span class="country-card-name">{{ c.country }}</span>
            <span class="country-card-badges">
              <span v-if="c.league" class="badge badge-league">Championnat{{ c.league.closedAt ? ' 🔒' : '' }}</span>
              <span v-if="c.cup" class="badge badge-cup">Coupe{{ c.cup.closedAt ? ' 🔒' : '' }}</span>
            </span>
          </div>
        </router-link>
      </div>
    </section>
  </template>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import api from '../services/api'
import FlagIcon from '../components/FlagIcon.vue'
import { archivedSeasons } from '../utils/archives.js'

const competitions = ref([])
const loaded = ref(false)
const archives = computed(() => archivedSeasons(competitions.value))

onMounted(async () => {
  competitions.value = await api.getCompetitions()
  loaded.value = true
})
</script>

<style scoped>
.archive-season {
  margin-bottom: 32px;
}
.archive-season .continental-grid {
  margin-bottom: 16px;
}
</style>
