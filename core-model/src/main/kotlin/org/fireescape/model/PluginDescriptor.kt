package org.fireescape.model

/** The plugin classes from the design proposal. Each class belongs to exactly one trust tier. */
enum class PluginClass(val tier: Int) {
    THEME_LAYOUT(0),        // Tier 0, declarative pack
    WALLPAPER(1),           // Tier 1
    WIDGET(1),              // Tier 1
    IDLE(1),                // Tier 1
    CONTENT_ROWS(2),        // Tier 2
    LAUNCH_SOURCE(2),       // Tier 2
    CATALOG(2),             // Tier 2
    AVAILABILITY(2),        // Tier 2
    RECOMMENDATION(2),      // Tier 2 (local). External providers use the trusted-publisher rules.
    AUTOMATION(3),          // Tier 3
    PROFILES_POLICY(3),     // Tier 3
    SPIKE(1),               // M0.5 host-skeleton test contract; real classes arrive with M1.x
}

/** A capability a plugin declares. Defaults: every capability is denied. */
enum class Capability {
    NETWORK,
    SEES_INSTALLED_APPS,
    FOCUS_CONTEXT, // focused tile's package name, off unless granted
    READS_RATINGS,
    READS_WATCH_HISTORY,
    SYSTEM_GRANT, // an Android permission that needs ADB or a system setting
}

/**
 * What the host learned about one plugin service, parsed from its manifest metadata.
 * `api` names the class contract and major version, e.g. "wallpaper/1".
 */
data class PluginDescriptor(
    val packageName: String,
    val serviceClassName: String,
    val api: String,
    val displayName: String,
    /** OSI-approved license identifier, required for the default index. */
    val licenseId: String? = null,
    /** Source URL, required for plugins listed in the default index. */
    val sourceUrl: String? = null,
    val declaredCapabilities: Set<Capability> = emptySet(),
)

/** A plugin as the host sees it: its descriptor plus what the launcher derived from it. */
data class InstalledPlugin(
    val descriptor: PluginDescriptor,
    val pluginClass: PluginClass,
    val apiMajor: Int,
    /** True when the launcher supports this class contract's major version. */
    val compatible: Boolean,
) {
    /** The tier comes from the class, never from the plugin's own claim. */
    val tier: Int get() = pluginClass.tier
}

object PluginMetadata {
    /** Metadata keys a plugin service declares. See Appendix A of the design proposal. */
    const val KEY_API = "fireescape.api"
    const val KEY_CLASS = "fireescape.class"
    const val KEY_CAPABILITIES = "fireescape.capabilities"
    const val KEY_SOURCE = "fireescape.source"
    const val KEY_LICENSE = "fireescape.license"

    /** Per-class intent action used to discover plugins. */
    const val ACTION_PREFIX = "org.fireescape.plugin."

    /** API major versions this launcher supports. Two majors at once, per the stability rule. */
    val supportedMajors: Map<String, Set<Int>> = mapOf(
        "theme" to setOf(1),
        "wallpaper" to setOf(1),
        "widget" to setOf(1),
        "idle" to setOf(1),
        "contentrows" to setOf(1),
        "launchsource" to setOf(1),
        "catalog" to setOf(1),
        "availability" to setOf(1),
        "recommendation" to setOf(1),
        "automation" to setOf(1),
        "profiles" to setOf(1),
        "spike" to setOf(1),
    )

    private val classByName: Map<String, PluginClass> = mapOf(
        "theme" to PluginClass.THEME_LAYOUT,
        "wallpaper" to PluginClass.WALLPAPER,
        "widget" to PluginClass.WIDGET,
        "idle" to PluginClass.IDLE,
        "contentrows" to PluginClass.CONTENT_ROWS,
        "launchsource" to PluginClass.LAUNCH_SOURCE,
        "catalog" to PluginClass.CATALOG,
        "availability" to PluginClass.AVAILABILITY,
        "recommendation" to PluginClass.RECOMMENDATION,
        "automation" to PluginClass.AUTOMATION,
        "profiles" to PluginClass.PROFILES_POLICY,
        "spike" to PluginClass.SPIKE,
    )

    /** Maps a class contract name (the part before "/") to its class, or null if unknown. */
    fun classFor(apiName: String): PluginClass? = classByName[apiName.substringBefore("/")]

    /** Parses one raw metadata value into a capability, or null if unrecognized. */
    fun parseCapability(raw: String): Capability? =
        Capability.entries.firstOrNull { it.name.equals(raw.trim(), ignoreCase = true) }

    /**
     * Validates a descriptor and derives its class, major version and compatibility.
     * Returns null when the declaration is malformed; callers must then list the plugin
     * as invalid and never bind it.
     */
    fun validate(descriptor: PluginDescriptor): InstalledPlugin? {
        val slash = descriptor.api.lastIndexOf('/')
        if (slash <= 0 || slash == descriptor.api.length - 1) return null
        val name = descriptor.api.substring(0, slash)
        val major = descriptor.api.substring(slash + 1).toIntOrNull() ?: return null
        if (major < 1) return null
        val pluginClass = classFor(name) ?: return null
        val compatible = major in (supportedMajors[name] ?: emptySet())
        return InstalledPlugin(
            descriptor = descriptor,
            pluginClass = pluginClass,
            apiMajor = major,
            compatible = compatible,
        )
    }
}
