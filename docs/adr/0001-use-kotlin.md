# 0001. Use Kotlin, not Java

Status: Accepted

## Context

FireEscape is an Android TV and Google TV launcher with a plugin SDK and sample plugins. The proposal (Appendix A) already leans toward Kotlin and Jetpack Compose for TV. We need a single language rule so the core, SDK and samples read the same way and contributors know what to write.

## Decision

All FireEscape code is written in Kotlin. We prefer Kotlin to Java for new code in the core, the plugin SDK, sample plugins and tests. Java is not used for hand-written code unless Kotlin cannot do the job, and the reason is stated in the change that adds it.

## Consequences

- Jetpack Compose for TV requires Kotlin, so the UI and the language agree.
- Plugin interfaces are defined in AIDL. AIDL is its own interface language and is not Kotlin or Java, so it is an exception to this rule. Hand-written code that implements or calls those interfaces is Kotlin.
- Build scripts use the Gradle Kotlin DSL (`build.gradle.kts`).
- Plugin authors may use any language that can implement the AIDL interface. This ADR governs this repository only.
- Contributors need to know Kotlin. This is the main cost.
