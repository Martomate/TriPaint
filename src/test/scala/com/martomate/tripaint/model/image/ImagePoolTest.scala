package com.martomate.tripaint.model.image

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model
import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.StorageCoords
import com.martomate.tripaint.model.image.ImagePool.SaveLocation
import com.martomate.tripaint.model.image.format.{SimpleStorageFormat, StorageFormat}
import com.martomate.tripaint.util.Tracker
import munit.FunSuite
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import scalafx.scene.paint.Color as FXColor

import java.io.File
import scala.util.{Failure, Success}

class ImagePoolTest extends FunSuite with MockitoSugar {
  implicit val collisionHandler: ImageSaveCollisionHandler = mock[ImageSaveCollisionHandler]
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
    f.move(image, location, info)

    assertEquals(f.locationOf(image), Some(location))
  }

  test("fromFile should return the image at that location if it exists") {
    val image = ImageStorage.fill(16, FXColor.Blue)
    val location = SaveLocation(null)
    val info = ImagePool.SaveInfo(null)

    val f = new ImagePool()
    f.move(image, location, info)

    assertEquals(f.fromFile(location, storageFormat, 16, FileSystem.createNull()), Success(image))
  }

  test("fromFile should return Failure if there is no image there and the loading failed") {
    val location = SaveLocation(null)
    val imageSize = 16
    val fs = FileSystem.createNull(new FileSystem.NullArgs(initialImages = Map.empty))

    val pool = new ImagePool()

    assertEquals(pool.fromFile(location, storageFormat, imageSize, fs).isFailure, true)
  }

  test("fromFile should save and return the newly loaded image if there was none already") {
    val file = new File("path/to/image.png")
    val location = SaveLocation(file)
    val imageSize = 16

    val image = ImageStorage.fill(imageSize, FXColor.Orange)
    val regularImage = image.toRegularImage(storageFormat)
    val fs = FileSystem.createNull(
      new FileSystem.NullArgs(initialImages = Map(file -> regularImage))
    )

    val pool = new ImagePool()

    pool.fromFile(location, storageFormat, imageSize, fs) match {
      case Success(actualImage) =>
        assertEquals(actualImage.toRegularImage(storageFormat), regularImage)
        assertEquals(pool.locationOf(actualImage), Some(location))
      case Failure(_) => fail("The image pool failed to load the image from file")
    }
  }

  test("fromFile should load image with offset") {
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

    pool.fromFile(location, storageFormat, imageSize, fs) match {
      case Success(actualImage) =>
        assertEquals(actualImage.toRegularImage(storageFormat), regularImage)
        assertEquals(pool.locationOf(actualImage), Some(location))
      case Failure(_) => fail("The image pool failed to load the image from file")
    }
  }

  test("move should set the image and return true if the location is empty") {
    val p = new ImagePool()
    val image = ImageStorage.fill(8, Color.Blue)
    val location = SaveLocation(null)
    val info = ImagePool.SaveInfo(null)
    assertEquals(p.move(image, location, info), true)
    assertEquals(p.locationOf(image), Some(location))
  }

  test("move should simply return true if the image is already there") {
    val p = new ImagePool()
    val image = ImageStorage.fill(8, Color.Blue)
    val location = SaveLocation(null)
    val info = ImagePool.SaveInfo(null)
    p.move(image, location, info)
    assertEquals(p.move(image, location, info), true)
    assertEquals(p.locationOf(image), Some(location))
  }

  test("move should return false if the handler doesn't accept the collision") {
    val handler = collisionHandler
    val p = new ImagePool()
    val currentImage = ImageStorage.fill(8, Color.Blue)
    val newImage = ImageStorage.fill(8, Color.Yellow)
    val location = SaveLocation(null)
    val info = ImagePool.SaveInfo(null)

    when(handler.shouldReplaceImage(currentImage, newImage, location)).thenReturn(None)

    p.move(currentImage, location, info)
    assertEquals(p.move(newImage, location, info), false)
  }

  test(
    "move should replace the current image, notify listeners, and return true if the handler wants to replace it"
  ) {
    val handler = collisionHandler
    val tracker = Tracker.withStorage[ImagePool.Event]

    val p = new ImagePool()
    p.trackChanges(tracker)
    val currentImage = ImageStorage.fill(8, Color.Blue)
    val newImage = ImageStorage.fill(8, Color.Yellow)
    val location = SaveLocation(null)
    val info = ImagePool.SaveInfo(null)

    when(handler.shouldReplaceImage(currentImage, newImage, location)).thenReturn(Some(true))

    p.move(currentImage, location, info)
    assertEquals(p.move(newImage, location, info), true)
    assertEquals(p.locationOf(currentImage), None)
    assertEquals(p.locationOf(newImage), Some(location))

    assertEquals(
      tracker.events,
      Seq(ImagePool.Event.ImageReplaced(currentImage, newImage, location))
    )
  }

  test(
    "move should keep the current image, notify listeners, and return true if the handler wants to keep it"
  ) {
    val handler = collisionHandler
    val tracker = Tracker.withStorage[ImagePool.Event]

    val p = new ImagePool()
    p.trackChanges(tracker)
    val currentImage = ImageStorage.fill(8, Color.Blue)
    val newImage = ImageStorage.fill(8, Color.Yellow)
    val location = SaveLocation(null)
    val info = ImagePool.SaveInfo(null)

    when(handler.shouldReplaceImage(currentImage, newImage, location)).thenReturn(Some(false))

    p.move(currentImage, location, info)
    assertEquals(p.move(newImage, location, info), true)
    assertEquals(p.locationOf(currentImage), Some(location))
    assertEquals(p.locationOf(newImage), None)

    assertEquals(
      tracker.events,
      Seq(ImagePool.Event.ImageReplaced(newImage, currentImage, location))
    )
  }
}
