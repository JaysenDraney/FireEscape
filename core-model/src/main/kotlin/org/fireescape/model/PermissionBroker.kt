package org.fireescape.model

/** What the launcher grants a plugin. Grants live in the settings store and can be withdrawn. */
enum class Grant {
    NETWORK,
    INSTALLED_APPS,   // the launch-source list handed to a plugin
    FOCUS_CONTEXT,    // focused tile's package name
    RATINGS,          // data level 2 for recommendation providers
    WATCH_HISTORY,    // data level 3, local providers only
    TIER3_APPROVAL,   // separate approval required for Tier 3 classes
    SYSTEM_GRANT,     // an Android permission granted through ADB or a system setting
}

/**
 * The data levels from the external-services rules. A provider receives at most the
 * level it is granted; levels are cumulative (PLUS_RATINGS includes CANDIDATE_POOL).
 */
enum class DataLevel {
    CANDIDATE_POOL,
    PLUS_RATINGS,
    PLUS_WATCH_HISTORY,
}

/**
 * Decides what data the launcher hands a plugin on each call. The launcher controls
 * everything that crosses the plugin interface, so withdrawing a grant takes effect
 * from the next call onward.
 */
object PermissionBroker {

    /**
     * The tier applies automatically: a Tier 0 pack needs no grant, and a plugin's own
     * declaration can never lower its class tier. Returns the set of grants this plugin
     * currently holds.
     */
    fun activeGrants(plugin: InstalledPlugin, granted: Set<Grant>): Set<Grant> {
        if (!plugin.compatible) return emptySet()
        val allowed = allowedGrantsFor(plugin.pluginClass)
        return granted.intersect(allowed)
    }

    /** Grants a class can ever hold. Anything else is dropped, even if stored. */
    fun allowedGrantsFor(pluginClass: PluginClass): Set<Grant> = when (pluginClass) {
        PluginClass.THEME_LAYOUT -> emptySet()
        PluginClass.WALLPAPER -> setOf(Grant.NETWORK)
        PluginClass.WIDGET -> setOf(Grant.NETWORK)
        PluginClass.IDLE -> setOf(Grant.NETWORK)
        PluginClass.CONTENT_ROWS -> setOf(Grant.NETWORK)
        PluginClass.LAUNCH_SOURCE -> setOf(Grant.NETWORK)
        PluginClass.CATALOG -> setOf(Grant.NETWORK)
        PluginClass.AVAILABILITY -> setOf(Grant.NETWORK)
        PluginClass.RECOMMENDATION ->
            setOf(Grant.NETWORK, Grant.RATINGS, Grant.WATCH_HISTORY)
        PluginClass.AUTOMATION ->
            setOf(Grant.NETWORK, Grant.INSTALLED_APPS, Grant.TIER3_APPROVAL, Grant.SYSTEM_GRANT)
        PluginClass.PROFILES_POLICY ->
            setOf(Grant.INSTALLED_APPS, Grant.TIER3_APPROVAL, Grant.SYSTEM_GRANT)
    }

    /**
     * The highest data level a recommendation provider may receive. Watch history is
     * local-only; ratings require an explicit grant, and for an external provider also
     * a trusted publisher (checked by the caller via [externalNeedsPublisher]).
     */
    fun dataLevelFor(plugin: InstalledPlugin, active: Set<Grant>, isExternal: Boolean): DataLevel {
        if (plugin.pluginClass != PluginClass.RECOMMENDATION) return DataLevel.CANDIDATE_POOL
        if (isExternal) {
            // External providers default to the candidate pool. Ratings only, by name,
            // and never watch history.
            return if (Grant.RATINGS in active) DataLevel.PLUS_RATINGS else DataLevel.CANDIDATE_POOL
        }
        return when {
            Grant.WATCH_HISTORY in active -> DataLevel.PLUS_WATCH_HISTORY
            Grant.RATINGS in active -> DataLevel.PLUS_RATINGS
            else -> DataLevel.CANDIDATE_POOL
        }
    }

    /** Combination rule: a provider that can reach the network never receives watch history. */
    fun externalNeedsPublisher(isExternal: Boolean): Boolean = isExternal

    /** Whether the install/approval flow must show the separate Tier 3 warning screen. */
    fun requiresSeparateApproval(plugin: InstalledPlugin): Boolean =
        plugin.tier == 3 && plugin.compatible
}
