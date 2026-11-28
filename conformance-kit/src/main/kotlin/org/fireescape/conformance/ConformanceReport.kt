package org.fireescape.conformance

/** One contract rule's outcome. A failure's detail names what went wrong. */
data class RuleResult(val rule: String, val passed: Boolean, val detail: String)

/** Per-rule pass/fail for one plugin against its class contract. */
data class ConformanceReport(val plugin: String, val results: List<RuleResult>) {
    val passed: Boolean get() = results.isNotEmpty() && results.all { it.passed }

    fun render(): String = buildString {
        appendLine(if (passed) "PASS $plugin" else "FAIL $plugin")
        results.forEach { result ->
            appendLine("  ${if (result.passed) "ok  " else "FAIL"} ${result.rule}: ${result.detail}")
        }
    }
}
