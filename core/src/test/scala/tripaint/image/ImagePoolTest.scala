package tripaint.image

import munit.FunSuite
import tripaint.color.Color
import tripaint.image.ImagePool.SaveLocation
import tripaint.image.format.{SimpleStorageFormat, StorageFormat}

class ImagePoolTest extends FunSuite {
  val storageFormat: StorageFormat = SimpleStorageFormat

  test("locationOf should return None if the image doesn't exist") {
    val image = ImageStorage.fill(2, Color.Black)
    assertEquals(new ImagePool().locationOf(image), None)
  }

  test("locationOf should return the location of the image if it exists") {
    val image = ImageStorage.fill(2, Color.Black)
    val location = SaveLocation(null)
    val info = ImagePool.SaveInfo(null)

    val f = new ImagePool()
    f.set(image, location, info)

    assertEquals(f.locationOf(image), Some(location))
  }
}
