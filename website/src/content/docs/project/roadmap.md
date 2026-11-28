---
title: Roadmap and decisions
description: The build order by phase and milestone, the acceptance criteria for each milestone, what has been decided, and what is still open.
---

## Phases

| Phase | Delivers | Reason |
| --- | --- | --- |
| 0 | Core, plugin host, permission screens, one default index | Everything else depends on it. |
| 1 | Theme and layout (with row slots), Wallpaper providers, Launch sources | Launch sources are needed for the launcher to be usable, since installed apps are one. Theme and wallpaper are the lowest-risk visible features. |
| 2 | Widgets, Idle providers, Content rows, Catalog providers, Availability resolvers, local Recommendation providers, row bindings with priority schedules and confidence | Local providers need no trust machinery beyond data levels. Bindings with priorities are the core of the home screen. |
| 3 | Recommendation providers that use an external service, Automation, Profiles and policy | External providers need the trusted-publisher list, data levels and the data log. Automation and Profiles are the highest privilege. |

## Milestones

Each phase is split into milestones. A milestone is small enough to review in a few pull requests and has its own acceptance criteria, listed in the [deep dive](#milestone-deep-dive) below. Milestones inside a phase can overlap unless one lists another as a dependency.

| Milestone | Delivers | Status |
| --- | --- | --- |
| **Phase 0: Foundation** | | |
| [M0.1](#m01-project-scaffold-and-test-harness) | Project scaffold and test harness | Done |
| [M0.2](#m02-decision-plugin-form-and-transport) | Decision: plugin form and transport | Done |
| [M0.3](#m03-launcher-shell) | Launcher shell: home screen, focus engine, settings store | Done |
| [M0.4](#m04-emulator-test-layer-in-ci) | Emulator test layer in CI | Done |
| [M0.5](#m05-plugin-host-and-api-v1-skeleton) | Plugin host and plugin API v1 skeleton | Done |
| [M0.6](#m06-permission-screens) | Permission screens | Done |
| [M0.7](#m07-default-plugin-index) | Default plugin index | Done |
| [M0.8](#m08-conformance-kit-and-mock-host) | Conformance kit and mock host | Done |
| **Phase 1: A usable launcher** | | |
| [M1.1](#m11-installed-apps) | Installed apps as the built-in launch source | Not started |
| [M1.2](#m12-launch-sources-api) | Launch sources API and reference plugin | Not started |
| [M1.3](#m13-theme-and-layout-packs) | Theme and layout packs (Tier 0) | Not started |
| [M1.4](#m14-wallpaper-providers) | Wallpaper providers (Tier 1) | Not started |
| [M1.5](#m15-first-alpha-release) | First alpha release | Not started |
| **Phase 2: The home screen** | | |
| [M2.1](#m21-row-bindings-and-priority-schedules) | Row bindings and priority schedules | Not started |
| [M2.2](#m22-row-setup-with-the-remote) | Row setup with the remote, manual override, time scrubber | Not started |
| [M2.3](#m23-content-rows) | Content rows (Tier 2) | Not started |
| [M2.4](#m24-catalog-providers-and-search) | Catalog providers and search (Tier 2) | Not started |
| [M2.5](#m25-availability-resolvers) | Availability resolvers (Tier 2) | Not started |
| [M2.6](#m26-local-recommendation-providers-and-hero-slot) | Local recommendation providers, data levels, hero slot | Not started |
| [M2.7](#m27-widgets) | Widgets (Tier 1) | Not started |
| [M2.8](#m28-idle-providers) | Idle providers (Tier 1) | Not started |
| [M2.9](#m29-setup-export-and-import) | Setup export and import | Not started |
| **Phase 3: External services and control** | | |
| [M3.1](#m31-trusted-publishers-and-data-log) | Trusted publishers and the data log | Not started |
| [M3.2](#m32-external-recommendation-providers) | Recommendation providers that use an external service | Not started |
| [M3.3](#m33-automation) | Automation (Tier 3) | Not started |
| [M3.4](#m34-profiles-and-policy) | Profiles and policy (Tier 3) | Not started |

## How a milestone is accepted

### Test layers

Every milestone lists its tests by layer. The layers are set in [ADR 0002](https://github.com/JaysenDraney/FireEscape/blob/main/docs/adr/0002-testing-layers.md).

| Layer | What it is | Needs a device? |
| --- | --- | --- |
| **L1 Unit** | JUnit tests of plain Kotlin logic | No |
| **L2 Focus** | Compose tests that press D-pad keys and check which element has focus | No |
| **L3 Screenshot** | Roborazzi images at 1920x1080, reviewed in the pull request comment ([ADR 0003](https://github.com/JaysenDraney/FireEscape/blob/main/docs/adr/0003-screenshot-review-ci.md)) | No |
| **L4 Emulator** | Instrumented tests on an Android TV virtual device, for anything that crosses an app boundary | Emulator only |
| **Manual** | A check by a person on a real TV device, used only where the emulator cannot answer | Yes |

### Definition of done for every milestone

A milestone is done when all of these hold, in addition to its own criteria:

1. Its tests are merged and pass in CI on `main`.
2. Every screen it adds or changes has an L3 screenshot, and the images were reviewed in the pull request comment.
3. Every screen it adds can be used with only the D-pad, Select, long-press Select and Back, and an L2 test shows it.
4. Any decision it needed is recorded as an ADR in `docs/adr`.
5. The docs site and the design proposal describe what was built, and they agree with each other.
6. Any plugin class it adds has a reference plugin and passes the conformance kit (from M0.8 onward).
7. The owner has signed off on the milestone's pull request or tracking issue.

## Milestone deep dive

Numbers that the design has not fixed yet, such as timeouts and limits, are marked "set in the spec". Each is decided in the milestone's own work and recorded there.

### Phase 0: Foundation

#### M0.1 Project scaffold and test harness

**Status: Done** (pull requests #2 and #4).

**Delivers:** the Gradle project, a toolchain pinned with mise, a placeholder Compose for TV home screen, and CI that compares screenshots and posts the diff on each pull request.

**Acceptance criteria**

- [x] `mise install` and one Gradle command build the project and run all tests on a clean Windows machine.
- [x] The same tests pass in GitHub Actions on every pull request.
- [x] A pull request that changes the UI gets a comment with reference, diff and new images.
- [x] The comment's companion branch is deleted when the pull request closes.

**Tests:** L1 row resolver tests, L2 D-pad focus test, L3 home screen screenshot, all running in CI.

#### M0.2 Decision: plugin form and transport

**Status: Done** (pull request #6).

**Delivers:** [ADR 0004](https://github.com/JaysenDraney/FireEscape/blob/main/docs/adr/0004-plugin-form-and-transport.md), which settles the plugin form: separate APKs for Tiers 1 to 3, each exposing a bound service with a versioned AIDL interface per class, and data packs for Tier 0.

**Depends on:** nothing. M0.5 to M0.8 depend on it.

**Acceptance criteria**

- [x] A spike shows a launcher binding to a service in a second APK on the TV emulator, calling it and surviving that APK's crash.
- [x] The ADR compares the options, records the choice and is approved by the owner.
- [x] The design proposal's section 6 and this page are updated to match.

**Tests:** the spike's L4 test is kept and becomes the first test of M0.5.

#### M0.3 Launcher shell

**Status: Done** (pull request #8).

**Delivers:** an app that can be the TV's home screen, a focus engine that rows and plugins build on, and a settings store.

**Acceptance criteria**

- [x] The launcher can be chosen as the home app on the TV emulator, and pressing Home returns to it.
- [x] Back on the home screen never leaves the launcher.
- [x] From any focusable element, every D-pad press either moves focus to another element or stays put. Focus is never lost.
- [x] Focus returns to the last focused element when the user comes back to the home screen.
- [x] Settings survive the app being killed and restarted.
- [x] The core contains no plugin-class features of its own.

**Tests**

- L1: settings read and write, including a corrupt settings file falling back to defaults.
- L2: D-pad traversal at every edge of a grid of rows, and focus restore.
- L3: the empty home screen, and one element in its focused state.
- L4: set as home, press Home, check the launcher is in front. Kill the process and check settings are kept.

On the Google TV emulator image the system launcher keeps HOME while its process is alive, whatever default is selected, so the L4 test disables and stops it for the run and restores it afterwards. Real-device HOME behavior is checked in M1.5.

#### M0.4 Emulator test layer in CI

**Status: Done** (pull requests #9 and #10).

**Delivers:** L4 tests running on an Android TV virtual device in GitHub Actions. ADR 0002 left it unverified whether this can run headless in CI; [ADR 0005](https://github.com/JaysenDraney/FireEscape/blob/main/docs/adr/0005-emulator-ci.md) answers it and records the measurements and the every-PR cadence decision.

**Acceptance criteria**

- [x] An instrumented test runs on an Android TV system image in GitHub Actions and reports pass or fail.
- [x] The run time and how often it fails for no code reason are measured over at least 10 runs and written down.
- [x] A decision on running it on every pull request or nightly is recorded in an ADR.

**Tests:** a single L4 smoke test that starts the launcher and checks it shows the home screen.

#### M0.5 Plugin host and API v1 skeleton

**Status: Done** (pull request #11).

**Delivers:** the part of the core that finds, binds and talks to plugins, and the first version of the plugin API.

**Depends on:** M0.2.

**Acceptance criteria**

- [x] The host finds a plugin by its per-class intent action and reads its declared API version, capabilities and source URL.
- [x] A plugin that declares an API major version the launcher does not support is listed as incompatible and never bound.
- [x] A plugin that crashes, hangs or answers late does not freeze or crash the launcher. Calls time out after a limit set in the spec.
- [x] The host unbinds plugins it is not using.
- [x] The API is published in the repository with a version number.

**Tests**

- L1: metadata parsing against valid, missing and malformed fixtures. Version compatibility rules.
- L4: install a sample plugin as a separate APK. Bind and call it. Kill its process and check the launcher stays responsive. Make it hang and check the call times out.

#### M0.6 Permission screens

**Status: Done** (pull request #12).

**Delivers:** the screens where a user sees and approves what a plugin can do.

**Depends on:** M0.5.

**Acceptance criteria**

- [x] The screen shows the tier first, then each capability the plugin declares.
- [x] A plugin is placed in the tier of its class. It cannot claim a lower one.
- [x] Tier 3 plugins need a separate approval with a plain-language warning.
- [x] Declared network use is labeled as shown, not enforced.
- [x] Withdrawing a grant stops the launcher handing that data to the plugin from the next call on.

**Tests**

- L1: class-to-tier mapping, and that a plugin's declaration cannot lower its tier.
- L2: each screen can be completed with the D-pad and Select only.
- L3: the permission screen for one plugin of each tier.
- L4: grant the installed-app list, check the plugin receives it. Withdraw it and check the next call does not include it.

#### M0.7 Default plugin index

**Status: Done** (pull request #13).

**Delivers:** the signed JSON index format, its schema, and one default index the launcher ships with.

**Depends on:** M0.5.

**Acceptance criteria**

- [x] The index JSON Schema is published in the repository.
- [x] The launcher rejects an index whose signature does not verify.
- [x] The launcher rejects a plugin update signed with a different key than the installed version.
- [x] A user can add a second index by URL and remove it.
- [x] The default index lists only plugins with an OSI-approved license and a source link.

**Tests**

- L1: schema validation and signature checks against fixtures: a valid index, a tampered one, and one signed with the wrong key.
- L4: install a plugin from a test index on the emulator, then try an update signed with another key and check it is refused.

#### M0.8 Conformance kit and mock host

**Status: Done** (pull request #14).

**Delivers:** the test kit that checks a plugin against its class contract, and a mock host that runs it without the launcher.

**Depends on:** M0.5.

**Acceptance criteria**

- [x] The kit runs against a plugin APK and reports pass or fail per contract rule.
- [x] A deliberately broken sample plugin fails with a message that names the broken rule.
- [x] Each reference plugin's CI runs the kit.

**Tests:** the kit's own L1 tests, and L4 runs against a good and a broken sample plugin.

### Phase 1: A usable launcher

#### M1.1 Installed apps

**Delivers:** installed apps on the home screen, as the built-in launch source.

**Depends on:** M0.3.

**Acceptance criteria**

- [ ] Every app with a TV launcher entry appears, with its banner or icon.
- [ ] Selecting an app opens it. Back from the app returns to the launcher.
- [ ] Installing or removing an app updates the row without a restart.

**Tests**

- L1: app list sorting and filtering.
- L2: D-pad through a long app row.
- L3: an app row with banners, and with an app that has no banner.
- L4: install a test app and check it appears. Open it. Uninstall it and check it disappears.

#### M1.2 Launch sources API

**Delivers:** the Launch sources class contract and a reference plugin, for example HDMI inputs or links.

**Depends on:** M0.5, M0.8, M1.1.

**Acceptance criteria**

- [ ] The class contract is published, with a version.
- [ ] The reference plugin passes the conformance kit.
- [ ] Items from a plugin open only when the user selects them.

**Tests:** L1 contract parsing, L3 a row of plugin items, L4 the reference plugin installed and an item opened.

#### M1.3 Theme and layout packs

**Delivers:** Tier 0 packs: colors, type, icon sets and row slot layouts as JSON with images.

**Depends on:** M0.3.

**Acceptance criteria**

- [ ] The pack JSON Schema is published.
- [ ] A pack that fails the schema, or contains executable content, is rejected with a clear message. The launcher keeps the current theme.
- [ ] Switching packs takes effect without a restart.
- [ ] Text in every bundled pack meets a contrast ratio set in the spec.

**Tests**

- L1: schema validation against valid and invalid fixtures, including a pack with executable content.
- L3: the home screen in every bundled pack.
- L4: install a pack from a file and switch to it.

#### M1.4 Wallpaper providers

**Delivers:** the Wallpaper providers class, a reference plugin, and video backgrounds played with Media3.

**Depends on:** M0.5, M0.6, M0.8. Waits on the open decision about compatibility with existing wallpaper plugins.

**Acceptance criteria**

- [ ] The class contract is published.
- [ ] Still and video wallpapers both work.
- [ ] If the provider fails or returns nothing, the launcher falls back to the theme background.
- [ ] Declared network use is shown on the permission screen.
- [ ] The reference plugin passes the conformance kit.

**Tests:** L1 provider response parsing, L3 home screen with a still wallpaper and with the fallback, L4 reference plugin installed, then killed, and the fallback shown.

#### M1.5 First alpha release

**Delivers:** a signed alpha APK that someone can install and use as their home screen.

**Depends on:** M1.1 to M1.4.

**Acceptance criteria**

- [ ] A release build is produced by CI and signed. The signing key is not in the repository.
- [ ] Install and setup instructions are on the docs site.
- [ ] The launcher has been used as the home screen on at least one real Google TV device for a week without a crash or a lost-focus bug.

**Tests:** the full L1 to L4 suite on the release build, and the manual week on a device.

### Phase 2: The home screen

#### M2.1 Row bindings and priority schedules

**Delivers:** the rules in [Rows, priorities and schedules](/concepts/rows-and-schedules/), built into the core.

**Depends on:** M0.3, M1.3 (row slots).

**Acceptance criteria**

- [ ] Rows fill from bindings in priority order.
- [ ] Bindings below the last row are not asked for anything.
- [ ] Confidence only skips rows below a floor, breaks ties and orders titles. It never outranks priority.
- [ ] Schedule rules switch priority at the right time, including at midnight and across clock changes for daylight saving.
- [ ] Rows far below the screen are asked only when scrolled near.

**Tests**

- L1: the three-provider example on the concepts page as a test table. Midnight and daylight-saving edge cases. A provider that claims confidence 1.0 at a lower priority.
- L1: a mock provider below the last row records zero calls.
- L3: the home screen at three times of day from the example.

#### M2.2 Row setup with the remote

**Delivers:** long-press row editing, the manual override picker and the time scrubber.

**Depends on:** M2.1. Waits on the open decision on manual override length.

**Acceptance criteria**

- [ ] Every setting from the concepts page can be changed with the D-pad, Select, long-press Select and Back.
- [ ] A manual override lasts as long as the decision on override length says.
- [ ] The time scrubber shows which bindings take the rows at the chosen time, and it matches what M2.1 computes.

**Tests:** L1 override expiry, L2 every editing screen with D-pad keys only, L3 each editing screen and the scrubber at two times.

#### M2.3 Content rows

**Delivers:** the Content rows class and a reference plugin.

**Depends on:** M0.8, M2.1.

**Acceptance criteria**

- [ ] The class contract is published and the reference plugin passes the conformance kit.
- [ ] Cards open something only when selected.
- [ ] Required credit text on an item is displayed.

**Tests:** L1 contract parsing, L3 a row of cards with credit text, L4 reference plugin bound to a row.

#### M2.4 Catalog providers and search

**Delivers:** the Catalog providers class, browsing and search, and a reference plugin. The default data source is an open decision. The current leaning is to ship none and document TMDB setup.

**Depends on:** M0.8.

**Acceptance criteria**

- [ ] The class contract is published and the reference plugin passes the conformance kit.
- [ ] Search can be done with the remote and gives results from every enabled catalog provider.
- [ ] A provider that needs a key uses the user's own key. The project ships no shared keys.

**Tests:** L1 result merging, L2 search with the D-pad on-screen keyboard, L3 search results, L4 reference plugin.

#### M2.5 Availability resolvers

**Delivers:** the Availability resolvers class: where a title can be watched and how to open it. Where the deep-link mappings live is an open decision. The current leaning is a public repository.

**Depends on:** M2.4.

**Acceptance criteria**

- [ ] Selecting "watch" on a title opens the right app at the title when a mapping exists, and the app's home otherwise.
- [ ] The launcher never plays protected content itself.

**Tests:** L1 deep-link template filling, L4 open a test app from a resolved link.

#### M2.6 Local recommendation providers and hero slot

**Delivers:** the Recommendation providers class for local providers, data levels, and the hero slot. Hero source and confidence scale are open decisions.

**Depends on:** M2.1, M0.8.

**Acceptance criteria**

- [ ] The class contract is published and the reference plugin passes the conformance kit.
- [ ] A provider receives only the data level the user granted.
- [ ] Each proposal shows its reason.
- [ ] The hero slot shows a title with a backdrop and tagline, or falls back cleanly when the top title has none.

**Tests:** L1 data-level filtering, L3 a recommendation row and the hero slot with and without a backdrop, L4 reference provider receiving only granted data.

#### M2.7 Widgets

**Delivers:** the Widgets class for small live info in header slots, and a reference plugin such as a clock or weather.

**Depends on:** M0.8, M1.3.

**Acceptance criteria**

- [ ] The class contract is published and the reference plugin passes the conformance kit.
- [ ] A widget cannot take focus away from the rows.
- [ ] A widget that stops updating shows its last value or hides. It never blocks the home screen.

**Tests:** L2 focus is never taken by a widget, L3 header with widgets, L4 reference widget killed mid-update.

#### M2.8 Idle providers

**Delivers:** the Idle providers class for full-screen content while the TV is idle, and a reference plugin.

**Depends on:** M0.8.

**Acceptance criteria**

- [ ] The class contract is published and the reference plugin passes the conformance kit.
- [ ] Any remote button leaves idle and returns focus to where it was.

**Tests:** L1 idle timer, L2 focus restore after idle, L4 reference idle provider.

#### M2.9 Setup export and import

**Delivers:** the whole setup as a file, so it can be edited on a PC or phone.

**Depends on:** M2.1, M2.2.

**Acceptance criteria**

- [ ] Export then import gives back the same setup.
- [ ] An invalid file is rejected with a message and leaves the current setup unchanged.
- [ ] The file format has a published schema.

**Tests:** L1 round trip and invalid-file fixtures, L4 export, wipe settings, import.

### Phase 3: External services and control

#### M3.1 Trusted publishers and data log

**Delivers:** the trusted publisher list and the data log described in [Open by design](/ecosystem/open-ecosystem/#external-services-and-trusted-publishers). How a publisher is identified is an open decision. The current leaning is a key fingerprint with name and source URL.

**Depends on:** M0.6, M0.7.

**Acceptance criteria**

- [ ] Adding a publisher takes a deliberate screen that names it and what it could receive.
- [ ] Removing a publisher disables its providers.
- [ ] The data log lists every handover: provider, data level and time.

**Tests:** L1 log entries for each handover, L2 the add-publisher screen, L3 the publisher and log screens, L4 remove a publisher and check its provider is disabled.

#### M3.2 External recommendation providers

**Delivers:** recommendation providers that use an external service. The default external data level is an open decision. The current leaning is candidate pool only.

**Depends on:** M2.6, M3.1.

**Acceptance criteria**

- [ ] An external provider installs only if its publisher is trusted.
- [ ] It is marked "External service" wherever it appears.
- [ ] A binding below the last row hands it nothing, shown by the data log.

**Tests:** L1 install refused for an untrusted publisher, L3 the external marking, L4 a provider below the last row with an empty data log.

#### M3.3 Automation

**Delivers:** the Automation class for event-triggered actions, with its own approval and log.

**Depends on:** M0.6, M0.8.

**Acceptance criteria**

- [ ] Each automation plugin needs its own approval with a plain-language warning.
- [ ] Every action it takes is logged and listed in settings.
- [ ] Withdrawing approval stops it at once.

**Tests:** L1 action logging, L3 the approval screen, L4 an automation that opens an app at boot, then is withdrawn.

#### M3.4 Profiles and policy

**Delivers:** the Profiles and policy class. Kids and profiles is an open decision. The current leaning is launcher-level first.

**Depends on:** M3.3.

**Acceptance criteria**

- [ ] Hidden apps and locked items do not appear on the launcher's screens for that profile.
- [ ] The settings screen states plainly that profiles are not a security boundary.
- [ ] A PIN is needed to leave a locked profile.

**Tests:** L1 visibility rules, L2 PIN entry with the D-pad, L3 the profile switcher and the warning, L4 switch profile and check hidden apps are absent.

## Decided

- **Name:** FireEscape.
- **License:** Apache-2.0 for the whole project, including the core, the SDK and the sample plugins. It includes a patent grant. It also permits closed-source forks, so the openness rules bind the spec and the default index, not forks.
- **Do not copy code from GPL projects.** Apache-2.0 code cannot absorb GPL-3.0 code. Other launchers are references only.
- **Plugin form and transport:** separate APKs for Tiers 1 to 3, one bound service per class with a versioned AIDL interface, found by a per-class intent action. Tier 0 stays declarative data packs. [ADR 0004](https://github.com/JaysenDraney/FireEscape/blob/main/docs/adr/0004-plugin-form-and-transport.md).
- **Documentation:** Astro Starlight.

## Still open

| Decision | Current leaning |
| --- | --- |
| Compatibility with existing wallpaper plugins | Undecided. Needs a read of that interface and the license of its sample project. |
| Kids and profiles | Launcher-level first, documented as non-enforcing. |
| Manual override length | Until the next schedule change. |
| Confidence scale and floor | 0 to 1, floor per binding, default off. Priority always outranks confidence. |
| Hero source | Its own binding, so it can use a different provider and schedule. |
| Trusted publisher identity | Key fingerprint, with name and source URL shown. Domain verification later. |
| Default data source | Ship none. Document TMDB setup. Re-read its terms before any bundled use. |

The full list, with options and reasoning, is in the [design proposal](/design/plugin-system-proposal.html).
