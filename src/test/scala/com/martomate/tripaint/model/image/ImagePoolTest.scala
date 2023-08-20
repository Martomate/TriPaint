package com.martomate.tripaint.model.image

import com.martomate.tripaint.control.Actions
import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model
import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.StorageCoords
import com.martomate.tripaint.model.image.ImagePool.SaveLocation
import com.martomate.tripaint.model.image.format.{SimpleStorageFormat, StorageFormat}

import munit.FunSuite
import scalafx.scene.paint.Color as FXColor

import java.io.File
import scala.util.{Failure, Success}

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

  test(
    "Actions.loadFromFileIntoPool should return Failure if there is no image there and the loading failed"
  ) {
    val location = SaveLocation(null)
    val imageSize = 16
    val fs = FileSystem.createNull(new FileSystem.NullArgs(initialImages = Map.empty))

    val pool = new ImagePool()

    assertEquals(
      Actions.loadFromFileIntoPool(pool, location, storageFormat, imageSize, fs).isFailure,
      true
    )
  }

  test(
    "Actions.loadFromFileIntoPool should save and return the newly loaded image if there was none already"
  ) {
    val file = new File("path/to/image.png")
    val location = SaveLocation(file)
    val imageSize = 16

    val image = ImageStorage.fill(imageSize, FXColor.Orange)
    val regularImage = image.toRegularImage(storageFormat)
    val fs = FileSystem.createNull(
      new FileSystem.NullArgs(initialImages = Map(file -> regularImage))
    )

    val pool = new ImagePool()

    Actions.loadFromFileIntoPool(pool, location, storageFormat, imageSize, fs) match {
      case Success(actualImage) =>
        assertEquals(actualImage.toRegularImage(storageFormat), regularImage)
        assertEquals(pool.locationOf(actualImage), Some(location))
      case Failure(_) => fail("The image pool failed to load the image from file")
    }
  }

  test("Actions.loadFromFileIntoPool should load image with offset") {
    val file = new File("path/to/image.png")
    val offset = StorageCoords(2, 3)
    val location = SaveLocation(file, offset)
    val imageSize = 16

    val image = ImageStorage.fill(imageSize, FXColor.Orange)
    val regularImage = image.toRegularImage(storageFormat)

    val storedImage = RegularImage.ofSize(imageSize + offset.x, imageSize + offset.y)
    storedImage.pasteImage(offset, regularImage)
    val fs = FileSystem.createNull(
      new FileSystem.NullArgs(initialImages = Map(file -> storedImage))
    )

    val pool = new ImagePool()

    Actions.loadFromFileIntoPool(pool, location, storageFormat, imageSize, fs) match {
      case Success(actualImage) =>
        assertEquals(actualImage.toRegularImage(storageFormat), regularImage)
        assertEquals(pool.locationOf(actualImage), Some(location))
      case Failure(_) => fail("The image pool failed to load the image from file")
    }
  }
}
