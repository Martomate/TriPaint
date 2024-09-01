package tripaint.app

import org.junit.jupiter.api.Assertions.assertEquals
import tripaint.color.Color
import tripaint.coords.GridCoords
import tripaint.image.RegularImage
import tripaint.image.format.SimpleStorageFormat
import kotlin.test.Test

class NewActionTest {
    @Test
    fun `NewAction should add a new image to the grid`() {
        val model = TriPaintModel.createNull(8)

        val imageSize = model.imageGrid.imageSize
        val backgroundColor = Color.Cyan

        Actions.createNewImage(model.imageGrid, backgroundColor, GridCoords.from(3, 4))

        val expectedImage = RegularImage.fill(imageSize, imageSize, backgroundColor)

        val cell = model.imageGrid.apply(GridCoords.from(3, 4))!!
        val actualImage = cell.storage.toRegularImage(SimpleStorageFormat)

        assertEquals(expectedImage, actualImage)
    }

    // TODO: Is this really how it should work?
    @Test
    fun `NewAction should replace any existing image at the location`() {
        val model = TriPaintModel.createNull(8)

        val imageSize = model.imageGrid.imageSize
        val backgroundColor = Color.Cyan

        Actions.createNewImage(model.imageGrid, backgroundColor, GridCoords.from(3, 4))
        Actions.createNewImage(model.imageGrid, backgroundColor, GridCoords.from(3, 4))

        val expectedImage = RegularImage.fill(imageSize, imageSize, backgroundColor)

        val cell = model.imageGrid.apply(GridCoords.from(3, 4))!!
        val actualImage = cell.storage.toRegularImage(SimpleStorageFormat)

        assertEquals(expectedImage, actualImage)
    }
}