"""Extraction des tableaux "wikitable" d'une page Wikipedia (HTML de l'API REST), sans dependance.

Chaque tableau devient une grille de textes : rowspan/colspan recopies dans chaque case couverte,
notes de bas de page ([1], [a]) et espaces superflus retires. Utilise par palmares.py.
"""
import re
from html.parser import HTMLParser

NOTE = re.compile(r'\[[^\]]{1,12}\]')


VOID = {'area', 'base', 'br', 'col', 'embed', 'hr', 'img', 'input', 'link', 'meta', 'source', 'track', 'wbr'}
HIDDEN_TAGS = {'sup', 'style', 'script'}


class _TableParser(HTMLParser):
    def __init__(self):
        super().__init__(convert_charrefs=True)
        self.tables = []      # [(classes, caption, [[(text, rowspan, colspan, is_header, links)]], titres)]
        self._stack = []      # tableaux en cours (tableaux imbriques)
        self._cell = None
        self._depth = 0       # profondeur courante des balises ouvertes
        self._hidden = None   # profondeur de la balise masquee en cours (<sup>, display:none...)
        self._headings = {'h2': '', 'h3': '', 'h4': ''}   # titres de section en cours
        self._heading = None  # titre en cours de lecture : (niveau, [textes])

    def handle_starttag(self, tag, attrs):
        a = dict(attrs)
        if tag not in VOID:
            self._depth += 1
            hidden = tag in HIDDEN_TAGS or 'display:none' in (a.get('style') or '').replace(' ', '')
            if hidden and self._hidden is None:
                self._hidden = self._depth
        if tag in self._headings:
            self._heading = (tag, [])
        if tag == 'table':
            headings = [self._headings[h] for h in ('h2', 'h3', 'h4') if self._headings[h]]
            self._stack.append({'classes': a.get('class', ''), 'rows': [], 'caption': None, 'headings': headings})
        elif not self._stack:
            return
        elif tag == 'tr':
            self._stack[-1]['rows'].append([])
        elif tag in ('td', 'th'):
            if not self._stack[-1]['rows']:
                self._stack[-1]['rows'].append([])
            self._cell = {'text': [], 'rowspan': _int(a.get('rowspan')), 'colspan': _int(a.get('colspan')),
                          'th': tag == 'th', 'links': []}
        elif tag == 'br' and self._cell is not None:
            self._cell['text'].append(' / ')
        elif tag == 'caption':
            self._stack[-1]['caption'] = []
        elif tag == 'img' and self._cell is not None:
            # Drapeau d'un pays ("File:Flag_of_the_Netherlands.svg") -> lien "flag:the Netherlands".
            flag = re.search(r'Flag_of_(.+?)\.svg', a.get('resource') or a.get('src') or '')
            if flag:
                self._cell['links'].append('flag:' + flag.group(1).replace('_', ' '))
        elif tag == 'a' and self._cell is not None and self._hidden is None:
            self._cell['links'].append(a.get('title') or '')

    def handle_endtag(self, tag):
        if tag in VOID:
            return
        if self._hidden is not None and self._depth <= self._hidden:
            self._hidden = None
        self._depth -= 1
        if self._heading and tag == self._heading[0]:
            level, texts = self._heading
            self._headings[level] = re.sub(r'\s+', ' ', ''.join(texts)).strip()
            for lower in ('h3', 'h4')[('h2', 'h3', 'h4').index(level):]:
                self._headings[lower] = ''
            self._heading = None
        if not self._stack:
            return
        if tag == 'table':
            t = self._stack.pop()
            caption = ' '.join(t['caption'] or []).strip() if t['caption'] is not None else ''
            self.tables.append((t['classes'], caption, t['rows'], t['headings']))
        elif tag in ('td', 'th') and self._cell is not None:
            text = NOTE.sub('', ''.join(self._cell['text']))
            text = re.sub(r'\s+', ' ', text).strip(' /')
            self._stack[-1]['rows'][-1].append((text, self._cell['rowspan'], self._cell['colspan'],
                                               self._cell['th'], self._cell['links']))
            self._cell = None

    def handle_data(self, data):
        if self._hidden is not None:
            return
        if self._heading:
            self._heading[1].append(data)
        if self._cell is not None:
            self._cell['text'].append(data)
        elif self._stack and self._stack[-1]['caption'] is not None:
            self._stack[-1]['caption'].append(data)


def _int(value):
    try:
        return max(1, int(re.sub(r'\D', '', value or '1') or 1))
    except ValueError:
        return 1


def _grid(rows):
    grid, pending = [], {}   # pending[(row, col)] = case recopiee par un rowspan
    for r, cells in enumerate(rows):
        line, col = [], 0
        cells = list(cells)
        while cells or (r, col) in pending:
            if (r, col) in pending:
                line.append(pending.pop((r, col)))
                col += 1
                continue
            text, rowspan, colspan, th, links = cells.pop(0)
            cell = {'text': text, 'th': th, 'links': links}
            for c in range(colspan):
                line.append(cell)
                for k in range(1, rowspan):
                    pending[(r + k, col + c)] = cell
            col += colspan
        grid.append(line)
    return grid


def wikitables(html):
    """[{'caption', 'headings', 'rows': [[{'text', 'th', 'links'}]]}] pour chaque tableau 'wikitable' de
    la page ; headings = titres de section (h2, h3, h4) sous lesquels le tableau se trouve."""
    parser = _TableParser()
    parser.feed(html)
    return [{'caption': caption, 'headings': headings, 'rows': _grid(rows)}
            for classes, caption, rows, headings in parser.tables if 'wikitable' in classes]
