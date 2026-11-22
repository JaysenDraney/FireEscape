// Builds each look into its own folder (dist-ember, dist-terminal, dist-aurora) so they can be compared side by side.
import { spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const all = ['ember', 'terminal', 'aurora', 'escape'];
const requested = process.argv.slice(2);
const themes = requested.length ? requested : all;

for (const theme of themes) {
	if (!all.includes(theme)) {
		console.error(`Unknown theme "${theme}". Choose from: ${all.join(', ')}`);
		process.exit(1);
	}
	console.log(`\n=== building ${theme} ===`);
	const result = spawnSync('npx astro build', {
		cwd: root,
		stdio: 'inherit',
		shell: true,
		env: { ...process.env, FE_THEME: theme, FE_OUT: `./dist-${theme}` },
	});
	if (result.status !== 0) {
		console.error(`Build failed for ${theme}`);
		process.exit(result.status ?? 1);
	}
}
console.log('\nAll requested looks built.');
