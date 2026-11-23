package org.fireescape.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RowResolverTest {
    @Test
    fun priorityOutranksConfidence() {
        val high = Binding("high-priority", priority = 1, confidence = 0.2)
        val confident = Binding("confident", priority = 2, confidence = 0.99)
        assertEquals(high, RowResolver.resolve(listOf(confident, high)))
    }

    @Test
    fun confidenceBreaksTiesWithinAPriority() {
        val a = Binding("a", priority = 1, confidence = 0.4)
        val b = Binding("b", priority = 1, confidence = 0.8)
        assertEquals(b, RowResolver.resolve(listOf(a, b)))
    }

    @Test
    fun bindingBelowItsFloorIsSkipped() {
        val belowFloor = Binding("below", priority = 1, confidence = 0.3, confidenceFloor = 0.5)
        val fallback = Binding("fallback", priority = 2, confidence = 0.1)
        assertEquals(fallback, RowResolver.resolve(listOf(belowFloor, fallback)))
    }

    @Test
    fun noBindingsResolvesToNull() {
        assertNull(RowResolver.resolve(emptyList()))
    }
}
