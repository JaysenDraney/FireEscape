package org.fireescape.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** M0.6 L1: class-to-tier mapping, and that a declaration cannot lower the tier. */
class PermissionBrokerTest {

    private fun plugin(api: String, compatible: Boolean = true): InstalledPlugin {
        val base = PluginMetadata.validate(
            PluginDescriptor("p", "c", api, "n")
        )!!
        return base.copy(compatible = compatible)
    }

    @Test
    fun everyClassMapsToExactlyOneTier() {
        assertEquals(0, PluginClass.THEME_LAYOUT.tier)
        assertEquals(1, PluginClass.WALLPAPER.tier)
        assertEquals(1, PluginClass.WIDGET.tier)
        assertEquals(1, PluginClass.IDLE.tier)
        for (c in listOf(
            PluginClass.CONTENT_ROWS, PluginClass.LAUNCH_SOURCE, PluginClass.CATALOG,
            PluginClass.AVAILABILITY, PluginClass.RECOMMENDATION,
        )) assertEquals(2, c.tier)
        assertEquals(3, PluginClass.AUTOMATION.tier)
        assertEquals(3, PluginClass.PROFILES_POLICY.tier)
    }

    @Test
    fun incompatiblePluginGetsNoGrants() {
        val p = plugin("wallpaper/2", compatible = false)
        assertTrue(PermissionBroker.activeGrants(p, setOf(Grant.NETWORK)).isEmpty())
    }

    @Test
    fun grantOutsideTheClassAllowListIsDropped() {
        // A wallpaper plugin stored with an installed-apps grant never receives it.
        val p = plugin("wallpaper/1")
        val active = PermissionBroker.activeGrants(p, setOf(Grant.NETWORK, Grant.INSTALLED_APPS))
        assertEquals(setOf(Grant.NETWORK), active)
    }

    @Test
    fun withdrawingAGrantTakesEffectFromTheNextCall() {
        // The recommendation class can hold the ratings grant; withdrawal is checked
        // against the active set the broker computes for each call.
        val p = plugin("recommendation/1")
        val before = PermissionBroker.activeGrants(p, setOf(Grant.NETWORK, Grant.RATINGS))
        val after = PermissionBroker.activeGrants(p, setOf(Grant.NETWORK))
        assertTrue(Grant.RATINGS in before)
        assertFalse(Grant.RATINGS in after)
    }

    @Test
    fun tierThreeNeedsSeparateApproval() {
        assertTrue(PermissionBroker.requiresSeparateApproval(plugin("automation/1")))
        assertFalse(PermissionBroker.requiresSeparateApproval(plugin("wallpaper/1")))
    }

    @Test
    fun externalRecommendationDefaultsToCandidatePool() {
        val p = plugin("recommendation/1")
        assertEquals(
            DataLevel.CANDIDATE_POOL,
            PermissionBroker.dataLevelFor(p, active = emptySet(), isExternal = true),
        )
        assertEquals(
            DataLevel.PLUS_RATINGS,
            PermissionBroker.dataLevelFor(p, active = setOf(Grant.RATINGS), isExternal = true),
        )
        // External providers never receive watch history, even if a stale grant exists.
        assertEquals(
            DataLevel.CANDIDATE_POOL,
            PermissionBroker.dataLevelFor(
                p, active = setOf(Grant.WATCH_HISTORY), isExternal = true,
            ),
        )
    }

    @Test
    fun localRecommendationCanReachHistoryWhenGranted() {
        val p = plugin("recommendation/1")
        assertEquals(
            DataLevel.PLUS_WATCH_HISTORY,
            PermissionBroker.dataLevelFor(
                p, active = setOf(Grant.RATINGS, Grant.WATCH_HISTORY), isExternal = false,
            ),
        )
    }
}
