# 0005. Emulator test layer in CI

Status: Accepted. Approved by the owner (JaysenDraney) on 2026-11-28 in PR #10 (roadmap milestone M0.4).

## Context

ADR 0002 defines layer 4 (emulator tests) and left one question unverified: whether a TV
system image can run headless in CI, naming Gradle Managed Devices as the thing to check.
M0.2 and M0.3 kept L4 tests (plugin transport, HOME intent, settings persistence) that ran
only on a local Google TV API 36 emulator. M0.4 requires these tests in GitHub Actions, a
measured record of run time and flakiness over at least 10 runs, and a recorded decision on
running them on every pull request or nightly.

## Decision

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
  `docs/`), with cancel-in-progress per PR, plus manual dispatch. The test script retries
  once on failure to absorb transient repository flakes. At a measured ~5.5 minutes per
  run and no code-cause test failures in 10 runs, every-PR is affordable; nightly-only
  would let transport and HOME regressions land between runs.

## Measurements

10 `workflow_dispatch` runs on `main` at commit 5f942af, 2026-11-28, all started within
16 seconds of each other (parallel, same code):

- 8 passed. Run times 4m40s to 5m53s, median about 5m30s (run-level, including queue).
- 2 failed (20%) with no code cause, both about 30 seconds in, before any test ran:
  Gradle could not resolve a plugin artifact from the repositories (Roborazzi in one run,
  the Kotlin JVM plugin in the other). Transient repository/network failures; both
  runners also downloaded the Gradle distribution cold.
- No test failed in any run that reached the tests: the L4 suite itself was 8/8 every
  time it executed.
- Mitigation: the test script now retries once on failure, which covers the observed
  failure mode. A real test failure still fails the job after the retry.

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
- A freshly booted CI AVD differs from a long-used development AVD: the system launcher
  does not qualify for the HOME role until the image is provisioned, so the launcher can
  become the role holder on install, and restoring the previous home can fail. The HOME
  test therefore restores device state best-effort and retries the home selection while
  the role service picks up a fresh install. Verified on both a fresh and a provisioned
  AVD locally.
- The snapshot cache must be invalidated when the image, profile or emulator options
  change; the cache key carries the image and profile.
