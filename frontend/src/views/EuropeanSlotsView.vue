<template>
  <section v-if="season" class="hero">
    <span class="hero-eyebrow">Coupes d'Europe</span>
    <h1>Qualifiés européens {{ seasonLabel(season) }}</h1>
    <p class="section-intro">
      Places de chaque pays pour la saison {{ seasonLabel(season) }}, reprises des tours d'entrée de
      {{ seasonLabel(season - 1) }} et modifiables à la main (nouveau cycle UEFA, pays qui change de rang,
      tenants du titre...). Les clubs se remplissent à la clôture du championnat et de la coupe de chaque pays.
    </p>
  </section>

  <template v-if="loaded">
    <div v-if="!slots.length" class="empty-state">
      <p>Aucune place pour {{ seasonLabel(season) }}.</p>
      <button type="button" :disabled="busy" @click="generate">
        Générer les places d'après {{ seasonLabel(season - 1) }}
      </button>
    </div>

    <template v-else>
      <div class="tab-bar">
        <button
          v-for="c in SLOT_COMPETITIONS"
          :key="c.code"
          class="tab-btn"
          :class="{ active: activeCode === c.code }"
          @click="activeCode = c.code"
        >
          {{ c.label }} ({{ fillProgress(slots, c.code).filled }}/{{ fillProgress(slots, c.code).total }})
        </button>
      </div>

      <div class="slots-toolbar">
        <input v-model="search" aria-label="Filtrer par pays ou club" placeholder="Filtrer par pays ou club..." />
        <button type="button" :disabled="busy" @click="fillAll">Remplir d'après les clôtures</button>
      </div>

      <div v-for="group in groups" :key="group.stage" class="standings-group">
        <h2>{{ group.label }} <span class="slots-count">{{ group.slots.length }}</span></h2>
        <table>
          <thead>
            <tr>
              <th>Pays</th>
              <th>Voie d'accès</th>
              <th>Club</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <template v-for="s in group.slots" :key="s.id">
              <tr v-if="editing?.id !== s.id">
                <td><span class="team-cell"><FlagIcon :country="s.country" /> {{ s.country }}</span></td>
                <td>{{ s.route ?? '—' }}</td>
                <td>
                  <span v-if="s.teamId" class="team-cell">
                    <TeamLogo :name="s.teamName" :logo-path="s.teamLogoPath" :country="s.country" />
                    <router-link :to="`/equipes/${s.teamId}`">{{ s.teamName }}</router-link>
                    <span v-if="s.manual" title="Club choisi à la main">✋</span>
                  </span>
                  <span v-else class="muted">À déterminer</span>
                </td>
                <td class="slot-actions">
                  <button type="button" :aria-label="`Modifier la place ${s.country}`" @click="startEdit(s)">✏️</button>
                  <button type="button" class="delete-btn" :aria-label="`Supprimer la place ${s.country}`" @click="remove(s)">🗑</button>
                </td>
              </tr>
              <tr v-else class="slot-edit">
                <td colspan="4">
                  <SlotForm v-model="editing" :teams="teamsByCountry[editing.country] ?? []" />
                  <p class="section-intro">Choisir un club fige la place ; sans club, elle revient au remplissage automatique.</p>
                  <button type="button" :disabled="busy" @click="saveEdit">Enregistrer</button>
                  <button type="button" @click="editing = null">Annuler</button>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
      <p v-if="!groups.length" class="empty-state">Aucune place ne correspond.</p>

      <details class="add-form">
        <summary>Ajouter une place</summary>
        <SlotForm v-model="adding" :teams="teamsByCountry[adding.country] ?? []" />
        <button type="button" :disabled="busy || !adding.country" @click="add">Ajouter</button>
      </details>
    </template>
    <p v-if="error" class="error-text">{{ error }}</p>
  </template>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import api from '../services/api'
import FlagIcon from '../components/FlagIcon.vue'
import TeamLogo from '../components/TeamLogo.vue'
import SlotForm from '../components/EuropeanSlotForm.vue'
import { SLOT_COMPETITIONS, fillProgress, seasonLabel, slotsByStage } from '../utils/europeanSlots.js'

// Saison par defaut : celle qui suit la saison en cours des coupes d'Europe.
const season = ref(null)
const slots = ref([])
const loaded = ref(false)
const busy = ref(false)
const error = ref('')
const activeCode = ref('LDC')
const search = ref('')
const editing = ref(null)
const adding = ref(emptySlot())
const teamsByCountry = reactive({})

const groups = computed(() => slotsByStage(slots.value, activeCode.value, search.value))

function emptySlot() {
  return { competitionCode: 'LDC', entryStage: 6, country: '', route: '', teamId: null }
}

async function loadTeams(country) {
  if (!country || teamsByCountry[country]) return
  teamsByCountry[country] = await api.getTeams({ country })
}

async function run(action) {
  busy.value = true
  error.value = ''
  try {
    await action()
  } catch (e) {
    error.value = e.response?.data?.error ?? 'Erreur lors de l\'enregistrement.'
  } finally {
    busy.value = false
  }
}

function payload(slot) {
  return {
    season: season.value,
    competitionCode: slot.competitionCode,
    entryStage: slot.entryStage,
    country: slot.country,
    route: slot.route || null,
    teamId: slot.teamId || null
  }
}

const generate = () => run(async () => { slots.value = await api.generateEuropeanSlots(season.value) })
const fillAll = () => run(async () => { slots.value = await api.fillEuropeanSlots(season.value) })

function startEdit(slot) {
  editing.value = { ...slot, route: slot.route ?? '' }
}

const saveEdit = () => run(async () => {
  await api.updateEuropeanSlot(editing.value.id, payload(editing.value))
  editing.value = null
  slots.value = await api.getEuropeanSlots(season.value)
})

const add = () => run(async () => {
  await api.createEuropeanSlot(payload(adding.value))
  adding.value = emptySlot()
  slots.value = await api.getEuropeanSlots(season.value)
})

function remove(slot) {
  if (!window.confirm(`Supprimer cette place (${slot.country}) ?`)) return
  return run(async () => {
    await api.deleteEuropeanSlot(slot.id)
    slots.value = slots.value.filter(s => s.id !== slot.id)
  })
}

watch(() => editing.value?.country, loadTeams)
watch(() => adding.value.country, loadTeams)

onMounted(async () => {
  const competitions = await api.getCompetitions({ type: 'CONTINENTAL_CUP' })
  const current = Math.max(...competitions.map(c => c.season), new Date().getFullYear())
  slots.value = await api.getEuropeanSlots()
  season.value = slots.value[0]?.season ?? current + 1
  loaded.value = true
})
</script>

<style scoped>
.slots-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin: 12px 0;
}
.slots-toolbar input {
  flex: 1;
  min-width: 200px;
}
.slots-count {
  color: var(--text-muted);
  font-size: 0.7em;
}
.slot-actions {
  white-space: nowrap;
  text-align: right;
}
.muted {
  color: var(--text-muted);
}
</style>
