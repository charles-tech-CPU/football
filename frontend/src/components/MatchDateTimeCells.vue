<template>
  <td><input v-model="date" aria-label="Date" class="date-input" type="date" /></td>
  <td>
    <input v-model="time" aria-label="Heure" class="time-input" type="time" />
    <span v-if="flagMissingTime && !time" class="no-time-tag">Sans horaire</span>
  </td>
</template>

<script setup>
// Les cellules date et heure d'une ligne de match editable.
defineProps({
  // Signale les matchs dont seule la date est connue
  flagMissingTime: { type: Boolean, default: false }
})
const date = defineModel('date', { type: String, default: '' })
const time = defineModel('time', { type: String, default: '' })
</script>
