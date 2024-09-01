package tripaint.image

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import tripaint.color.Color
import tripaint.coords.StorageCoords
import tripaint.image.format.SimpleStorageFormat
import java.awt.image.BufferedImage
import java.io.File
import kotlin.test.Test

class RegularImageTest {
    @Test
    fun `ofDim should produce an image of correct size`() {
        val image = RegularImage.ofSize(4, 5)
        assertEquals(4, image.width)
        assertEquals(5, image.height)
    }
    
    @Test
    fun `ofDim should fill the image with transparent black`() {
        val image = RegularImage.ofSize(1, 2)
        assertEquals(Color(0.0, 0.0, 0.0, 0.0), image.getColor(0, 0))
        assertEquals(Color(0.0, 0.0, 0.0, 0.0), image.getColor(0, 1))
    }

    @Test
    fun `fromBufferedImage should produce an image of correct size`() {
        val buf = BufferedImage(2, 5, BufferedImage.TYPE_INT_RGB)
        val image = RegularImage.fromBufferedImage(buf)
        assertEquals(2, image.width)
        assertEquals(5, image.height)
    }

    @Test
    fun `fromBufferedImage should copy the contents`() {
        val buf = BufferedImage(2, 1, BufferedImage.TYPE_INT_RGB)
        buf.setRGB(0, 0, 0x123456)
        buf.setRGB(1, 0, 0x345678)

        val image = RegularImage.fromBufferedImage(buf)
        assertEquals(0xff123456.toInt(), image.getColor(0, 0).toInt())
        assertEquals(0xff345678.toInt(), image.getColor(1, 0).toInt())
    }

    @Test
    fun `fill should produce an image of correct size`() {
        val image = RegularImage.fill(3, 5, Color.Red)
        assertEquals(3, image.width)
        assertEquals(5, image.height)
    }

    @Test
    fun `fill should fill the image with the color`() {
        val image = RegularImage.fill(3, 5, Color.Red)
        assertEquals(Color.Red, image.getColor(0, 0))
        assertEquals(Color.Red, image.getColor(0, 4))
        assertEquals(Color.Red, image.getColor(2, 4))
        assertEquals(Color.Red, image.getColor(2, 0))
        assertEquals(Color.Red, image.getColor(1, 1))
    }

    @Test
    fun `getColor should return the color at the given location`() {
        val image = RegularImage.ofSize(3, 2)
        image.setColor(2, 1, Color.Red)
        assertEquals(Color.Red, image.getColor(2, 1))
    }

    @Test
    fun `getColor should return transparent black for unset pixels`() {
        val image = RegularImage.ofSize(3, 2)
        assertEquals(Color(0.0, 0.0, 0.0, 0.0), image.getColor(2, 1))
    }

    @Test
    fun `getColor should fail if the coords are out of bounds`() {
        val image = RegularImage.ofSize(3, 2)

        assertThrows<IllegalArgumentException> { image.getColor(-1, 0) }
        assertThrows<IllegalArgumentException> { image.getColor(0, -1) }
        assertThrows<IllegalArgumentException> { image.getColor(3, 0) }
        assertThrows<IllegalArgumentException> { image.getColor(0, 2) }
    }

    @Test
    fun `setColor should fail if the coords are out of bounds`() {
        val image = RegularImage.ofSize(3, 2)

        assertThrows<IllegalArgumentException> { image.setColor(-1, 0, Color.Red) }
        assertThrows<IllegalArgumentException> { image.setColor(0, -1, Color.Red) }
        assertThrows<IllegalArgumentException> { image.setColor(3, 0, Color.Red) }
        assertThrows<IllegalArgumentException> { image.setColor(0, 2, Color.Red) }
    }

    @Test
    fun `setColor should handle transparency`() {
        val image = RegularImage.ofSize(3, 2)

        image.setColor(1, 0, Color.fromInt(0x12345678))
        assertEquals(image.getColor(1, 0).toInt(), 0x12345678)
    }

