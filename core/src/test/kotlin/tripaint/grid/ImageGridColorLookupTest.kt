package tripaint.grid

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import tripaint.color.Color
import tripaint.coords.GlobalPixCoords
import tripaint.coords.GridCoords
import tripaint.coords.TriangleCoords
import tripaint.image.ImageStorage
import kotlin.test.Test

class ImageGridColorLookupTest {
    @Nested
    inner class Lookup {
        @Test
        fun `returns null if there is no image`() {
            val grid = ImageGrid(16)
            val lookup = ImageGridColorLookup(grid)

            assertEquals(null, lookup.lookup(GlobalPixCoords.from(0, 0)))
            assertEquals(null, lookup.lookup(GlobalPixCoords.from(10, 0)))
            assertEquals(null, lookup.lookup(GlobalPixCoords.from(100, 0)))
            assertEquals(null, lookup.lookup(GlobalPixCoords.from(0, 100)))
            assertEquals(null, lookup.lookup(GlobalPixCoords.from(0, -100)))
            assertEquals(null, lookup.lookup(GlobalPixCoords.from(-40, 10)))
        }

        @Test
        fun `returns the correct color for the (0, 0) image`() {
            val grid = ImageGrid(16)
            val lookup = ImageGridColorLookup(grid)
            val storage = ImageStorage.fill(16, Color.Black)
            val content = GridCell(GridCoords.from(0, 0), storage)

            storage.setColor(TriangleCoords.from(1, 13), Color.White)

            grid.set(content)

            assertEquals(Color.White, lookup.lookup(GlobalPixCoords.from(1, 2)))
            assertEquals(Color.Black, lookup.lookup(GlobalPixCoords.from(1, 3)))
            assertEquals(null, lookup.lookup(GlobalPixCoords.from(-1, 3)))
        }

        @Test
        fun `returns the correct color for any image`() {
            val grid = ImageGrid(16)
            val lookup = ImageGridColorLookup(grid)
            val storage = ImageStorage.fill(16, Color.Black)
            val content = GridCell(GridCoords.from(-1, 0), storage)

            storage.setColor(TriangleCoords.from(0, 2), Color.White)

            grid.set(content)

            assertEquals(Color.White, lookup.lookup(GlobalPixCoords.from(-1, 2)))
            assertEquals(Color.Black, lookup.lookup(GlobalPixCoords.from(-1, 3)))
            assertEquals(null, lookup.lookup(GlobalPixCoords.from(0, 3)))
        }
    }
}