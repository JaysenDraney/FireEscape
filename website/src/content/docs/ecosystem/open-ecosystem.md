---
title: Open by design
description: The rules that keep the ecosystem open in practice, and how external services and trusted publishers work.
---

Each rule below exists to block a specific way an ecosystem becomes closed in practice.

| Rule | What it means | Failure it prevents |
| --- | --- | --- |
| Public spec per class | Each class has a written contract and a versioned schema in the repository. | Plugins that depend on unpublished behavior. |
| No gated capabilities | The core never withholds a class, capability or setting behind payment or an account. | A paid tier that controls who can use plugins. |
| Any APK can be a plugin | Plugins install from any source. The index is a convenience, not a requirement. | Distribution through a single store. |
| Many indexes | An index is a signed JSON file anyone can host. The launcher ships with one default and lets users add more. | One index acting as a chokepoint. |
| Source required for the default index | The default index lists only plugins with an OSI-approved license and a source link. Other indexes can list anything. | Closed plugins in the default listing. |
| Stable contracts | Semantic versions per class. Two major versions supported at once, with 12 months notice before removal. | API churn breaking plugins that are still used. |
| No telemetry in core | Plugins that show ads or collect usage data are not eligible for the default index. Network hosts a plugin declares are shown at install. The launcher cannot verify them. | Tracking and ad injection. |
| Credit travels with data | Items can carry required credit and license text, and the launcher displays it. | Quietly breaking the terms of a data source. |
| User-supplied keys | Plugins that use keyed services use the user's own key. The project ships no shared keys. | The ecosystem depending on one account that can be revoked. |
| Reference plugin and tests per class | Every class ships a working open plugin and a conformance test kit. | Lock-in: another launcher can adopt the same spec and run the same plugins. |
| Open governance | Spec changes go through public proposals in the repository. Maintainers are listed. | Decisions made privately by one party. |

## External services and trusted publishers

A recommendation provider can use an external service, for example a model run by its publisher. The provider then handles the user's ratings on someone else's server. This is the riskiest use of a Tier 2 class, so it has its own rules.

**What the launcher can and cannot enforce**

- **Can:** decide whether to ask a provider at all, and what data to hand over.
- **Cannot:** see or block what a plugin does with data it has received, or which servers it contacts. Android lets an app with the network permission reach any host. Declared hosts are disclosure, not enforcement.

The real control is whom the user trusts. These rules limit how much a trusted publisher is handed.

- **Local by default.** A provider with no external service declaration is local. External providers are marked "External service" wherever they appear.
- **Trusted publishers.** An external provider installs only if its publisher is on the user's trusted list. A publisher is identified by a signing-key fingerprint, a name and a source URL. Adding one takes a deliberate screen that names the publisher and what it could receive. Removing a publisher disables its providers.
- **Disclosure at install.** The declared hosts and a link to the publisher's privacy statement are shown. The launcher does not verify either.
- **Data log.** Settings lists each time data was handed to a provider: which provider, which data level, and when.
- **Unused means untouched.** A binding that falls below the last row is not asked, so it receives nothing.
- **Your own service.** If you run the service yourself, you are the publisher. You add your own key to the trusted list.
- **Default index.** External providers are listed only with a source link and a privacy statement, and flagged as external. Other indexes can list anything.

## Limits of openness

- Open source does not prevent a malicious plugin. Permissions, tiers and review for the default index reduce the risk. They do not remove it.
- The launcher cannot stop a user from adding a third-party index that lists plugins we would exclude.
- Profiles and policy plugins are not a security boundary. A launcher can hide and lock what its own screen shows. It cannot stop someone from opening Settings, installing a different launcher, or using ADB.
