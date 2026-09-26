// Le backend renvoie l'heure au format HH:mm:ss (LocalTime Java) ; on
// n'affiche que HH:mm cote UI.
export function formatTime(time) {
  if (!time) return ''
  return time.slice(0, 5)
}

// Date ISO "AAAA-MM-JJ" en libelle court : "sam. 18 oct.". Parsee en date locale (et non UTC)
// pour ne pas decaler d'un jour.
export function formatShortDate(date) {
  if (!date) return ''
  const [y, m, d] = date.split('-').map(Number)
  return new Date(y, m - 1, d).toLocaleDateString('fr-FR', { weekday: 'short', day: 'numeric', month: 'short' })
}
