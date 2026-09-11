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
        <td>{{ t.name }}</td>
        <td>{{ t.country ?? '—' }}</td>
      </tr>
    </tbody>
  </table>
  <p v-else-if="loaded">Aucune équipe ne correspond à ce filtre.</p>
  <p v-if="loaded">{{ teams.length }} équipes au total.</p>

  <h2>Ajouter une équipe</h2>
  <form class="inline" @submit.prevent="submit">
    <input v-model="form.name" placeholder="Nom du club" required />
    <input v-model="form.country" placeholder="Pays" />
    <button type="submit">Ajouter</button>
  </form>
  <p v-if="error" style="color:#ff6b6b">{{ error }}</p>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import api from '../services/api'

const teams = ref([])
const loaded = ref(false)
const error = ref('')
const search = ref('')
const form = reactive({ name: '', country: '' })

const filtered = computed(() => {
  const q = search.value.trim().toLowerCase()
  if (!q) return teams.value
  return teams.value.filter(t => t.name.toLowerCase().includes(q) || (t.country ?? '').toLowerCase().includes(q))
})

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

onMounted(load)
</script>
