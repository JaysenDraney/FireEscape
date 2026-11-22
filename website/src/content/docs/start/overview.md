---
title: Overview
description: What FireEscape is, how it is organised, and where the project stands.
---

FireEscape is an open source launcher for Android TV and Google TV that puts you in control. You decide what fills your home screen, in what order, and when. The core handles focus, layout, settings and hosting plugins. Everything the user sees or does beyond that comes from a plugin class with a written contract.

:::caution[Design phase]
There is no code yet. These pages describe the design and are open to change.
:::

## The pieces

- **The core** draws the home screen, handles the remote, stores settings and talks to plugins. It has no features of its own beyond that.
- **Eleven plugin classes** cover how the screen looks, what appears on it, what can be opened, and what happens without anyone pressing a button. See [Plugin classes](/plugins/).
- **Four trust tiers** group the classes by the worst thing a plugin in that class can do. See [Trust tiers](/concepts/trust-tiers/).
- **Rows, priorities and schedules** decide which providers fill the rows of the home screen at any moment. See [Rows, priorities and schedules](/concepts/rows-and-schedules/).
- **Open ecosystem rules** keep the project open in practice. See [Open by design](/ecosystem/open-ecosystem/).

## Finding something to watch

Three plugin classes combine to answer "what should we watch?".

1. The user searches, or the home screen asks for a row. The launcher asks every enabled **catalog provider**.
2. Results are merged by shared ids. A **recommendation provider** reorders them using the user's ratings, says why, and reports how confident it is.
3. **Availability resolvers** add where each title can be watched.
4. The user picks a title and a way to watch. Only then does the launcher open the app or link.

## Where the project stands

The design is written and the repository is public. The next step is the core and the first three plugin classes. See the [roadmap](/project/roadmap/).

The original single-page design proposal is available as a [standalone page](/design/plugin-system-proposal.html).
