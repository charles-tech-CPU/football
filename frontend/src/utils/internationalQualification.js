// Places qualificatives des competitions de selections nationales (saison 2026-2027), par
// position dans le groupe. Couleurs communes a toutes les confederations :
//   vert   = qualifie pour la phase finale / les quarts
//   bleu   = promu dans la ligue superieure
//   jaune  = qualification possible selon le classement entre groupes (meilleurs 2es/3es)
//   orange = barrage
//   rouge  = relegation (directe ou via barrage selon le classement entre groupes)
// Sources : pages Wikipedia de chaque competition (section Format), septembre 2026.

const KINDS = {
  qualified: { cls: 'standing-green', legendClass: 'legend-green' },
  promoted: { cls: 'standing-blue', legendClass: 'legend-blue' },
  possible: { cls: 'standing-yellow', legendClass: 'legend-yellow' },
  playoff: { cls: 'standing-orange-dark', legendClass: 'legend-orange-dark' },
  relegated: { cls: 'standing-red', legendClass: 'legend-red' }
}

// Pays hotes de la CAN 2027, qualifies d'office : dans leur groupe, l'hote est qualifie
// quelle que soit sa place, et seul le meilleur non-hote prend l'autre place.
const CAN_2027_HOSTS = new Set(['Kenya', 'Ouganda', 'Tanzanie'])

// Pour chaque competition : liste de regles { league (prefixe du nom de groupe, optionnel),
// ranks, kind, label }. Le label sert a la legende.
const RULES = {
  UEFA_NL_2627: [
    { league: 'Ligue A', ranks: [1, 2], kind: 'qualified', label: 'Ligue A : quarts de finale' },
    { league: 'Ligue A', ranks: [3], kind: 'playoff', label: 'Ligue A : barrage si parmi les 2 moins bons 3es' },
    { league: 'Ligue A', ranks: [4], kind: 'relegated', label: 'Ligue A : relégué (2 moins bons 4es) ou barrage (2 meilleurs 4es)' },
    { league: 'Ligue B', ranks: [1], kind: 'promoted', label: 'Ligue B : promu en Ligue A' },
    { league: 'Ligue B', ranks: [2], kind: 'playoff', label: 'Ligue B : barrage de promotion' },
    { league: 'Ligue C', ranks: [1], kind: 'promoted', label: 'Ligue C : promu en Ligue B' },
    { league: 'Ligue D', ranks: [1, 2, 3], kind: 'promoted', label: 'Ligue D : tous promus en Ligue C (dernière édition de la Ligue D)' }
  ],
  CONCACAF_NL_2627: [
    { league: 'Ligue A', ranks: [1, 2], kind: 'qualified', label: 'Ligue A : quarts de finale' },
    { league: 'Ligue A', ranks: [3, 4], kind: 'playoff', label: 'Ligue A : tour préliminaire de la Gold Cup' },
    { league: 'Ligue A', ranks: [5, 6], kind: 'relegated', label: 'Ligue A : relégué en Ligue B' },
    { league: 'Ligue B', ranks: [1], kind: 'promoted', label: 'Ligue B : promu en Ligue A + qualifié Gold Cup' },
    { league: 'Ligue B', ranks: [2], kind: 'possible', label: 'Ligue B : tour préliminaire Gold Cup si parmi les 2 meilleurs 2es' },
    { league: 'Ligue B', ranks: [4], kind: 'relegated', label: 'Ligue B : relégué en Ligue C' },
    { league: 'Ligue C', ranks: [1], kind: 'promoted', label: 'Ligue C : promu en Ligue B' },
    { league: 'Ligue C', ranks: [2], kind: 'possible', label: 'Ligue C : promu si meilleur 2e' }
  ],
  CAF_CAN_2027: [
    { ranks: [1, 2], kind: 'qualified', label: 'Qualifié pour la CAN 2027 (hôte qualifié d\'office)' }
  ],
  AFC_ASIANCUP_2027: [
    { ranks: [1, 2], kind: 'qualified', label: 'Huitièmes de finale' },
    { ranks: [3], kind: 'possible', label: 'Huitièmes si parmi les 4 meilleurs 3es' }
  ]
}

function rulesFor(competitionCode, groupName) {
  return (RULES[competitionCode] ?? []).filter(r => !r.league || groupName?.startsWith(r.league))
}

/** Classe CSS de chaque ligne d'un groupe (meme ordre que rows), '' si aucune. */
export function internationalRowClasses(competitionCode, groupName, rows) {
  const rules = rulesFor(competitionCode, groupName)
  const kindForRank = rank => rules.find(r => r.ranks.includes(rank))?.kind

  if (competitionCode === 'CAF_CAN_2027' && rows.some(r => CAN_2027_HOSTS.has(r.teamName))) {
    // Groupe avec un hote : l'hote + le meilleur non-hote sont qualifies.
    const bestOther = rows.find(r => !CAN_2027_HOSTS.has(r.teamName))
    return rows.map(r => (CAN_2027_HOSTS.has(r.teamName) || r === bestOther) ? KINDS.qualified.cls : '')
  }

  return rows.map((_, index) => {
    const kind = kindForRank(index + 1)
    return kind ? KINDS[kind].cls : ''
  })
}

/** Legende de la competition : [{ legendClass, label }]. */
export function internationalLegend(competitionCode) {
  return (RULES[competitionCode] ?? []).map(r => ({ legendClass: KINDS[r.kind].legendClass, label: r.label }))
}
