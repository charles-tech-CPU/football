<template>
  <section class="hero">
    <span class="hero-eyebrow">Sélections nationales</span>
    <h1>Classements internationaux</h1>
    <p class="section-intro">
      Choisis une compétition, puis une édition : phase finale et qualifications sont regroupées dans
      la même édition. Classements recalculés en direct comme pour les championnats de clubs ;
      <span class="live-dot" aria-hidden="true"></span> = en cours cette saison.
    </p>
  </section>

  <InternationalNav />

  <template v-if="families.length">
    <div class="tab-bar" aria-label="Compétition">
      <button
        v-for="f in families"
        :key="f.key"
        class="tab-btn"
        :class="{ active: active?.family.key === f.key }"
        @click="selectFamily(f)"
      >{{ f.label }}<span v-if="f.current" class="live-dot" title="En cours cette saison"></span></button>
    </div>

    <div v-if="active" class="edition-row">
      <div class="tab-bar sub-tab-bar" aria-label="Édition">
        <button
          v-for="e in recentEditions"
          :key="e.key"
          class="tab-btn"
          :class="{ active: active.edition.key === e.key }"
          @click="selectEdition(e)"
        >{{ e.label }}<span v-if="e.current" class="live-dot" title="En cours cette saison"></span></button>
      </div>
      <select
        v-if="olderEditions.length"
        class="older-editions"
        :class="{ active: olderEditions.includes(active.edition) }"
        :value="olderEditions.includes(active.edition) ? active.edition.key : ''"
        aria-label="Éditions précédentes"
        @change="selectEdition(olderEditions.find(e => e.key === $event.target.value))"
      >
        <option value="" disabled>Éditions précédentes…</option>
        <option v-for="e in olderEditions" :key="e.key" :value="e.key">{{ e.label }}</option>
      </select>
    </div>

    <div v-if="active" class="phase-row">
      <div v-if="active.edition.phases.length > 1" class="tab-bar sub-tab-bar" aria-label="Phase">
        <button
          v-for="p in active.edition.phases"
          :key="p.competition.id"
          class="tab-btn"
          :class="{ active: active.phase.competition.id === p.competition.id }"
          @click="selectedId = p.competition.id"
        >{{ p.label }}</button>
      </div>
      <div class="tab-bar sub-tab-bar" aria-label="Affichage">
        <button class="tab-btn" :class="{ active: activeView === 'classement' }" @click="activeView = 'classement'">Classements</button>
        <button class="tab-btn" :class="{ active: activeView === 'resultats' }" @click="activeView = 'resultats'">Résultats</button>
      </div>
    </div>

    <div v-if="comp" class="competition-block">
      <h2>{{ comp.name }}</h2>
      <InternationalResults
        v-if="activeView === 'resultats'"
        :matches="matchesByCompetition[comp.id] ?? []"
        :rows="standingsByCompetition[comp.id] ?? []"
        @saved="loadFor(comp)"
      />
      <template v-else>
        <StandingsTable
          v-if="(standingsByCompetition[comp.id] ?? []).length"
          :rows="standingsByCompetition[comp.id]"
          :show-flags="true"
          :show-logos="false"
          :points-first="true"
          :show-base-legend="false"
          :group-row-classes="(groupName, rows) => internationalRowClasses(comp.code, groupName, rows)"
        />
        <p v-else-if="loadedIds.has(comp.id)" class="empty-state">
          Pas de phase de groupes dans cette compétition : voir l'onglet « Résultats ».
        </p>
        <ul v-if="internationalLegend(comp.code).length" class="standings-legend">
          <li v-for="(item, i) in internationalLegend(comp.code)" :key="i">
            <span class="legend-swatch" :class="item.legendClass"></span>{{ item.label }}
          </li>
        </ul>
      </template>
    </div>
  </template>
  <p v-else-if="loaded" class="empty-state">Aucune compétition de sélections.</p>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../services/api'
