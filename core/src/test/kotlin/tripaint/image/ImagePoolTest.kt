package tripaint.image

import org.junit.jupiter.api.Assertions.assertEquals
import tripaint.color.Color
import tripaint.image.format.SimpleStorageFormat
import tripaint.image.format.StorageFormat
import java.io.File
import kotlin.test.Test

class ImagePoolTest {
    val storageFormat: StorageFormat = SimpleStorageFormat

    @Test
    fun `locationOf should return None if the image doesn't exist`() {
        val image = ImageStorage.fill(2, Color.Black)
        assertEquals(null, ImagePool().locationOf(image))
    }

    @Test
    fun `locationOf should return the location of the image if it exists`() {
        val image = ImageStorage.fill(2, Color.Black)
        val location = ImagePool.SaveLocation(File(""))
        val info = ImagePool.SaveInfo(storageFormat)

        val f = ImagePool()
        f.set(image, location, info)

        assertEquals(location, f.locationOf(image))
    }
}