    @Test
    fun `pasteImage should overwrite part of dest image with contents of src image`() {
        val dest = RegularImage.ofSize(5, 6)

        val src = RegularImage.ofSize(3, 2)
        src.setColor(0, 0, Color.Red)
        src.setColor(2, 0, Color.Yellow)
        src.setColor(2, 1, Color.Blue)
        src.setColor(0, 1, Color.Cyan)

        dest.pasteImage(StorageCoords.from(0, 0), src)

        assertEquals(Color.Red, dest.getColor(0, 0))
        assertEquals(Color.Yellow, dest.getColor(2, 0))
        assertEquals(Color.Blue, dest.getColor(2, 1))
        assertEquals(Color.Cyan, dest.getColor(0, 1))
    }

    @Test
    fun `pasteImage should keep the content of the dest image outside of the paste area`() {
        val dest = RegularImage.ofSize(5, 6)
        dest.setColor(3, 1, Color.Green)
        dest.setColor(1, 2, Color.Yellow)
        dest.setColor(3, 2, Color.Blue)

        val src = RegularImage.ofSize(3, 2)
        src.setColor(0, 0, Color.Red)
        src.setColor(2, 0, Color.Red)
        src.setColor(2, 1, Color.Red)
        src.setColor(0, 1, Color.Red)

        dest.pasteImage(StorageCoords.from(0, 0), src)

        assertEquals(Color.Green, dest.getColor(3, 1))
        assertEquals(Color.Yellow, dest.getColor(1, 2))
        assertEquals(Color.Blue, dest.getColor(3, 2))
    }

    @Test
    fun `pasteImage should paste with an offset`() {
        val dest = RegularImage.ofSize(5, 6)
        dest.setColor(0, 0, Color.Blue)

        val src = RegularImage.ofSize(3, 2)
        src.setColor(0, 0, Color.Red)

        dest.pasteImage(StorageCoords.from(1, 2), src)

        assertEquals(Color.Blue, dest.getColor(0, 0))
        assertEquals(Color.Red, dest.getColor(1, 2))
    }

    @Test
    fun `pasteImage should work against the border`() {
        val dest = RegularImage.ofSize(5, 6)
        dest.setColor(0, 0, Color.Blue)

        val src = RegularImage.ofSize(3, 2)
        src.setColor(0, 0, Color.Red)

        dest.pasteImage(StorageCoords.from(2, 4), src)

        assertEquals(Color.Blue, dest.getColor(0, 0))
        assertEquals(Color.Red, dest.getColor(2, 4))
    }

    @Test
    fun `pasteImage should fail if destination image has too small width`() {
        val dest = RegularImage.ofSize(5, 6)
        val src = RegularImage.ofSize(3, 2)
        assertThrows<IllegalArgumentException>  {
            dest.pasteImage(StorageCoords.from(3, 0), src)
        }
    }

    @Test
    fun `pasteImage should fail if destination image has too small height`() {
        val dest = RegularImage.ofSize(5, 6)
        val src = RegularImage.ofSize(3, 2)
        assertThrows<IllegalArgumentException>  {
            dest.pasteImage(StorageCoords.from(0, 5), src)
        }
    }

    @Test
    fun `toBufferedImage should produce an image with the same size`() {
        val image = RegularImage.ofSize(3, 2)
        val buf = image.toBufferedImage()
        assertEquals(3, buf.width)
        assertEquals(2, buf.height)
    }

    @Test
    fun `toBufferedImage should be correct for simple colors`() {
        val image = RegularImage.ofSize(3, 2)
        image.setColor(0, 0, Color.Red)
        image.setColor(0, 1, Color.Green)
        image.setColor(1, 0, Color.Blue)
        image.setColor(1, 1, Color.Magenta)
        image.setColor(2, 0, Color.Yellow)
        image.setColor(2, 1, Color.Blue)

        val buf = image.toBufferedImage()
        assertEquals(Color.Red, Color.fromInt(buf.getRGB(0, 0)))
        assertEquals(Color.Green, Color.fromInt(buf.getRGB(0, 1)))
        assertEquals(Color.Blue, Color.fromInt(buf.getRGB(1, 0)))
        assertEquals(Color.Magenta, Color.fromInt(buf.getRGB(1, 1)))
        assertEquals(Color.Yellow, Color.fromInt(buf.getRGB(2, 0)))
        assertEquals(Color.Blue, Color.fromInt(buf.getRGB(2, 1)))
    }

