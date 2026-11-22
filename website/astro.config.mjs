// @ts-check
import { defineConfig } from 'astro/config';
import starlight from '@astrojs/starlight';

// Look and feel is chosen at build time: FE_THEME=ember|terminal|aurora.
const themes = ['ember', 'terminal', 'aurora', 'escape'];
const theme = process.env.FE_THEME ?? 'ember';
if (!themes.includes(theme)) {
	throw new Error(`FE_THEME must be one of ${themes.join(', ')} (got "${theme}")`);
}

// Local previews use "/". A GitHub Pages deploy sets FE_BASE=/FireEscape.
const base = process.env.FE_BASE ?? '/';
const basePrefix = base.replace(/\/$/, '');

// https://astro.build/config
export default defineConfig({
	site: process.env.FE_SITE ?? 'https://jaysendraney.github.io',
	base,
	outDir: process.env.FE_OUT ?? './dist',
	integrations: [
		starlight({
			title: 'FireEscape',
			description:
				'An open source launcher for Android TV and Google TV that puts you in control. No sponsored rows, no nudges.',
			social: [
				{ icon: 'github', label: 'GitHub', href: 'https://github.com/JaysenDraney/FireEscape' },
			],
			customCss: ['./src/styles/base.css', `./src/styles/themes/${theme}.css`],
			head: [{ tag: 'meta', attrs: { name: 'fe-theme', content: theme } }],
			sidebar: [
				{
					label: 'Start here',
					items: [
						{ label: 'Overview', slug: 'start/overview' },
						{ label: 'Roadmap and decisions', slug: 'project/roadmap' },
					],
				},
				{
					label: 'Concepts',
					items: [
						{ label: 'Trust tiers', slug: 'concepts/trust-tiers' },
						{ label: 'Rows, priorities and schedules', slug: 'concepts/rows-and-schedules' },
					],
				},
				{
					label: 'Plugin classes',
					items: [
						{ label: 'All classes', slug: 'plugins' },
						{ label: 'Wallpaper providers', slug: 'plugins/wallpaper-providers' },
						{ label: 'Recommendation providers', slug: 'plugins/recommendation-providers' },
					],
				},
				{
					label: 'Ecosystem',
					items: [{ label: 'Open by design', slug: 'ecosystem/open-ecosystem' }],
				},
				{
					label: 'Project',
					items: [
						{ label: 'What we are not building', slug: 'project/non-goals' },
						{
							label: 'Full design proposal',
							link: `${basePrefix}/design/plugin-system-proposal.html`,
							attrs: { target: '_blank' },
						},
					],
				},
			],
		}),
	],
});
