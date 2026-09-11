// Le backend renvoie l'heure au format HH:mm:ss (LocalTime Java) ; on
// n'affiche que HH:mm cote UI.
export function formatTime(time) {
  if (!time) return ''
  return time.slice(0, 5)
}
