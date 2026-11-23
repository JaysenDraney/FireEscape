package org.fireescape.model

/** A provider's offer to fill a home-screen row. */
data class Binding(
    val providerId: String,
    /** Lower number wins. Priority always outranks confidence. */
    val priority: Int,
    /** 0.0 to 1.0, reported by the provider. */
    val confidence: Double,
    /** Bindings below this confidence are skipped. Null means no floor. */
    val confidenceFloor: Double? = null,
)

object RowResolver {
    /**
     * Picks the binding that fills a row: the lowest priority number among bindings
     * that meet their confidence floor. Confidence only breaks ties within a priority.
     */
    fun resolve(bindings: List<Binding>): Binding? =
        bindings
            .filter { it.confidenceFloor == null || it.confidence >= it.confidenceFloor }
            .sortedWith(compareBy<Binding> { it.priority }.thenByDescending { it.confidence })
            .firstOrNull()
}
