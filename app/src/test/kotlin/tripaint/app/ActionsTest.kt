package tripaint.app

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import tripaint.color.Color
import tripaint.coords.GridCoords
import tripaint.coords.StorageCoords
import tripaint.grid.ImageGrid
import tripaint.image.ImagePool
import tripaint.image.ImageStorage
import tripaint.image.RegularImage
import tripaint.image.format.SimpleStorageFormat
import tripaint.image.format.StorageFormat
import tripaint.view.FileOpenSettings
import java.io.File
import kotlin.test.Test

class ActionsTest {
    private val storageFormat: StorageFormat = SimpleStorageFormat

    @Nested
    inner class OpenImage {
        @Test
        fun `does nothing if loading fails`() {
            val location = ImagePool.SaveLocation(File("a.png"))
            val imageSize = 16

            val fileSystem = FileSystem.createNull(FileSystem.NullArgs(initialImages = mapOf()))
            val imagePool = ImagePool()
            val imageGrid = ImageGrid(imageSize)

            Actions.openImage(
                fileSystem, imagePool, imageGrid,
                location.file,
                FileOpenSettings(location.offset, storageFormat),
                GridCoords.from(0, 0)
            )

            assertEquals(null, imagePool.imageAt(location))
        }

        @Test
        fun `stores the loaded image in the image pool`() {
            val file = File("path/to/image.png")
            val location = ImagePool.SaveLocation(file)
            val imageSize = 16

            val image = ImageStorage.fill(imageSize, Color.Yellow)
            val regularImage = image.toRegularImage(storageFormat)

            val fileSystem = run {
                val initialImages = mapOf(Pair(file, regularImage))
                FileSystem.createNull(FileSystem.NullArgs(initialImages))
            }
            val imagePool = ImagePool()
            val imageGrid = ImageGrid(imageSize)

            Actions.openImage(
                fileSystem, imagePool, imageGrid,
                location.file,
                FileOpenSettings(location.offset, storageFormat),
                GridCoords.from(0, 0)
            )
            val loadedImage = imagePool.imageAt(location)!!

            assertEquals(regularImage, loadedImage.toRegularImage(storageFormat))
        }

        @Test
        fun `loads image with offset`() {
            val file = File("path/to/image.png")
            val offset = StorageCoords.from(2, 3)
            val location = ImagePool.SaveLocation(file, offset)
            val imageSize = 16

            val image = ImageStorage.fill(imageSize, Color.Yellow)
            val regularImage = image.toRegularImage(storageFormat)

            val storedImage = RegularImage.ofSize(imageSize + offset.x, imageSize + offset.y)
            storedImage.pasteImage(offset, regularImage)

            val fileSystem = FileSystem.createNull(FileSystem.NullArgs(initialImages = mapOf(Pair(file, storedImage))))
            val imagePool = ImagePool()
            val imageGrid = ImageGrid(imageSize)

            Actions.openImage(
                fileSystem, imagePool, imageGrid,
                location.file,
                FileOpenSettings(location.offset, storageFormat),
                GridCoords.from(0, 0)
            )
            val loadedImage = imagePool.imageAt(location)!!

            assertEquals(regularImage, loadedImage.toRegularImage(storageFormat))
        }
    }
}