<template>
  <div class="inline slot-form">
    <label>Compétition
      <select :value="modelValue.competitionCode" aria-label="Compétition" @change="set('competitionCode', $event.target.value)">
        <option v-for="c in SLOT_COMPETITIONS" :key="c.code" :value="c.code">{{ c.label }}</option>
      </select>
    </label>
    <label>Tour d'entrée
      <select :value="modelValue.entryStage" aria-label="Tour d'entrée" @change="set('entryStage', Number($event.target.value))">
        <option v-for="s in SLOT_STAGES" :key="s.stage" :value="s.stage">{{ s.label }}</option>
      </select>
    </label>
    <label>Pays
      <input :value="modelValue.country" aria-label="Pays" placeholder="ex: Finlande" @change="set('country', $event.target.value.trim())" />
    </label>
    <label>Voie d'accès
      <input :value="modelValue.route" aria-label="Voie d'accès" placeholder="ex: Champion, Tenant du titre" @input="set('route', $event.target.value)" />
    </label>
    <label>Club
      <TeamSelect
        :model-value="modelValue.teamId"
        :teams="teams"
        aria-label="Club"
        placeholder="Automatique"
        @update:model-value="v => set('teamId', v)"
      />
    </label>
  </div>
</template>

<script setup>
// Champs d'une place europeenne (creation ou modification, cf. EuropeanSlotsView).
import TeamSelect from './TeamSelect.vue'
import { SLOT_COMPETITIONS, SLOT_STAGES } from '../utils/europeanSlots.js'

const props = defineProps({
  modelValue: { type: Object, required: true },
  // Clubs du pays de la place.
  teams: { type: Array, default: () => [] }
})
const emit = defineEmits(['update:modelValue'])

function set(field, value) {
  emit('update:modelValue', { ...props.modelValue, [field]: value })
}
</script>

<style scoped>
.slot-form {
  margin: 8px 0;
}
</style>
