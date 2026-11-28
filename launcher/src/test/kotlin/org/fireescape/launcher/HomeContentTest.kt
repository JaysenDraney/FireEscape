package org.fireescape.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** M0.3 L1: the empty-state decision, pure logic. */
class HomeContentTest {

    @Test
    fun noShelvesMeansEmptyState() {
        assertEquals(HomeContent.Empty, HomeContent.from(emptyList()))
    }

    @Test
    fun shelvesWithoutTilesMeanEmptyState() {
        val shelves = listOf(Shelf("Continue watching", 0), Shelf("Apps", 0))
        assertEquals(HomeContent.Empty, HomeContent.from(shelves))
    }

    @Test
    fun anyShelfWithTilesShowsShelvesInOrder() {
        val shelves = listOf(Shelf("Apps", 0), Shelf("Continue watching", 4))
        val content = HomeContent.from(shelves)
        assertTrue(content is HomeContent.Shelves)
        assertEquals(shelves, (content as HomeContent.Shelves).shelves)
    }
}
