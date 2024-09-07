package tripaint.app

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import tripaint.color.Color
import tripaint.coords.GridCoords
import tripaint.grid.ImageGrid
import tripaint.image.RegularImage
import tripaint.image.format.SimpleStorageFormat
import kotlin.test.Test

class NewActionTest {
    @Nested
    inner class NewAction {
        @Test
        fun `adds a new image to the grid`() {
            val imageGrid = ImageGrid(8)

            val imageSize = imageGrid.imageSize
            val backgroundColor = Color.Cyan

            Actions.createNewImage(imageGrid, backgroundColor, GridCoords.from(3, 4))

            val expectedImage = RegularImage.fill(imageSize, imageSize, backgroundColor)

            val cell = imageGrid.apply(GridCoords.from(3, 4))!!
            val actualImage = cell.storage.toRegularImage(SimpleStorageFormat)

            assertEquals(expectedImage, actualImage)
        }

        // TODO: Is this really how it should work?
        @Test
        fun `replaces any existing image at the location`() {
            val imageGrid = ImageGrid(8)

            val imageSize = imageGrid.imageSize
            val backgroundColor = Color.Cyan

            Actions.createNewImage(imageGrid, backgroundColor, GridCoords.from(3, 4))
            Actions.createNewImage(imageGrid, backgroundColor, GridCoords.from(3, 4))

            val expectedImage = RegularImage.fill(imageSize, imageSize, backgroundColor)

            val cell = imageGrid.apply(GridCoords.from(3, 4))!!
            val actualImage = cell.storage.toRegularImage(SimpleStorageFormat)

            assertEquals(expectedImage, actualImage)
        }
    }
}