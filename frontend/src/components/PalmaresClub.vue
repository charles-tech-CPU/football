<template>
  <span class="team-cell palmares-club">
    <FlagIcon v-if="withFlag" :country="club.country" />
    <TeamLogo :name="club.name" :logo-path="club.logoPath" :country="club.country ?? country" />
    <router-link v-if="club.teamId" :to="`/equipes/${club.teamId}`" class="team-link">{{ club.name }}</router-link>
    <span v-else>{{ club.name }}</span>
  </span>
</template>

<script setup>
// Club du palmares (PalmaresDto.Club) : drapeau de son pays pour les coupes d'Europe, logo et lien
// vers sa fiche quand il existe dans l'appli, nom Wikipedia sinon (club disparu ou hors base).
import FlagIcon from './FlagIcon.vue'
import TeamLogo from './TeamLogo.vue'

defineProps({
  club: { type: Object, required: true },
  // Pays du palmares, pour la couleur des initiales quand le club n'a pas de logo.
  country: { type: String, default: null },
  withFlag: { type: Boolean, default: false }
})
</script>

<style scoped>
.palmares-club {
  gap: 6px;
}
</style>
