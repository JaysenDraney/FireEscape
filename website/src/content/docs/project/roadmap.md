---
title: Roadmap and decisions
description: The build order, what has been decided, and what is still open.
---

## Phases

| Phase | Delivers | Reason |
| --- | --- | --- |
| 0 | Core, plugin host, permission screens, one default index | Everything else depends on it. |
| 1 | Theme and layout (with row slots), Wallpaper providers, Launch sources | Launch sources are needed for the launcher to be usable, since installed apps are one. Theme and wallpaper are the lowest-risk visible features. |
| 2 | Widgets, Idle providers, Content rows, Catalog providers, Availability resolvers, local Recommendation providers, row bindings with priority schedules and confidence | Local providers need no trust machinery beyond data levels. Bindings with priorities are the core of the home screen. |
| 3 | Recommendation providers that use an external service, Automation, Profiles and policy | External providers need the trusted-publisher list, data levels and the data log. Automation and Profiles are the highest privilege. |

## Decided

- **Name:** FireEscape.
- **License:** Apache-2.0 for the whole project, including the core, the SDK and the sample plugins. It includes a patent grant. It also permits closed-source forks, so the openness rules bind the spec and the default index, not forks.
- **Do not copy code from GPL projects.** Apache-2.0 code cannot absorb GPL-3.0 code. Other launchers are references only.
- **Documentation:** Astro Starlight.

## Still open

| Decision | Current leaning |
| --- | --- |
| Plugin form | Separate APKs for Tiers 1 to 3, data packs for Tier 0. |
| Compatibility with existing wallpaper plugins | Undecided. Needs a read of that interface and the license of its sample project. |
| Kids and profiles | Launcher-level first, documented as non-enforcing. |
| Manual override length | Until the next schedule change. |
| Confidence scale and floor | 0 to 1, floor per binding, default off. Priority always outranks confidence. |
| Hero source | Its own binding, so it can use a different provider and schedule. |
| Trusted publisher identity | Key fingerprint, with name and source URL shown. Domain verification later. |
| Default data source | Ship none. Document TMDB setup. Re-read its terms before any bundled use. |

The full list, with options and reasoning, is in the [design proposal](/design/plugin-system-proposal.html).
