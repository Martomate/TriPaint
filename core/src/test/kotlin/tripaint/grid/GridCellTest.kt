package tripaint.grid

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import tripaint.color.Color
import tripaint.coords.GridCoords
import tripaint.coords.TriangleCoords
import tripaint.image.ImageStorage
import tripaint.util.Tracker
import kotlin.test.Ignore
import kotlin.test.Test

class GridCellTest {
    @Nested
    inner class OnImageChangedALot {
        @Test
        fun `tells the listeners that a lot has changed`() {
            val image = ImageStorage.fill(2, Color.Black)
            val f = GridCell(GridCoords.from(0, 0), image)

            val tracker = Tracker.withStorage<GridCell.Event>()
            f.trackChanges(tracker)

            f.onImageChangedALot()

            assertEquals(tracker.events, listOf(GridCell.Event.ImageChangedALot))
        }
    }

    @Nested
    inner class Changed {
        @Test
        fun `returns false if nothing has happened`() {
            val image = ImageStorage.fill(2, Color.Black)
            val f = GridCell(GridCoords.from(0, 0), image)
            assert(!f.changed)
        }

        @Test
        fun `returns true if the image has been modified since the last save`() {
            val image = ImageStorage.fill(2, Color.Black)
            val f = GridCell(GridCoords.from(0, 0), image)

            image.setColor(TriangleCoords.from(0, 0), Color.Blue)

            assert(f.changed)
        }

        @Test
        fun `returns false if the image was just saved`() {
            val image = ImageStorage.fill(2, Color.Black)

            val cell = GridCell(GridCoords.from(0, 0), image)

            image.setColor(TriangleCoords.from(0, 0), Color.Blue)
            assert(cell.changed)

            cell.setImageSaved()
            assert(!cell.changed)
        }
    }

    @Ignore
    @Test
    fun `changedProperty should return a property with the same value as 'changed'`() {}

    @Ignore
    @Test
    fun `image should return the initial image if the image hasn't been replaced in the pool`() {}

    @Ignore
    @Test
    fun `image should return the new image if the image was replaced in the pool`() {}

    // TODO: maybe it should take a coordinate instead of an initial image
}