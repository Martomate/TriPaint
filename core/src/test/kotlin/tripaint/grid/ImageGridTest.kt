package tripaint.grid

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import tripaint.color.Color
import tripaint.coords.GridCoords
import tripaint.coords.TriangleCoords
import tripaint.image.ImagePool
import tripaint.image.ImageStorage
import tripaint.image.format.SimpleStorageFormat
import tripaint.util.Tracker
import java.io.File
import kotlin.test.Test

class ImageGridTest {
    private val collisionHandler: ImageSaveCollisionHandler = mockk<ImageSaveCollisionHandler>()

    @Nested
    inner class SetImageSizeIfEmpty {
        @Test
        fun `sets the image size and returns true if the grid is new`() {
            val f = ImageGrid(16)
            val initSize = f.imageSize
            assertEquals(true, f.setImageSizeIfEmpty(initSize + 16))
            assertEquals(initSize + 16, f.imageSize)
        }

        @Test
        fun `does not set the image size and returns false if the grid contains images`() {
            val f = ImageGrid(16)
            val initSize = f.imageSize

            f.set(GridCell(GridCoords.from(0, 0), ImageStorage.fill(4, Color.Black)))

            assertEquals(f.setImageSizeIfEmpty(initSize + 16), false)
            assertEquals(f.imageSize, initSize)
        }

        @Test
        fun `sets the image size and returns true if the grid no longer contains images`() {
            val f = ImageGrid(16)
            val initSize = f.imageSize

            f.set(GridCell(GridCoords.from(0, 0), ImageStorage.fill(4, Color.Black)))
            f.remove(GridCoords.from(0, 0))

            assertEquals(f.setImageSizeIfEmpty(initSize + 16), true)
            assertEquals(f.imageSize, initSize + 16)
        }
    }

    @Nested
    inner class Apply {
        @Test
        fun `returns null if there is no image there`() {
            val f = ImageGrid(16)
            val image = GridCell(GridCoords.from(1, 0), ImageStorage.fill(4, Color.Black))

            assertEquals(f.apply(GridCoords.from(0, 0)), null)
            f.set(image)
            assertEquals(f.apply(GridCoords.from(0, 0)), null)
        }

        @Test
        fun `returns the image at the given location`() {
            val f = ImageGrid(16)
            val image = GridCell(GridCoords.from(1, 0), ImageStorage.fill(4, Color.Black))

            assertEquals(f.apply(GridCoords.from(1, 0)), null)
            f.set(image)
            assertEquals(f.apply(GridCoords.from(1, 0)), image)
            assertEquals(f.apply(GridCoords.from(0, 1)), null)
            assertEquals(f.apply(GridCoords.from(1, 0)), image)
        }
    }

    @Nested
    inner class Update {
        @Test
        fun `adds the image if it doesn't already exist`() {
            val f = ImageGrid(16)
            val image = GridCell(GridCoords.from(1, 0), ImageStorage.fill(4, Color.Black))

            f.set(image)
            assertEquals(f.apply(GridCoords.from(1, 0)), image)
        }

        @Test
        fun `replaces the image if there is already one at that location`() {
            val f = ImageGrid(16)
            val image = GridCell(GridCoords.from(1, 0), ImageStorage.fill(4, Color.Black))
            val image2 = GridCell(GridCoords.from(1, 0), ImageStorage.fill(4, Color.Black))

            f.set(image)
            f.set(image2)
            assertEquals(f.apply(GridCoords.from(1, 0)), image2)
        }

        @Test
        fun `notifies listeners about image addition, and image removal if there was one`() {
            val f = ImageGrid(16)
            val image = GridCell(GridCoords.from(1, 0), ImageStorage.fill(4, Color.Black))
            val image2 = GridCell(GridCoords.from(1, 0), ImageStorage.fill(4, Color.Black))

            val tracker = Tracker.withStorage<ImageGrid.Event>()
            f.trackChanges(tracker)

            f.set(image)
            assertEquals(tracker.events, listOf(ImageGrid.Event.ImageAdded(image)))

            f.set(image2)
            assertEquals(
                tracker.events,
                listOf(
                    ImageGrid.Event.ImageAdded(image),
                    ImageGrid.Event.ImageRemoved(image),
                    ImageGrid.Event.ImageAdded(image2)
                )
            )
        }
    }

    @Nested
    inner class Remove {
        @Test
        fun `returns null if there is no image there`() {
            val f = ImageGrid(16)
            assertEquals(f.remove(GridCoords.from(1, 2)), null)
        }

        @Test
        fun `removes the image and returns it if it exists`() {
            val f = ImageGrid(16)
            val image = GridCell(GridCoords.from(1, 0), ImageStorage.fill(4, Color.Black))

            f.set(image)
            assertEquals(image, f.remove(GridCoords.from(1, 0)))
            assertEquals(null, f.apply(GridCoords.from(1, 0)))
        }

        @Test
        fun `notifies listeners if there was a removal`() {
            val f = ImageGrid(16)
            val image = GridCell(GridCoords.from(1, 0), ImageStorage.fill(4, Color.Black))

            f.set(image)

            val tracker = Tracker.withStorage<ImageGrid.Event>()
            f.trackChanges(tracker)

            f.remove(GridCoords.from(0, 0))
            assertEquals(tracker.events, emptyList<ImageGrid.Event>())

            f.remove(GridCoords.from(1, 0))
            assertEquals(tracker.events, listOf(ImageGrid.Event.ImageRemoved(image)))
        }
    }

