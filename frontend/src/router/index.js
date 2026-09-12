import { createRouter, createWebHistory } from 'vue-router'
import CompetitionsView from '../views/CompetitionsView.vue'
import CompetitionDetailView from '../views/CompetitionDetailView.vue'
import CountryView from '../views/CountryView.vue'
import TeamsView from '../views/TeamsView.vue'
import RecentResultsView from '../views/RecentResultsView.vue'
import CalendarView from '../views/CalendarView.vue'
import PostponedMatchesView from '../views/PostponedMatchesView.vue'

const routes = [
  { path: '/', name: 'competitions', component: CompetitionsView },
  { path: '/competitions/:id', name: 'competition-detail', component: CompetitionDetailView, props: true },
  {
    path: '/pays/:country',
    name: 'country-detail',
    component: CountryView,
    props: route => ({ country: decodeURIComponent(route.params.country) })
  },
  { path: '/teams', name: 'teams', component: TeamsView },
  { path: '/resultats', name: 'recent-results', component: RecentResultsView },
  { path: '/calendrier', name: 'calendar', component: CalendarView },
  { path: '/reportes-suspendus', name: 'postponed', component: PostponedMatchesView }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
