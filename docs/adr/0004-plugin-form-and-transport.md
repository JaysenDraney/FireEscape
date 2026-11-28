# 0004. Plugin form and transport

Status: Accepted. Approved by the owner (JaysenDraney) on 2026-11-27 in PR #6 (roadmap milestone M0.2).

## Context

The plugin form is listed as an open decision in section 6 of the design proposal. The current leaning is separate APKs for Tiers 1 to 3, and data packs for Tier 0. Appendix A sketches the transport: each plugin exposes a bound service with a versioned AIDL interface per class, found by a per-class intent action.

The plugin host (M0.5) and everything after it depend on this choice. The design requires that a buggy plugin cannot take the launcher down, that plugins can come from any source, and that each class has a stable, versioned contract.

## Options

| Option | How it works | Isolation | For plugin authors | Notes |
| --- | --- | --- | --- | --- |
| **A. Separate APK, bound service with AIDL** | Each plugin is an app. It exports one service per class. The launcher finds it by intent action and binds to it. | Own process. A crash or hang stays in the plugin. | Android project in Kotlin or Java. One install per plugin. | The current leaning. Tested in the spike below. |
| **B. Separate APK, content provider and intents** | The plugin exposes data through a content provider. The launcher queries it. | Own process. | Android project. Simpler for read-only data. | Suits data-only classes. Awkward for callbacks and streams. Could be added for one class later without changing A. |
| **C. Plugin APK code loaded into the launcher** | The launcher loads the plugin's classes into its own process. | None. A plugin crash crashes the launcher. | Android project. | Google advises avoiding dynamic code loading unless needed ([Android docs](https://developer.android.com/privacy-and-security/risks/dynamic-code-loading)). Apps targeting Android 14 must mark dynamically loaded files read-only ([summary](https://scrapbox.io/android-wagashi/Safer_dynamic_code_loading), not an official source). Breaks the isolation the trust tiers assume. |
| **D. Scripts in an embedded interpreter** | Plugins are scripts, for example JavaScript, run inside the launcher. | Same process. Only as strong as the sandbox. | No Android build. Light install. | Google Play's policy exempts interpreted code with indirect access to Android APIs from its ban on downloading executable code ([policy text as quoted on Google's support forum](https://support.google.com/googleplay/android-developer/thread/264893635/can-the-app-provide-the-download-link-to-app-that-is-from-the-same-company-but-not-ps-compliant?hl=en)). We would have to build and maintain the sandbox and the API bridge. |

Tier 0 stays declarative in every option: JSON validated against JSON Schema, with images and no executable content.

## Spike

A spike on branch `feat/m0.2-plugin-transport-spike` tests option A end to end:

- `:plugin-api`: an Android library holding a spike AIDL interface, `ISpikePlugin`.
- `:spike-plugin`: a separate APK, `org.fireescape.spike.plugin`, that exports the service with intent action `org.fireescape.plugin.SPIKE` and the `fireescape.api`, `fireescape.capabilities` and `fireescape.source` metadata from Appendix A.
- `:launcher`: `SpikePluginClient`, a minimal host, and the L4 test `SpikeTransportTest`.

Run with `gradlew :spike-plugin:installDebug :launcher:connectedDebugAndroidTest` on an emulator.

### Results

Google TV system image, API 36, x86_64, on the Android Emulator with WHPX acceleration on Windows. Run on 2026-11-23.

| Test | What it checks | Result |
| --- | --- | --- |
| `discoversBindsAndCallsPluginInAnotherApk` | The launcher finds the plugin by intent action, reads `fireescape.api = spike/1`, binds, and calls it. The answer comes from a different process. | Pass, 0.06 s |
| `launcherSurvivesPluginCrashAndReconnects` | The plugin kills its own process. The launcher gets `onServiceDisconnected`, keeps running, and is reconnected when the system restarts the plugin. The new plugin process has a new PID. | Pass, 1.25 s |
| `hungPluginCallTimesOutWithoutBlockingCaller` | The plugin sleeps 5 s before answering. The launcher's call returns "timed out" after 500 ms and the connection stays usable. | Pass, 0.56 s |

The spike is one run on one emulator image. It has not been run on a real TV device or in CI.

### Findings

1. **Package visibility.** Apps targeting Android 11 or later see only some other apps by default ([Android docs](https://developer.android.com/training/package-visibility/declaring)). The launcher must declare a `<queries>` entry for each plugin intent action, or discovery finds nothing. This fits the per-class action design. Each new class adds one entry.
2. **Reserved AIDL name.** The AIDL compiler rejects a method named `getInterfaceVersion()` as reserved for internal use. Our version method must use another name. The spike uses `getPluginApiVersion()`.
3. **Calls cannot be cancelled.** A Binder call that times out keeps running in the plugin, and it keeps the launcher's worker thread waiting until the plugin answers or dies. The host needs a per-plugin limit on calls in flight, set in the spec in M0.5.
4. **Crash recovery is automatic.** With `BIND_AUTO_CREATE`, the system restarted the crashed plugin and reconnected without launcher code. `ServiceConnection` also reports when a binding is dead and must be rebound, for example after an update ([Android docs](https://developer.android.com/reference/android/content/ServiceConnection)).
5. **Versioning is ours to enforce.** Android's stable AIDL tooling, which checks backward compatibility, is part of the platform build system ([AOSP docs](https://source.android.com/docs/core/architecture/aidl/stable-aidl)). I have not found an equivalent for app Gradle builds, so this is unverified. The plan is our own rules: methods are only added at the end of an interface, a version method answers first, and the major version is in the metadata so the host can refuse a plugin before binding.

## Decision

Option A for Tiers 1 to 3: each plugin is a separate APK that exports one bound service per class, with a versioned AIDL interface per class, found by a per-class intent action `org.fireescape.plugin.<CLASS>`. Tier 0 stays declarative data packs.

Option B may be added later for a data-only class if A proves heavy for it. That would be its own ADR.

Options C and D are rejected. C removes process isolation, which the trust tiers rely on. D needs a sandbox we would have to build and secure, and it still runs plugin code in the launcher's process.

## Consequences

- A crash or hang in a plugin stays in the plugin's process. The spike shows the launcher recovering from both.
- Plugin authors need an Android build. Each plugin is one install.
- Every class adds a `<queries>` entry to the launcher manifest.
- Every call to a plugin runs off the main thread with a timeout. Limits on time and calls in flight are set in the spec in M0.5.
- Each bound plugin is an extra process. Memory use on low-RAM TVs is not measured yet. M0.5 measures it on the emulator and M1.5 on a real device.
- Who may bind to a plugin's service is not settled. Any app can bind to an exported service unless it is protected by a permission. M0.5 decides how a plugin limits binding to the launcher.
- The spike interface and client are throwaway. M0.5 replaces them with the real plugin API and host.

## On acceptance

- Mark the plugin form as decided in section 6 of the design proposal, in `website/public/design/plugin-system-proposal.html`, and in the roadmap's Still open table.
- Mark M0.2 done in the roadmap.
