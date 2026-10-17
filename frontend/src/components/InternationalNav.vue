<template>
  <nav class="tab-bar international-nav" aria-label="Sélections nationales">
    <router-link to="/classements-internationaux" class="tab-btn">Classements et résultats</router-link>
    <router-link to="/calendrier-international" class="tab-btn">Calendrier</router-link>
    <router-link to="/classement-mondial" class="tab-btn">Classement mondial</router-link>
  </nav>
</template>

<script setup>
// Sous-onglets de l'onglet International (une seule entree dans la barre principale).
</script>

<style scoped>
.international-nav .tab-btn {
  text-decoration: none;
}

.international-nav .router-link-active {
  color: white;
  background: linear-gradient(135deg, var(--pitch-800), var(--primary));
  box-shadow: 0 3px 10px rgba(15, 107, 59, 0.3);
}
</style>