    @Test
    fun `toBufferedImage should ignore transparency`() {
        val image = RegularImage.ofSize(3, 2)
        image.setColor(0, 0, Color.fromInt(0x00123456))
        image.setColor(0, 1, Color.fromInt(0x45123456))

        assertEquals(0xff123456.toInt(), image.toBufferedImage().getRGB(0, 0))
        assertEquals(0xff123456.toInt(), image.toBufferedImage().getRGB(0, 1))
    }

    @Test
    fun `toBufferedImage should produce black for unset pixels`() {
        val image = RegularImage.ofSize(3, 2)
        assertEquals(0xff000000.toInt(), image.toBufferedImage().getRGB(0, 0))
    }

    @Test
    fun `equals should be false if sizes are different`() {
        val image1 = RegularImage.ofSize(3, 2)
        val image2 = RegularImage.ofSize(3, 3)
        assertEquals(false, image1 == image2)
    }

    @Test
    fun `equals should be true for new images of same size`() {
        val image1 = RegularImage.ofSize(3, 2)
        val image2 = RegularImage.ofSize(3, 2)
        assertEquals(true, image1 == image2)
    }

    @Test
    fun `equals should be false for different images of same size`() {
        val image1 = RegularImage.ofSize(3, 2)
        val image2 = RegularImage.ofSize(3, 2)
        image2.setColor(1, 0, Color.Red)
        assertEquals(false, image1 == image2)
    }

    @Test
    fun `equals should check the entire image`() {
        val image1 = RegularImage.ofSize(3, 2)
        val image2 = RegularImage.ofSize(3, 2)
        image2.setColor(2, 1, Color.Red)
        assertEquals(false, image1 == image2)
    }

    @Test
    fun `overwritePartOfImage should write image if it doesn't exist`() {
        val file = File("file.png")
        val offset = StorageCoords.from(0, 0)
        val format = SimpleStorageFormat

        val storage = ImageStorage.fill(16, Color.Blue)
        storage.setColor(format.reverse(StorageCoords.from(2, 3)), Color.Cyan)
        storage.setColor(format.reverse(StorageCoords.from(15, 0)), Color.Magenta)
        storage.setColor(format.reverse(StorageCoords.from(15, 15)), Color.Yellow)

        val expectedImage = RegularImage.fill(storage.imageSize, storage.imageSize, Color.Blue)
        expectedImage.pasteImage(StorageCoords.from(0, 0), storage.toRegularImage(format))

        val storedImage =
            RegularImage.fromBaseAndOverlay(null, storage.toRegularImage(format), offset)

        assertEquals(expectedImage, storedImage)
    }

    @Test
    fun `overwritePartOfImage should overwrite part of image if it already exists`() {
        val offset = StorageCoords.from(2, 3)
        val format = SimpleStorageFormat

        val existingStorage = ImageStorage.fill(8, Color.Yellow)

        val existingImage = RegularImage.ofSize(8, 8) // ???
        existingImage.pasteImage(StorageCoords.from(0, 0), existingStorage.toRegularImage(format))

        val image = ImageStorage.fill(4, Color.Cyan)

        val storedImage =
            RegularImage.fromBaseAndOverlay(existingImage, image.toRegularImage(format), offset)

        val expectedImage = RegularImage.fill(8, 8, Color.Yellow)
        expectedImage.pasteImage(offset, RegularImage.fill(4, 4, Color.Cyan))

        assertEquals(expectedImage, storedImage)
    }
}