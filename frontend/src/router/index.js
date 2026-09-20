import { createRouter, createWebHistory } from 'vue-router'
import CompetitionsView from '../views/CompetitionsView.vue'
import CompetitionDetailView from '../views/CompetitionDetailView.vue'
import CountryView from '../views/CountryView.vue'
import TeamsView from '../views/TeamsView.vue'
import CalendarView from '../views/CalendarView.vue'
import PostponedMatchesView from '../views/PostponedMatchesView.vue'
import UefaRankingsView from '../views/UefaRankingsView.vue'
import InternationalCalendarView from '../views/InternationalCalendarView.vue'
import InternationalStandingsView from '../views/InternationalStandingsView.vue'

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
  { path: '/calendrier', name: 'calendar', component: CalendarView },
  { path: '/reportes-suspendus', name: 'postponed', component: PostponedMatchesView },
  { path: '/classement-uefa', name: 'uefa-rankings', component: UefaRankingsView },
  { path: '/calendrier-international', name: 'international-calendar', component: InternationalCalendarView },
  { path: '/classements-internationaux', name: 'international-standings', component: InternationalStandingsView }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
