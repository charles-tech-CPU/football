<template>
  <header class="topbar">
    <div class="topbar-inner">
      <router-link to="/" class="brand">
        <span class="brand-mark">
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <circle cx="12" cy="12" r="10" fill="none" stroke="currentColor" stroke-width="1.6" />
            <path d="M12 7.2l3.3 2.4-1.3 3.9H10l-1.3-3.9z" fill="currentColor" />
            <path d="M12 2v5.2M15.3 9.6l4.9-1.6M14 13.5l3 4.2M10 13.5l-3 4.2M8.7 9.6L3.8 8" stroke="currentColor" stroke-width="1.4" fill="none" />
          </svg>
        </span>
        <span class="brand-text">
          <span class="brand-name">Foot Results</span>
          <span class="brand-sub">Saison 2026-2027</span>
        </span>
      </router-link>
      <nav class="tabs-nav">
        <router-link to="/">Compétitions</router-link>
        <router-link to="/teams">Équipes</router-link>
        <router-link to="/calendrier">Calendrier</router-link>
        <router-link to="/classement-uefa">Classement UEFA</router-link>
        <router-link to="/calendrier-international">Calendrier international</router-link>
        <router-link to="/classements-internationaux">Classements internationaux</router-link>
      </nav>
    </div>
  </header>

  <main class="page">
    <router-view v-slot="{ Component, route }">
      <div :key="route.path" class="page-enter">
        <component :is="Component" />
      </div>
    </router-view>
  </main>

  <footer class="site-footer">
    <span>⚽ Foot Results</span>
    <span class="site-footer-sep">·</span>
    <span>Championnats européens, coupes nationales, coupes d'Europe et sélections</span>
  </footer>
</template>
