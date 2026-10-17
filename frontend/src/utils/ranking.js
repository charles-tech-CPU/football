// Evolution d'un rang depuis le debut de saison (classement UEFA) ou une autre reference
// (since) : startRank - rank places gagnees (> 0) ou perdues (< 0). Rien a afficher sans rang de depart.
export function rankMove(rank, startRank, since = 'le début de saison') {
  if (rank == null || startRank == null) return null
  const delta = startRank - rank
  if (delta > 0) return { label: `▲ ${delta}`, cls: 'move-up', title: `${delta} place${delta > 1 ? 's' : ''} gagnée${delta > 1 ? 's' : ''} depuis ${since}` }
  if (delta < 0) return { label: `▼ ${-delta}`, cls: 'move-down', title: `${-delta} place${delta < -1 ? 's' : ''} perdue${delta < -1 ? 's' : ''} depuis ${since}` }
  return { label: '=', cls: 'move-same', title: since === 'le début de saison' ? 'Même place qu’en début de saison' : `Même place qu’il y a ${since}` }
}

// Ecart de total avec la ligne precedente d'un classement trie (null pour la 1re ligne).
export function gapToPrevious(rows, index, totalOf = r => r.total) {
  if (index <= 0 || index >= rows.length) return null
  return totalOf(rows[index - 1]) - totalOf(rows[index])
}

// "1er", "2e"...
export function formatRank(rank) {
  return rank === 1 ? '1er' : `${rank}e`
}
