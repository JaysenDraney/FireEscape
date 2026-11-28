package org.fireescape.launcher

/** One home-screen shelf. Tiles are placeholders until the plugin host lands (M0.5). */
data class Shelf(val title: String, val tileCount: Int)

/** What the home screen shows: shelves, or the empty state when no shelf has any tile. */
sealed interface HomeContent {
    data object Empty : HomeContent

    data class Shelves(val shelves: List<Shelf>) : HomeContent

    companion object {
        fun from(shelves: List<Shelf>): HomeContent =
            if (shelves.none { it.tileCount > 0 }) Empty else Shelves(shelves)
    }
}
