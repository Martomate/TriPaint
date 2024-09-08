package tripaint.app

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import tripaint.color.Color
import tripaint.coords.GridCoords
import tripaint.coords.StorageCoords
import tripaint.grid.ImageGrid
import tripaint.image.ImagePool
import tripaint.image.RegularImage
import tripaint.image.format.SimpleStorageFormat
import java.io.File
import kotlin.test.Test

class OpenActionTest {
    @Nested
    inner class OpenAction {
        @Test
        fun `opens an image`() {
            val file = File("image.png")
            val image = RegularImage.fill(8, 8, Color.Yellow)
            image.setColor(5, 6, Color.Cyan)

            val fileSystem = FileSystem.createNull(FileSystem.NullArgs(initialImages = mapOf(Pair(file, image))))
            val imagePool = ImagePool()
            val imageGrid = ImageGrid(8)

            Actions.openImage(
                fileSystem, imagePool, imageGrid,
                file,
                Pair(StorageCoords.from(0, 0), SimpleStorageFormat),
                GridCoords.from(3, 4)
            )

            val cell = imageGrid.apply(GridCoords.from(3, 4))!!
            val actualImage = cell.storage.toRegularImage(SimpleStorageFormat)

            assertEquals(image, actualImage)
        }

        @Test
        fun `opens an image at an offset`() {
            val file = File("image.png")
            val image = RegularImage.fill(8, 8, Color.Yellow)
            image.setColor(5, 6, Color.Cyan)

            val storedImage = RegularImage.ofSize(9, 10)
            val offset = StorageCoords.from(1, 2)
            storedImage.pasteImage(offset, image)

            val fileSystem = FileSystem.createNull(FileSystem.NullArgs(initialImages = mapOf(Pair(file, storedImage))))
            val imagePool = ImagePool()
            val imageGrid = ImageGrid(8)

            Actions.openImage(
                fileSystem, imagePool, imageGrid,
                file,
                Pair(offset, SimpleStorageFormat),
                GridCoords.from(3, 4)
            )

            val cell = imageGrid.apply(GridCoords.from(3, 4))!!
            val actualImage = cell.storage.toRegularImage(SimpleStorageFormat)

            assertEquals(image, actualImage)
        }
    }
}