    @Nested
    inner class SelectedImages {
        @Test
        fun `returns all images that are currently selected`() {
            val f = ImageGrid(16)
            val image = GridCell(GridCoords.from(1, 0), ImageStorage.fill(4, Color.Black))
            val image2 = GridCell(GridCoords.from(2, 0), ImageStorage.fill(4, Color.Black))

            f.set(image)
            f.set(image2)

            assertEquals(
                listOf(image, image2).sortedBy { it.hashCode() },
                f.selectedImages().sortedBy { it.hashCode() })
            image.editable = false
            assertEquals(listOf(image2).sortedBy { it.hashCode() }, f.selectedImages().sortedBy { it.hashCode() })
            image.editable = true
            image2.editable = false
            assertEquals(listOf(image).sortedBy { it.hashCode() }, f.selectedImages().sortedBy { it.hashCode() })
            image2.editable = true
            assertEquals(
                listOf(image, image2).sortedBy { it.hashCode() },
                f.selectedImages().sortedBy { it.hashCode() })
        }
    }

    @Nested
    inner class Undo {
        @Test
        fun `removes the last action`() {
            val grid = ImageGrid(16)

            val storage = ImageStorage.fill(16, Color.Black)
            storage.setColor(TriangleCoords.from(1, 2), Color.Blue)

            grid.set(GridCell(GridCoords.from(1, 0), storage))

            val imageChange = ImageChange
                .builder()
                .addChange(TriangleCoords.from(1, 2), Color.Blue, Color.Red)
                .addChange(TriangleCoords.from(3, 5), Color.Black, Color.Cyan)
                .done(storage)
            grid.performChange(ImageGridChange(mapOf(Pair(GridCoords.from(1, 0), imageChange))))

            assertEquals(Color.Red, storage.getColor(TriangleCoords.from(1, 2)))
            assertEquals(Color.Cyan, storage.getColor(TriangleCoords.from(3, 5)))

            grid.undo()

            assertEquals(storage.getColor(TriangleCoords.from(1, 2)), Color.Blue)
            assertEquals(storage.getColor(TriangleCoords.from(3, 5)), Color.Black)
        }
    }

    @Nested
    inner class SetImageSource {
        @Test
        fun `sets the image and returns true if the location is empty`() {
            val image = ImageStorage.fill(8, Color.Blue)
            val location = ImagePool.SaveLocation(File(""))
            val info = ImagePool.SaveInfo(SimpleStorageFormat)

            val p = ImagePool()
            val grid = ImageGrid(8)

            assertEquals(true, grid.setImageSource(image, location, info, p, collisionHandler))
            assertEquals(location, p.locationOf(image))
        }

        @Test
        fun `returns true if the image is already there`() {
            val image = ImageStorage.fill(8, Color.Blue)
            val location = ImagePool.SaveLocation(File(""))
            val info = ImagePool.SaveInfo(SimpleStorageFormat)

            val p = ImagePool()
            val grid = ImageGrid(8)

            grid.setImageSource(image, location, info, p, collisionHandler)
            assertEquals(true, grid.setImageSource(image, location, info, p, collisionHandler))
            assertEquals(location, p.locationOf(image))
        }

        @Test
        fun `returns false if the handler does not accept the collision`() {
            val handler = collisionHandler
            val currentImage = ImageStorage.fill(8, Color.Blue)
            val newImage = ImageStorage.fill(8, Color.Yellow)
            val location = ImagePool.SaveLocation(File(""))
            val info = ImagePool.SaveInfo(SimpleStorageFormat)

            val p = ImagePool()
            val grid = ImageGrid(8)

            every { handler.shouldReplaceImage(currentImage, newImage, location) } returns null

            grid.setImageSource(currentImage, location, info, p, handler)
            assertEquals(false, grid.setImageSource(newImage, location, info, p, handler))
        }

        @Test
        fun `replaces the current image and returns true if the handler wants to replace it`() {
            val handler = collisionHandler

            val currentImage = ImageStorage.fill(8, Color.Blue)
            val newImage = ImageStorage.fill(8, Color.Yellow)
            val location = ImagePool.SaveLocation(File(""))
            val info = ImagePool.SaveInfo(SimpleStorageFormat)

            val p = ImagePool()
            val grid = ImageGrid(8)

            every { handler.shouldReplaceImage(currentImage, newImage, location) } returns true

            grid.setImageSource(currentImage, location, info, p, handler)
            assertEquals(true, grid.setImageSource(newImage, location, info, p, handler))
            assertEquals(null, p.locationOf(currentImage))
            assertEquals(location, p.locationOf(newImage))
        }

        @Test
        fun `keeps the current image and returns true if the handler wants to keep it`() {
            val handler = collisionHandler

            val currentImage = ImageStorage.fill(8, Color.Blue)
            val newImage = ImageStorage.fill(8, Color.Yellow)
            val location = ImagePool.SaveLocation(File(""))
            val info = ImagePool.SaveInfo(SimpleStorageFormat)

            val p = ImagePool()
            val grid = ImageGrid(8)

            every { handler.shouldReplaceImage(currentImage, newImage, location) } returns false

            grid.setImageSource(currentImage, location, info, p, handler)
            assertEquals(true, grid.setImageSource(newImage, location, info, p, handler))
            assertEquals(location, p.locationOf(currentImage))
            assertEquals(null, p.locationOf(newImage))
        }
    }
}