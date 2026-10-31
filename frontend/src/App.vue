<template>
  <header class="topbar">
    <div class="topbar-inner">
      <a :href="PORTAL_URL" class="portal-link" title="Retour au portail du foyer">← Portail</a>
      <router-link to="/" class="brand">
        <span class="brand-mark">
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <circle cx="12" cy="12" r="10" fill="none" stroke="currentColor" stroke-width="1.6" />
            <path d="M12 7.2l3.3 2.4-1.3 3.9H10l-1.3-3.9z" fill="currentColor" />
            <path d="M12 2v5.2M15.3 9.6l4.9-1.6M14 13.5l3 4.2M10 13.5l-3 4.2M8.7 9.6L3.8 8" stroke="currentColor" stroke-width="1.4" fill="none" />
          </svg>
        </span>
        <span class="brand-text">
          <span class="brand-name">Foot Results</span>
          <span class="brand-sub">Saison {{ seasonLabel }}</span>
        </span>
      </router-link>
      <nav class="tabs-nav">
        <router-link to="/">Compétitions</router-link>
        <router-link to="/teams">Équipes</router-link>
        <router-link to="/tete-a-tete">Tête-à-tête</router-link>
        <router-link to="/calendrier">Calendrier</router-link>
        <router-link to="/classement-uefa">Classement UEFA</router-link>
        <router-link to="/qualifies-europe">Qualifiés Europe</router-link>
        <router-link to="/statistiques">Statistiques</router-link>
        <router-link to="/classements-internationaux" :class="{ 'router-link-exact-active': isInternational }">International</router-link>
      </nav>
    </div>
  </header>

  <main class="page">
    <router-view v-slot="{ Component, route }">
      <div :key="route.path" class="page-enter">
        <component :is="Component" />
      </div>
    </router-view>
  </main>

  <footer class="site-footer">
    <span>⚽ Foot Results</span>
    <span class="site-footer-sep">·</span>
    <span>Championnats européens, coupes nationales, coupes d'Europe et sélections</span>
  </footer>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { PORTAL_URL } from './portal'
import api from './services/api'
import { currentSeason } from './utils/archives'

// Saison en cours = celle des coupes d'Europe (2027 -> "2026-2027").
const season = ref(2027)
const seasonLabel = computed(() => `${season.value - 1}-${season.value}`)
onMounted(async () => {
  season.value = currentSeason(await api.getCompetitions({ type: 'CONTINENTAL_CUP' })) ?? season.value
})

// Un seul onglet "International" pour ses trois sous-pages (cf. InternationalNav).
const INTERNATIONAL_PATHS = ['/classements-internationaux', '/calendrier-international', '/classement-mondial']
const currentRoute = useRoute()
const isInternational = computed(() => INTERNATIONAL_PATHS.includes(currentRoute.path))
</script>
