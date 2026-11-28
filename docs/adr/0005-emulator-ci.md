# 0005. Emulator test layer in CI

Status: Proposed. Needs the owner's approval before it is Accepted (roadmap milestone M0.4).

## Context

ADR 0002 defines layer 4 (emulator tests) and left one question unverified: whether a TV
system image can run headless in CI, naming Gradle Managed Devices as the thing to check.
M0.2 and M0.3 kept L4 tests (plugin transport, HOME intent, settings persistence) that ran
only on a local Google TV API 36 emulator. M0.4 requires these tests in GitHub Actions, a
measured record of run time and flakiness over at least 10 runs, and a recorded decision on
running them on every pull request or nightly.

## Decision (proposed)

- Run L4 with `reactivecircus/android-emulator-runner` (v2.38.0, SHA-pinned) on
  `ubuntu-latest` with KVM enabled, headless: `-no-window -gpu swiftshader_indirect
  -noaudio -no-boot-anim`.
- Use the same image as local development: Google TV API 36, x86_64, `tv_1080p` profile.
  Test fidelity with the local AVD decides this: the M0.3 HOME test's system-launcher
  workaround is specific to the Google TV image.
- Cache the AVD and its boot snapshot with `actions/cache`; generate the snapshot on a
  cache miss. The cache key names the image and profile, so an image change invalidates it.
- Run the whole kept L4 suite plus a fast home smoke test. Install `:spike-plugin` before
  the connected tests, since the transport tests bind a second APK.
- Upload the test reports as artifacts when the job fails.
- Answer to ADR 0002's open question: Gradle Managed Devices is not used. Its TV image
  support stays unverified; the runner with the sdkmanager `google-tv` image is verified
  by the runs recorded below.
- Cadence: every pull request that changes code (the workflow ignores `website/` and
  `docs/`), with cancel-in-progress per PR, plus manual dispatch. See the measurements
  below for the run time this costs.

## Measurements

To be filled in from at least 10 CI runs before acceptance: run times and failures with
no code cause.

## Alternatives considered

- **Gradle Managed Devices with a TV image.** Integrated with Gradle and local runs, but
  its `google-tv` image availability and headless CI behavior are unverified, and an AOSP
  TV image would not match the Google TV image the tests target.
- **Nightly only.** Cheaper per PR, but transport and HOME regressions would land between
  runs and be bisected after the fact. Reconsider if the measured run time or flakiness
  makes every-PR runs too expensive.
- **Self-hosted runner with real TV hardware.** Out of scope here; M1.5 covers real-device
  validation.

## Consequences

- Every code PR pays the measured emulator run time on top of the JVM and screenshot job.
- The Google TV system launcher's HOME monopoly (recorded in M0.3) applies in CI too; the
  HOME test disables and stops it for the run and restores it afterwards.
- The snapshot cache must be invalidated when the image, profile or emulator options
  change; the cache key carries the image and profile.
