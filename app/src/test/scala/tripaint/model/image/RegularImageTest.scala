package tripaint.model.image

import tripaint.Color
import tripaint.coords.StorageCoords
import tripaint.model.image.format.SimpleStorageFormat

import munit.FunSuite

import java.awt.image.BufferedImage
import java.io.File

class RegularImageTest extends FunSuite {
  test("ofDim should produce an image of correct size") {
    val image = RegularImage.ofSize(4, 5)
    assertEquals(image.width, 4)
    assertEquals(image.height, 5)
  }

  test("ofDim should fill the image with transparent black") {
    val image = RegularImage.ofSize(1, 2)
    assertEquals(image.getColor(0, 0), Color(0, 0, 0, 0))
    assertEquals(image.getColor(0, 1), Color(0, 0, 0, 0))
  }

  test("fromBufferedImage should produce an image of correct size") {
    val buf = new BufferedImage(2, 5, BufferedImage.TYPE_INT_RGB)
    val image = RegularImage.fromBufferedImage(buf)
    assertEquals(image.width, 2)
    assertEquals(image.height, 5)
  }

  test("fromBufferedImage should copy the contents") {
    val buf = new BufferedImage(2, 1, BufferedImage.TYPE_INT_RGB)
    buf.setRGB(0, 0, 0x123456)
    buf.setRGB(1, 0, 0x345678)

    val image = RegularImage.fromBufferedImage(buf)
    assertEquals(image.getColor(0, 0).toInt, 0xff123456)
    assertEquals(image.getColor(1, 0).toInt, 0xff345678)
  }

  test("fill should produce an image of correct size") {
    val image = RegularImage.fill(3, 5, Color.Red)
    assertEquals(image.width, 3)
    assertEquals(image.height, 5)
  }

  test("fill should fill the image with the color") {
    val image = RegularImage.fill(3, 5, Color.Red)
    assertEquals(image.getColor(0, 0), Color.Red)
    assertEquals(image.getColor(0, 4), Color.Red)
    assertEquals(image.getColor(2, 4), Color.Red)
    assertEquals(image.getColor(2, 0), Color.Red)
    assertEquals(image.getColor(1, 1), Color.Red)
  }

  test("getColor should return the color at the given location") {
    val image = RegularImage.ofSize(3, 2)
    image.setColor(2, 1, Color.Red)
    assertEquals(image.getColor(2, 1), Color.Red)
  }

  test("getColor should return transparent black for unset pixels") {
    val image = RegularImage.ofSize(3, 2)
    assertEquals(image.getColor(2, 1), Color(0, 0, 0, 0))
  }

  test("getColor should fail if the coords are out of bounds") {
    val image = RegularImage.ofSize(3, 2)

    intercept[IllegalArgumentException](image.getColor(-1, 0))
    intercept[IllegalArgumentException](image.getColor(0, -1))
    intercept[IllegalArgumentException](image.getColor(3, 0))
    intercept[IllegalArgumentException](image.getColor(0, 2))
  }

  test("setColor should fail if the coords are out of bounds") {
    val image = RegularImage.ofSize(3, 2)

    intercept[IllegalArgumentException](image.setColor(-1, 0, Color.Red))
    intercept[IllegalArgumentException](image.setColor(0, -1, Color.Red))
    intercept[IllegalArgumentException](image.setColor(3, 0, Color.Red))
    intercept[IllegalArgumentException](image.setColor(0, 2, Color.Red))
  }

  test("setColor should handle transparency") {
    val image = RegularImage.ofSize(3, 2)

    image.setColor(1, 0, Color.fromInt(0x12345678))
    assertEquals(image.getColor(1, 0).toInt, 0x12345678)
  }

  test("pasteImage should overwrite part of dest image with contents of src image") {
    val dest = RegularImage.ofSize(5, 6)

    val src = RegularImage.ofSize(3, 2)
    src.setColor(0, 0, Color.Red)
    src.setColor(2, 0, Color.Yellow)
    src.setColor(2, 1, Color.Blue)
    src.setColor(0, 1, Color.Cyan)

    dest.pasteImage(StorageCoords(0, 0), src)

    assertEquals(dest.getColor(0, 0), Color.Red)
    assertEquals(dest.getColor(2, 0), Color.Yellow)
    assertEquals(dest.getColor(2, 1), Color.Blue)
    assertEquals(dest.getColor(0, 1), Color.Cyan)
  }

  test("pasteImage should keep the content of the dest image outside of the paste area") {
    val dest = RegularImage.ofSize(5, 6)
    dest.setColor(3, 1, Color.Green)
    dest.setColor(1, 2, Color.Yellow)
    dest.setColor(3, 2, Color.Blue)

    val src = RegularImage.ofSize(3, 2)
    src.setColor(0, 0, Color.Red)
    src.setColor(2, 0, Color.Red)
    src.setColor(2, 1, Color.Red)
    src.setColor(0, 1, Color.Red)

    dest.pasteImage(StorageCoords(0, 0), src)

    assertEquals(dest.getColor(3, 1), Color.Green)
    assertEquals(dest.getColor(1, 2), Color.Yellow)
    assertEquals(dest.getColor(3, 2), Color.Blue)
  }

  test("pasteImage should paste with an offset") {
    val dest = RegularImage.ofSize(5, 6)
    dest.setColor(0, 0, Color.Blue)

    val src = RegularImage.ofSize(3, 2)
    src.setColor(0, 0, Color.Red)

    dest.pasteImage(StorageCoords(1, 2), src)

    assertEquals(dest.getColor(0, 0), Color.Blue)
    assertEquals(dest.getColor(1, 2), Color.Red)
  }

