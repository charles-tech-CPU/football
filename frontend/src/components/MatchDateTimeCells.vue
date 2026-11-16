<template>
  <td><input v-model="date" aria-label="Date" class="date-input" type="date" /></td>
  <td>
    <input v-model="time" aria-label="Heure" class="time-input" type="time" />
    <button
      v-if="time"
      type="button"
      class="clear-time-btn"
      title="Supprimer l'horaire (match à heure inconnue)"
      aria-label="Supprimer l'horaire"
      @click="time = ''"
    >
      ✕
    </button>
    <span v-if="flagMissingTime && !time" class="no-time-tag">Sans horaire</span>
  </td>
</template>

<script setup>
// Les cellules date et heure d'une ligne de match editable. Le bouton ✕ vide l'heure (le champ
// heure du navigateur se vide mal au clavier) ; l'enregistrement envoie alors une heure nulle.
defineProps({
  // Signale les matchs dont seule la date est connue
  flagMissingTime: { type: Boolean, default: false }
})
const date = defineModel('date', { type: String, default: '' })
const time = defineModel('time', { type: String, default: '' })
</script>