import StandingsTable from '../components/StandingsTable.vue'
import InternationalResults from '../components/InternationalResults.vue'
import InternationalNav from '../components/InternationalNav.vue'
import { internationalFamilies, locateCompetition } from '../utils/internationalEditions'
import { internationalLegend, internationalRowClasses } from '../utils/internationalQualification'

// Une seule competition affichee a la fois (phase d'une edition), dans l'URL (?competition=12)
// pour les liens depuis les listes de matchs (fiche club, tete-a-tete).
const route = useRoute()
const router = useRouter()

const competitions = ref([])
const standingsByCompetition = ref({})
const matchesByCompetition = ref({})
const loadedIds = ref(new Set())
const activeView = ref('classement')
const selectedId = ref(null)
const loaded = ref(false)

const currentSeason = computed(() => Math.max(...competitions.value.map(c => c.season ?? 0)))
const families = computed(() => internationalFamilies(competitions.value, currentSeason.value))
const active = computed(() => locateCompetition(families.value, selectedId.value))
const comp = computed(() => active.value?.phase.competition ?? null)

// Les editions les plus recentes en onglets, les autres (jusqu'a 1916 pour la Copa America) en liste.
const RECENT_EDITIONS = 10
const recentEditions = computed(() => active.value?.family.editions.slice(0, RECENT_EDITIONS) ?? [])
const olderEditions = computed(() => active.value?.family.editions.slice(RECENT_EDITIONS) ?? [])

// Nouvelle competition ou edition : sa phase finale (ou sa 1re phase) de l'edition la plus recente.
function selectFamily(family) {
  selectEdition(family.editions[0])
}

function selectEdition(edition) {
  selectedId.value = edition.phases[0].competition.id
}

// Classement + matchs (pour l'onglet Resultats), recharges ensemble pour que la grille des
// resultats suive toujours l'ordre du classement.
async function loadFor(c) {
  const [standings, matches] = await Promise.all([api.getStandings(c.id), api.getMatchesByCompetition(c.id)])
  standingsByCompetition.value = { ...standingsByCompetition.value, [c.id]: standings }
  matchesByCompetition.value = { ...matchesByCompetition.value, [c.id]: matches }
  loadedIds.value = new Set([...loadedIds.value, c.id])
}

// Par defaut : la competition demandee dans l'URL, sinon la premiere en cours cette saison.
function applyRouteCompetition() {
  const id = Number(route.query.competition)
  if (locateCompetition(families.value, id)) {
    selectedId.value = id
    return
  }
  const family = families.value.find(f => f.current) ?? families.value[0]
  if (family) selectEdition(family.editions.find(e => e.current) ?? family.editions[0])
}

async function load() {
  loaded.value = false
  competitions.value = await api.getCompetitions({ type: 'INTERNATIONAL' })
  applyRouteCompetition()
  if (comp.value) await loadFor(comp.value)
  loaded.value = true
}

watch(comp, c => {
  if (!loaded.value || !c) return
  router.replace({ query: { competition: c.id } })
  if (!loadedIds.value.has(c.id)) loadFor(c)
})

watch(() => route.query.competition, id => {
  if (loaded.value && id && Number(id) !== selectedId.value) applyRouteCompetition()
})

onMounted(load)
</script>

<style scoped>
.competition-block {
  margin-bottom: 32px;
}

.edition-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  align-items: flex-start;
}

.older-editions {
  margin-top: -8px;
  padding: 7px 12px;
  border-radius: 999px;
  font-size: 0.86em;
}

.older-editions.active {
  border-color: var(--primary);
  color: var(--primary-dark);
  font-weight: 700;
}

.phase-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 12px;
  align-items: flex-start;
}

.live-dot {
  display: inline-block;
  width: 7px;
  height: 7px;
  margin-left: 6px;
  vertical-align: middle;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 0 2px rgba(34, 197, 94, 0.25);
}
</style>
