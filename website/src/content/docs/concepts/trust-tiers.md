---
title: Trust tiers
description: "Four tiers that answer one question about each plugin class: what is the worst it can do to the person using the TV?"
---

Every plugin class belongs to one of four tiers. A tier answers one question: what is the worst a plugin in this class can do to the person using the TV?

| Tier | In plain terms | What the user is asked | Worst case if a plugin is bad or buggy | Classes |
| --- | --- | --- | --- | --- |
| **0 Declarative** | A data file. No code runs. | Nothing beyond choosing it. | An ugly or unreadable screen. Switching packs fixes it. | Theme and layout |
| **1 Presentation** | Code runs and supplies visuals. It cannot start apps or restrict anyone. | The network use the plugin declares. It is shown, not enforced. | A wrong, broken or distracting picture or widget. It could contact servers you did not expect. | Wallpaper, Widgets, Idle |
| **2 Content and targets** | Supplies things you can open or choose from. Opening anything still needs you to select it. | Declared network use, the installed-app list, your own API key, and for recommendation providers a data level. Providers that use an external service also need a trusted publisher. | Misleading titles or links. A leak of ratings or history if a provider with network access receives them. | Content rows, Launch sources, Catalog providers, Availability resolvers, Recommendation providers |
| **3 Control** | Can act without you pressing anything, or limit what someone sees. | A separate approval for each plugin, with a plain-language warning. System-level grants where Android requires them. | Opening apps or switching profiles unexpectedly, hiding apps, locking people out. Every action is logged and listed in settings. | Automation, Profiles and policy |

**Example.** A wallpaper plugin (Tier 1) can fetch images from the internet but cannot see which apps you have. An automation plugin (Tier 3) can open an app at boot, so you approve it on its own and every action it takes is logged.

## How tiers are applied

- A tier belongs to the class, not to the individual plugin. A plugin cannot claim a lower tier than its class.
- The install screen shows the tier first, then the specific permissions the plugin asks for.
- Review for the default index gets stricter as the tier rises: a schema check for Tier 0, up to a source review for Tier 3.

## What the launcher can and cannot enforce

The launcher controls what it hands a plugin: the installed-app list, ratings, history, the schedule. It can withdraw those later. It cannot take back a network permission that an installed plugin already has, and it cannot see what a plugin sends. For network use the declaration is a disclosure, and trust in the publisher is the control. See [External services and trusted publishers](/ecosystem/open-ecosystem/#external-services-and-trusted-publishers).
