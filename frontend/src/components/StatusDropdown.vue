<template>
  <div ref="root" class="status-dropdown">
    <button type="button" class="status-dropdown-trigger" @click="open = !open">
      {{ summary }}
      <span class="chevron">▾</span>
    </button>
    <div v-if="open" class="status-dropdown-menu">
      <label v-for="opt in options" :key="opt.key">
        <input v-model="local[opt.key]" type="checkbox" @change="emitChange" />
        {{ opt.label }}
      </label>
      <label class="europe-select">
        Europe (saison préc.)
        <select v-model="local.previousEuropeCompetition" @change="emitChange">
          <option :value="null">Aucune</option>
          <option value="LDC">Ligue des Champions</option>
          <option value="EL">Europa League</option>
          <option value="ECL">Conference League</option>
        </select>
      </label>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'

const props = defineProps({
  modelValue: { type: Object, required: true }
})
const emit = defineEmits(['update:modelValue'])

const options = [
  { key: 'defendingChampion', label: 'Tenant du titre' },
  { key: 'promoted', label: 'Promu' },
  { key: 'previousCupWinner', label: 'Vainqueur coupe (préc.)' }
]

const open = ref(false)
const root = ref(null)
const local = reactive({ ...props.modelValue })

watch(() => props.modelValue, v => Object.assign(local, v))

const EUROPE_LABELS = { LDC: 'LDC (préc.)', EL: 'EL (préc.)', ECL: 'ECL (préc.)' }

const summary = computed(() => {
  const active = options.filter(opt => local[opt.key]).map(opt => opt.label)
  if (local.previousEuropeCompetition) active.push(EUROPE_LABELS[local.previousEuropeCompetition])
  return active.length ? active.join(', ') : 'Aucun statut'
})

function emitChange() {
  emit('update:modelValue', { ...local })
}

function handleClickOutside(e) {
  if (root.value && !root.value.contains(e.target)) open.value = false
}

onMounted(() => document.addEventListener('click', handleClickOutside))
onBeforeUnmount(() => document.removeEventListener('click', handleClickOutside))
</script>

<style scoped>
.status-dropdown {
  position: relative;
  display: inline-block;
}

.status-dropdown-trigger {
  background: var(--surface);
  color: var(--text);
  border: 1px solid var(--border);
  font-weight: 400;
  font-size: 0.9em;
  padding: 6px 10px;
  min-width: 160px;
  text-align: left;
  display: inline-flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.chevron {
  color: var(--text-muted);
  font-size: 0.8em;
}

.status-dropdown-menu {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  z-index: 5;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 8px;
  box-shadow: var(--shadow);
  padding: 8px 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  white-space: nowrap;
}

.status-dropdown-menu label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 400;
  font-size: 0.9em;
  cursor: pointer;
}

.europe-select {
  border-top: 1px solid var(--border);
  padding-top: 8px;
  margin-top: 2px;
  justify-content: space-between;
}

.europe-select select {
  font-size: 0.85em;
  padding: 2px 4px;
}
</style>
