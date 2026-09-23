import { canonicalCountry } from './countryFlags'

// Couleurs inspirees du drapeau pour les pays les plus courants ; le reste
// des pays recoit une couleur generee de facon deterministe (meme pays =
// toujours la meme couleur), pour distinguer visuellement les equipes par
// pays dans l'ecran Equipes.
const CURATED = {
  France: { bg: '#0055a4', fg: '#000000' },
  Espagne: { bg: '#aa151b', fg: '#fcd116' },
  Allemagne: { bg: '#000000', fg: '#ffce00' },
  Italie: { bg: '#008c45', fg: '#ffffff' },
  Angleterre: { bg: '#ffffff', fg: '#c8102e' },
  Portugal: { bg: '#006600', fg: '#ff0000' },
  'Pays-Bas': { bg: '#ff8200', fg: '#ffffff' },
  Belgique: { bg: '#000000', fg: '#fdda24' },
  Ecosse: { bg: '#0065bd', fg: '#ffffff' },
  Turquie: { bg: '#e30a17', fg: '#ffffff' },
  Russie: { bg: '#0039a6', fg: '#ffffff' },
  Ukraine: { bg: '#0057b7', fg: '#ffd700' },
  Pologne: { bg: '#ffffff', fg: '#dc143c' },
  Suede: { bg: '#006aa7', fg: '#fecc02' },
  Norvege: { bg: '#ba0c2f', fg: '#ffffff' },
  Danemark: { bg: '#c60c30', fg: '#ffffff' },
  Suisse: { bg: '#d52b1e', fg: '#ffffff' },
  Autriche: { bg: '#ed2939', fg: '#ffffff' },
  Grece: { bg: '#0d5eaf', fg: '#ffffff' },
  Croatie: { bg: '#ff0000', fg: '#ffffff' },
  Serbie: { bg: '#c6363c', fg: '#ffffff' }
}

function hashString(str) {
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    hash = str.codePointAt(i) + ((hash << 5) - hash)
  }
  return Math.abs(hash)
}

function hslToRgb(h, s, l) {
  s /= 100
  l /= 100
  const k = n => (n + h / 30) % 12
  const a = s * Math.min(l, 1 - l)
  const f = n => l - a * Math.max(-1, Math.min(k(n) - 3, Math.min(9 - k(n), 1)))
  return [255 * f(0), 255 * f(8), 255 * f(4)]
}

function relativeLuminance([r, g, b]) {
  const [rs, gs, bs] = [r, g, b].map(v => {
    const c = v / 255
    return c <= 0.03928 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4
  })
  return 0.2126 * rs + 0.7152 * gs + 0.0722 * bs
}

function generatedStyle(country) {
  const hue = hashString(country) % 360
  const bg = `hsl(${hue}, 62%, 42%)`
  const fg = relativeLuminance(hslToRgb(hue, 62, 42)) > 0.35 ? '#111111' : '#ffffff'
  return { bg, fg }
}

const cache = new Map()

export function teamCountryStyle(rawCountry) {
  const country = canonicalCountry(rawCountry)
  if (!country) return { bg: '#e2e8e4', fg: '#667370' }
  if (cache.has(country)) return cache.get(country)
  const style = CURATED[country] ?? generatedStyle(country)
  cache.set(country, style)
  return style
}
