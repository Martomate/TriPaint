package tripaint.app

import org.junit.jupiter.api.Assertions.assertEquals
import tripaint.color.Color
import tripaint.coords.StorageCoords
import tripaint.grid.ImageGrid
import tripaint.image.ImagePool
import tripaint.image.ImageStorage
import tripaint.image.RegularImage
import tripaint.image.format.SimpleStorageFormat
import tripaint.util.Tracker
import java.io.File
import kotlin.test.Test

class ImageSaverTest {
    @Test
    fun `save should return false if the saver reports failure`() {
        val image = ImageStorage.fill(2, Color.Black)
        val location = ImagePool.SaveLocation(File("a.png"))
        val format = SimpleStorageFormat
        val info = ImagePool.SaveInfo(format)

        val grid = ImageGrid(2)

        val fs = FileSystem.createNull(FileSystem.NullArgs(supportedImageFormats = setOf()))
        assertEquals(false, ImageSaver.saveImage(grid, image, fs, location, info))
    }

    @Test
    fun `save should write image if it does not exist`() {
        val image = ImageStorage.fill(2, Color.Blue)
        val path = "a.png"
        val location = ImagePool.SaveLocation(File(path))
        val format = SimpleStorageFormat
        val info = ImagePool.SaveInfo(format)

        val fs = FileSystem.createNull()
        val tracker = Tracker.withStorage<FileSystem.Event>()
        fs.trackChanges(tracker)

        val grid = ImageGrid(2)

        ImageSaver.saveImage(grid, image, fs, location, info)

        assertEquals(
            listOf(
                FileSystem.Event.ImageWritten(image.toRegularImage(format), File(path))
            ),
            tracker.events
        )
    }

    @Test
    fun `save should overwrite image if it exists and has the same size`() {
        val image = ImageStorage.fill(2, Color.Blue)
        val path = "a.png"
        val location = ImagePool.SaveLocation(File(path))
        val format = SimpleStorageFormat
        val info = ImagePool.SaveInfo(format)

        val existingImage = RegularImage.fill(2, 2, Color.Red)
        val fs = FileSystem.createNull(
            FileSystem.NullArgs(initialImages = mapOf(Pair(File(path), existingImage)))
        )
        val tracker = Tracker.withStorage<FileSystem.Event>()
        fs.trackChanges(tracker)

        val grid = ImageGrid(2)

        ImageSaver.saveImage(grid, image, fs, location, info)

        assertEquals(
            listOf(
                FileSystem.Event.ImageWritten(image.toRegularImage(format), File(path))
            ),
            tracker.events
        )
    }

    @Test
    fun `save should overwrite part of image if there already exists a bigger image`() {
        val image = ImageStorage.fill(2, Color.Blue)
        val path = "a.png"
        val offset = StorageCoords.from(1, 2)
        val location = ImagePool.SaveLocation(File(path), offset)
        val format = SimpleStorageFormat
        val info = ImagePool.SaveInfo(format)

        val existingImage = RegularImage.fill(3, 5, Color.Red)
        val fs = FileSystem.createNull(
            FileSystem.NullArgs(initialImages = mapOf(Pair(File(path), existingImage)))
        )
        val tracker = Tracker.withStorage<FileSystem.Event>()
        fs.trackChanges(tracker)

        val grid = ImageGrid(2)

        ImageSaver.saveImage(grid, image, fs, location, info)

        val expectedImage = RegularImage.fill(3, 5, Color.Red)
        expectedImage.pasteImage(offset, RegularImage.fill(2, 2, Color.Blue))

        assertEquals(listOf(FileSystem.Event.ImageWritten(expectedImage, File(path))), tracker.events)
    }

    @Test
    fun `save should overwrite part of image if there already exists an image even if it is too small`() {
        val image = ImageStorage.fill(2, Color.Blue)
        val path = "a.png"
        val offset = StorageCoords.from(1, 2)
        val location = ImagePool.SaveLocation(File(path), offset)
        val format = SimpleStorageFormat
        val info = ImagePool.SaveInfo(format)

        val existingImage = RegularImage.fill(3, 2, Color.Red)
        val fs = FileSystem.createNull(
            FileSystem.NullArgs(initialImages = mapOf(Pair(File(path), existingImage)))
        )
        val tracker = Tracker.withStorage<FileSystem.Event>()
        fs.trackChanges(tracker)

        val grid = ImageGrid(2)

        ImageSaver.saveImage(grid, image, fs, location, info)

        val expectedImage = RegularImage.ofSize(3, 4)
        expectedImage.pasteImage(StorageCoords.from(0, 0), RegularImage.fill(3, 2, Color.Red))
        expectedImage.pasteImage(offset, RegularImage.fill(2, 2, Color.Blue))

        assertEquals(listOf(FileSystem.Event.ImageWritten(expectedImage, File(path))), tracker.events)
    }
}