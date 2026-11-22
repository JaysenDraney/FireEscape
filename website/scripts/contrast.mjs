// Checks WCAG contrast ratios for the colors each look defines. Run: npm run themes:contrast
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const themes = ['ember', 'terminal', 'aurora', 'escape'];

const lum = (hex) => {
	const n = parseInt(hex.slice(1), 16);
	const [r, g, b] = [(n >> 16) & 255, (n >> 8) & 255, n & 255].map((v) => {
		const c = v / 255;
		return c <= 0.03928 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4;
	});
	return 0.2126 * r + 0.7152 * g + 0.0722 * b;
};
const ratio = (a, b) => {
	const [hi, lo] = [lum(a), lum(b)].sort((x, y) => y - x);
	return (hi + 0.05) / (lo + 0.05);
};

function readBlock(css, selector) {
	const start = css.indexOf(selector);
	if (start < 0) return {};
	const open = css.indexOf('{', start);
	const close = css.indexOf('}', open);
	const vars = {};
	for (const m of css.slice(open, close).matchAll(/--sl-color-([a-z0-9-]+):\s*(#[0-9a-fA-F]{6})/g)) vars[m[1]] = m[2];
	return vars;
}

// In Starlight: "black" is the page background, "white" is the strongest text, buttons put "black" text on "accent".
const pairs = [
	['body text', 'white', 'black', 4.5],
	['secondary text', 'gray-2', 'black', 4.5],
	['muted text', 'gray-3', 'black', 4.5],
	['muted text on sidebar', 'gray-3', 'gray-6', 4.5],
	['links', 'accent-high', 'black', 4.5],
	['links on sidebar', 'accent-high', 'gray-6', 4.5],
	['button text on accent', 'black', 'accent', 4.5],
];

let failures = 0;
for (const theme of themes) {
	const css = fs.readFileSync(path.join(root, 'src', 'styles', 'themes', `${theme}.css`), 'utf8');
	for (const [mode, selector] of [['dark', ':root {'], ['light', ":root[data-theme='light']"]]) {
		const v = readBlock(css, selector);
		console.log(`\n${theme} / ${mode}`);
		for (const [label, fg, bg, min] of pairs) {
			if (!v[fg] || !v[bg]) {
				console.log(`  ${label.padEnd(24)} missing ${fg} or ${bg}`);
				failures++;
				continue;
			}
			const r = ratio(v[fg], v[bg]);
			const ok = r >= min;
			if (!ok) failures++;
			console.log(`  ${label.padEnd(24)} ${r.toFixed(2).padStart(6)}  ${ok ? 'pass' : 'FAIL (needs ' + min + ')'}`);
		}
	}
}
console.log(failures ? `\n${failures} check(s) failed` : '\nAll contrast checks passed');
process.exit(failures ? 1 : 0);
