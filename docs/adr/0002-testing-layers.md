# 0002. Testing layers and Roborazzi for screenshots

Status: Accepted

## Context

We want to test FireEscape locally without a TV device. The project is Kotlin (ADR 0001) and will use Jetpack Compose for TV, plugins as separate APKs, and a custom focus engine. We need a set of test layers that run fast on a developer machine and in CI, with a clear boundary for what needs an emulator.

## Decision

We use these layers, from fastest to slowest.

1. **JVM unit tests** (JUnit, Kotlin). Row and priority resolution, schedules, confidence floors and Tier 0 JSON Schema validation. This logic lives in plain Kotlin modules with no Android dependency.
2. **Compose focus and D-pad tests.** The Compose test library with `createComposeRule()` simulates key input and asserts focus. They check behavior, not appearance. See the [Android documentation on testing focus](https://developer.android.com/develop/ui/compose/touch-input/focus/testing-focus).
3. **Screenshot tests with Roborazzi.** They run on the JVM under Robolectric with native graphics. They check theme and layout rendering.
4. **Emulator tests** on an Android TV virtual device. Plugin binding over AIDL, install flows and launcher behavior such as claiming the HOME intent. These cannot run on the JVM.

## Alternatives considered

- **Paparazzi.** It renders with Layoutlib rather than real Android, and it does not work with Robolectric. Reports describe differences from a device for animations and `LaunchedEffect` ([comparison post](https://sergiosastre.hashnode.dev/a-world-beyond-libraries-cross-library-screenshot-tests-on-android)). Its 2.0 line is still alpha. Roborazzi uses a supported Robolectric API, as noted in the [Now in Android PR](https://github.com/android/nowinandroid/pull/876).
- **Shot and Dropshots.** They are on-device screenshot tools and need a running emulator for every run. That conflicts with the goal of local tests without one. Their reference images also depend on the emulator configuration. Dropshots may be reconsidered for the emulator layer if JVM renders miss real bugs.

## Consequences

- Layers 1 to 3 run without a device or emulator, so they are the default for local runs and pull requests.
- Layer 4 is the only layer that covers things the JVM cannot: real binding between APKs, the TV system image and the real compositor.
- Screenshot reference images are committed to the repository and recorded on the JVM, so they do not depend on an emulator.
- Roborazzi depends on Robolectric. Robolectric's support for new Android API levels can lag. We have not verified the current state.
- Unverified: whether Gradle Managed Devices has a TV system image that can run headless in CI. We will check this when we build the emulator layer.