  test("pasteImage should work against the border") {
    val dest = RegularImage.ofSize(5, 6)
    dest.setColor(0, 0, Color.Blue)

    val src = RegularImage.ofSize(3, 2)
    src.setColor(0, 0, Color.Red)

    dest.pasteImage(StorageCoords(2, 4), src)

    assertEquals(dest.getColor(0, 0), Color.Blue)
    assertEquals(dest.getColor(2, 4), Color.Red)
  }

  test("pasteImage should fail if destination image has too small width") {
    val dest = RegularImage.ofSize(5, 6)
    val src = RegularImage.ofSize(3, 2)
    intercept[IllegalArgumentException] {
      dest.pasteImage(StorageCoords(3, 0), src)
    }
  }

  test("pasteImage should fail if destination image has too small height") {
    val dest = RegularImage.ofSize(5, 6)
    val src = RegularImage.ofSize(3, 2)
    intercept[IllegalArgumentException] {
      dest.pasteImage(StorageCoords(0, 5), src)
    }
  }

  test("toBufferedImage should produce an image with the same size") {
    val image = RegularImage.ofSize(3, 2)
    val buf = image.toBufferedImage
    assertEquals(buf.getWidth(), 3)
    assertEquals(buf.getHeight(), 2)
  }

  test("toBufferedImage should be correct for simple colors") {
    val image = RegularImage.ofSize(3, 2)
    image.setColor(0, 0, Color.Red)
    image.setColor(0, 1, Color.Green)
    image.setColor(1, 0, Color.Blue)
    image.setColor(1, 1, Color.Magenta)
    image.setColor(2, 0, Color.Yellow)
    image.setColor(2, 1, Color.Blue)

    val buf = image.toBufferedImage
    assertEquals(Color.fromInt(buf.getRGB(0, 0)), Color.Red)
    assertEquals(Color.fromInt(buf.getRGB(0, 1)), Color.Green)
    assertEquals(Color.fromInt(buf.getRGB(1, 0)), Color.Blue)
    assertEquals(Color.fromInt(buf.getRGB(1, 1)), Color.Magenta)
    assertEquals(Color.fromInt(buf.getRGB(2, 0)), Color.Yellow)
    assertEquals(Color.fromInt(buf.getRGB(2, 1)), Color.Blue)
  }

  test("toBufferedImage should ignore transparency") {
    val image = RegularImage.ofSize(3, 2)
    image.setColor(0, 0, Color.fromInt(0x00123456))
    image.setColor(0, 1, Color.fromInt(0x45123456))

    assertEquals(image.toBufferedImage.getRGB(0, 0), 0xff123456)
    assertEquals(image.toBufferedImage.getRGB(0, 1), 0xff123456)
  }

  test("toBufferedImage should produce black for unset pixels") {
    val image = RegularImage.ofSize(3, 2)
    assertEquals(image.toBufferedImage.getRGB(0, 0), 0xff000000)
  }

  test("equals should be false if sizes are different") {
    val image1 = RegularImage.ofSize(3, 2)
    val image2 = RegularImage.ofSize(3, 3)
    assertEquals(image1 == image2, false)
  }

  test("equals should be true for new images of same size") {
    val image1 = RegularImage.ofSize(3, 2)
    val image2 = RegularImage.ofSize(3, 2)
    assertEquals(image1 == image2, true)
  }

  test("equals should be false for different images of same size") {
    val image1 = RegularImage.ofSize(3, 2)
    val image2 = RegularImage.ofSize(3, 2)
    image2.setColor(1, 0, Color.Red)
    assertEquals(image1 == image2, false)
  }

  test("equals should check the entire image") {
    val image1 = RegularImage.ofSize(3, 2)
    val image2 = RegularImage.ofSize(3, 2)
    image2.setColor(2, 1, Color.Red)
    assertEquals(image1 == image2, false)
  }

  test("overwritePartOfImage should write image if it doesn't exist") {
    val file = new File("file.png")
    val location = ImagePool.SaveLocation(file)
    val format = SimpleStorageFormat

    val storage = ImageStorage.fill(16, Color.Blue)
    storage.setColor(format.reverse(StorageCoords(2, 3)), Color.Cyan)
    storage.setColor(format.reverse(StorageCoords(15, 0)), Color.Magenta)
    storage.setColor(format.reverse(StorageCoords(15, 15)), Color.Yellow)

    val expectedImage = RegularImage.fill(storage.imageSize, storage.imageSize, Color.Blue)
    expectedImage.pasteImage(StorageCoords(0, 0), storage.toRegularImage(format))

    val storedImage =
      RegularImage.fromBaseAndOverlay(None, storage.toRegularImage(format), location.offset)

    assertEquals(storedImage, expectedImage)
  }

  test("overwritePartOfImage should overwrite part of image if it already exists") {
    val offset = StorageCoords(2, 3)
    val format = SimpleStorageFormat

    val existingStorage = ImageStorage.fill(8, Color.Yellow)

    val existingImage = RegularImage.ofSize(8, 8) // ???
    existingImage.pasteImage(StorageCoords(0, 0), existingStorage.toRegularImage(format))

    val image = ImageStorage.fill(4, Color.Cyan)

    val storedImage =
      RegularImage.fromBaseAndOverlay(Some(existingImage), image.toRegularImage(format), offset)

    val expectedImage = RegularImage.fill(8, 8, Color.Yellow)
    expectedImage.pasteImage(offset, RegularImage.fill(4, 4, Color.Cyan))

    assertEquals(storedImage, expectedImage)
  }
}
