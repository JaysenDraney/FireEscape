package org.fireescape.conformance

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** M0.8 L1: report aggregation and rendering name the broken rule. */
class ConformanceReportTest {

    @Test
    fun allRulesPassMeansPass() {
        val report = ConformanceReport(
            "p",
            listOf(RuleResult("bind", true, "ok"), RuleResult("api-version", true, "1")),
        )
        assertTrue(report.passed)
        assertTrue(report.render().startsWith("PASS p"))
    }

    @Test
    fun oneFailureFailsTheReportAndNamesTheRule() {
        val report = ConformanceReport(
            "p",
            listOf(RuleResult("bind", true, "ok"), RuleResult("api-version", false, "reported 2")),
        )
        assertFalse(report.passed)
        val rendered = report.render()
        assertTrue(rendered.startsWith("FAIL p"))
        assertTrue(rendered.contains("FAIL api-version: reported 2"))
    }

    @Test
    fun emptyReportIsNotAPass() {
        assertFalse(ConformanceReport("p", emptyList()).passed)
    }
}
