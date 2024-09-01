package tripaint.app

import org.junit.jupiter.api.Assertions.assertEquals
import tripaint.color.Color
import tripaint.coords.GridCoords
import tripaint.coords.StorageCoords
import tripaint.image.RegularImage
import tripaint.image.format.SimpleStorageFormat
import tripaint.view.FileOpenSettings
import java.io.File
import kotlin.test.Test

class OpenActionTest {
    @Test
    fun `OpenAction should open an image`() {
        val file = File("image.png")
        val image = RegularImage.fill(8, 8, Color.Yellow)
        image.setColor(5, 6, Color.Cyan)

        val model =
            TriPaintModel.createNull(8, FileSystem.NullArgs(initialImages = mapOf(Pair(file, image))))

        Actions.openImage(
            model,
            file,
            FileOpenSettings(StorageCoords.from(0, 0), SimpleStorageFormat),
            GridCoords.from(3, 4)
        )

        val cell = model.imageGrid.apply(GridCoords.from(3, 4))!!
        val actualImage = cell.storage.toRegularImage(SimpleStorageFormat)

        assertEquals(image, actualImage)
    }

    @Test
    fun `OpenAction should open an image at an offset`() {
        val file = File("image.png")
        val image = RegularImage.fill(8, 8, Color.Yellow)
        image.setColor(5, 6, Color.Cyan)

        val storedImage = RegularImage.ofSize(9, 10)
        val offset = StorageCoords.from(1, 2)
        storedImage.pasteImage(offset, image)

        val model =
            TriPaintModel.createNull(8, FileSystem.NullArgs(initialImages = mapOf(Pair(file, storedImage))))

        Actions.openImage(
            model,
            file,
            FileOpenSettings(offset, SimpleStorageFormat),
            GridCoords.from(3, 4)
        )

        val cell = model.imageGrid.apply(GridCoords.from(3, 4))!!
        val actualImage = cell.storage.toRegularImage(SimpleStorageFormat)

        assertEquals(image, actualImage)
    }
}