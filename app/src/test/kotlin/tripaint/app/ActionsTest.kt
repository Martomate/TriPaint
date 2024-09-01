package tripaint.app

import org.junit.jupiter.api.Assertions.assertEquals
import tripaint.color.Color
import tripaint.coords.GridCoords
import tripaint.coords.StorageCoords
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

    @Test
    fun `openImage should do nothing if loading failed`() {
        val location = ImagePool.SaveLocation(File(""))
        val imageSize = 16

        val model = TriPaintModel.createNull(imageSize, FileSystem.NullArgs(initialImages = mapOf()))
        val pool = model.imagePool

        Actions.openImage(
            model,
            location.file,
            FileOpenSettings(location.offset, storageFormat),
            GridCoords.from(0, 0)
        )

        assertEquals(null, pool.imageAt(location))
    }

    @Test
    fun `openImage should store the loaded image in the image pool`() {
        val file = File("path/to/image.png")
        val location = ImagePool.SaveLocation(file)
        val imageSize = 16

        val image = ImageStorage.fill(imageSize, Color.Yellow)
        val regularImage = image.toRegularImage(storageFormat)

        val model =
            TriPaintModel.createNull(
                imageSize,
                FileSystem.NullArgs(initialImages = mapOf(Pair(file, regularImage)))
        )

        Actions.openImage(
            model,
            location.file,
            FileOpenSettings(location.offset, storageFormat),
            GridCoords.from(0, 0)
        )
        val loadedImage = model.imagePool.imageAt(location)!!

        assertEquals(regularImage, loadedImage.toRegularImage(storageFormat))
    }

    @Test
    fun `openImage should load image with offset`() {
        val file = File("path/to/image.png")
        val offset = StorageCoords.from(2, 3)
        val location = ImagePool.SaveLocation(file, offset)
        val imageSize = 16

        val image = ImageStorage.fill(imageSize, Color.Yellow)
        val regularImage = image.toRegularImage(storageFormat)

        val storedImage = RegularImage.ofSize(imageSize + offset.x, imageSize + offset.y)
        storedImage.pasteImage(offset, regularImage)

        val model = TriPaintModel.createNull(
            imageSize,
            FileSystem.NullArgs(initialImages = mapOf(Pair(file, storedImage)))
        )
        val pool = model.imagePool

        Actions.openImage(
            model,
            location.file,
            FileOpenSettings(location.offset, storageFormat),
            GridCoords.from(0, 0)
        )
        val loadedImage = pool.imageAt(location)!!

        assertEquals(regularImage, loadedImage.toRegularImage(storageFormat))
    }
}