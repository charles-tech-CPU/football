<template>
  <div class="filters">
    <select v-model="scope" aria-label="Pays">
      <option v-for="s in scopes" :key="s" :value="s">{{ s === EUROPE ? "Coupes d'Europe" : s }}</option>
    </select>
  </div>

  <template v-if="palmares?.competitions.length">
    <div class="tab-bar sub-tab-bar" aria-label="Compétition">
      <button
        v-for="c in palmares.competitions"
        :key="c.kind"
        class="tab-btn"
        :class="{ active: active?.kind === c.kind }"
        @click="kind = c.kind"
      >{{ c.label }}</button>
    </div>

    <div v-if="active" class="palmares-layout">
      <section class="stats-card titles-card">
        <h3>Clubs les plus titrés</h3>
        <ol>
          <li v-for="t in active.titles" :key="t.team.name">
            <PalmaresClub class="title-team" :club="t.team" :country="clubCountry" :with-flag="isEurope" />
            <span class="stats-value" :title="`Dernier titre : ${t.lastSeason}`">{{ t.titles }} <small>titre{{ t.titles > 1 ? 's' : '' }}</small></span>
          </li>
        </ol>
      </section>

      <div class="seasons-table">
        <table>
          <thead>
            <tr>
              <th>Saison</th>
              <th>{{ isLeague ? 'Champion' : 'Vainqueur' }}</th>
              <th v-if="hasRunnerUp">{{ isLeague ? '2e' : 'Finaliste' }}</th>
              <th v-if="hasThird">3e</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in active.seasons" :key="`${s.season}-${s.winner.name}`">
              <td class="season-cell">{{ s.season }}</td>
              <td class="winner"><PalmaresClub :club="s.winner" :country="clubCountry" :with-flag="isEurope" /></td>
              <td v-if="hasRunnerUp">
                <PalmaresClub v-if="s.runnerUp" :club="s.runnerUp" :country="clubCountry" :with-flag="isEurope" />
                <span v-else>—</span>
              </td>
              <td v-if="hasThird" class="third-cell">
                <PalmaresClub v-if="s.third" :club="s.third" :country="clubCountry" />
                <span v-else>—</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <p class="section-intro source-note">
      Palmarès historique d'après Wikipédia, depuis la première édition de chaque compétition
      ({{ active?.seasons.length }} saisons) ; 2e et 3e quand la source les donne.
    </p>
  </template>
  <p v-else-if="palmares" class="empty-state">Pas de palmarès pour ce pays.</p>
</template>

<script setup>
// Palmares historique d'un pays (championnat, coupe nationale) ou des coupes d'Europe (onglet
// Statistiques > Palmares), cf. PalmaresService.
import { computed, onMounted, ref, watch } from 'vue'
import api from '../services/api'
import PalmaresClub from './PalmaresClub.vue'

const EUROPE = 'Europe'
// Pays affiche a l'ouverture (a defaut, le premier de la liste : l'Europe).
const DEFAULT_SCOPE = 'France'

const scopes = ref([])
const scope = ref(null)
const kind = ref(null)
const palmares = ref(null)

const active = computed(() =>
  palmares.value?.competitions.find(c => c.kind === kind.value) ?? palmares.value?.competitions[0] ?? null)
const isLeague = computed(() => active.value?.kind === 'LEAGUE')
const isEurope = computed(() => scope.value === EUROPE)
// Pays des clubs d'un palmares national (couleur des initiales sans logo).
const clubCountry = computed(() => (isEurope.value ? null : scope.value))
const hasRunnerUp = computed(() => active.value?.seasons.some(s => s.runnerUp) ?? false)
const hasThird = computed(() => active.value?.seasons.some(s => s.third) ?? false)

watch(scope, async value => {
  if (!value) return
  palmares.value = await api.getPalmares(value)
  kind.value = palmares.value.competitions[0]?.kind ?? null
})

onMounted(async () => {
  scopes.value = await api.getPalmaresScopes()
  scope.value = scopes.value.includes(DEFAULT_SCOPE) ? DEFAULT_SCOPE : scopes.value[0] ?? null
})
</script>

<style scoped>
.palmares-layout {
  display: grid;
  grid-template-columns: minmax(220px, 300px) 1fr;
  gap: 16px;
  align-items: start;
}

@media (max-width: 760px) {
  .palmares-layout {
    grid-template-columns: 1fr;
  }
}

.stats-card {
  padding: 14px 16px;
  border: 1px solid var(--border);
  border-radius: 14px;
  background: var(--surface);
}

.stats-card h3 {
  margin: 0 0 10px;
  font-size: 1em;
}

.titles-card ol {
  margin: 0;
  padding-left: 22px;
  max-height: 560px;
  overflow-y: auto;
}

.titles-card li {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  padding: 4px 0;
}

.title-team {
  font-weight: 600;
  min-width: 0;
}

.stats-value {
  font-weight: 800;
  white-space: nowrap;
}

.stats-value small {
  color: var(--text-muted);
  font-weight: 400;
}

.seasons-table {
  max-height: 620px;
  overflow-y: auto;
}

.seasons-table table {
  margin-bottom: 0;
  overflow: visible;
}

.seasons-table thead th {
  position: sticky;
  top: 0;
  z-index: 1;
}

.season-cell {
  white-space: nowrap;
  color: var(--text-muted);
}

.third-cell {
  color: var(--text-muted);
}

.winner {
  font-weight: 700;
}

.source-note {
  margin-top: 12px;
  font-size: 0.85em;
}
</style>
