<template>
  <select v-model="status" aria-label="Statut">
    <option value="">À venir</option>
    <option value="POSTPONED">Reporté</option>
    <option value="SUSPENDED">Suspendu</option>
    <option value="FORFEIT">Forfait</option>
  </select>
</template>

<script setup>
// Statut force d'un match pas encore joue ("" = a venir, statut deduit des scores).
const status = defineModel({ type: String, default: '' })
</script>
