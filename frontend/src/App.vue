<template>
  <div class="topbar">
    <div class="brand">
      <span class="brand-mark">⚽</span>
      Foot Results
    </div>
    <nav class="tabs-nav">
      <router-link to="/">Compétitions</router-link>
      <router-link to="/teams">Équipes</router-link>
      <router-link to="/calendrier">Calendrier</router-link>
      <router-link to="/classement-uefa">Classement UEFA</router-link>
      <router-link to="/calendrier-international">Calendrier international</router-link>
      <router-link to="/classements-internationaux">Classements internationaux</router-link>
    </nav>
  </div>
  <router-view />
</template>
