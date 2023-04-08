package com.martomate.tripaint.model.image.pool

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.StorageCoords
import com.martomate.tripaint.model.image.format.{SimpleStorageFormat, StorageFormat}
import com.martomate.tripaint.model.image.ImagePool.SaveLocation
import com.martomate.tripaint.model.image.{
  ImagePool,
  ImageSaveCollisionHandler,
  ImageStorage,
  RegularImage,
  pool
}
import com.martomate.tripaint.util.Tracker
import munit.FunSuite
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import scalafx.scene.paint.Color as FXColor

import java.io.File
import scala.util.{Failure, Success}

class ImagePoolTest extends FunSuite with MockitoSugar {
  implicit val collisionHandler: ImageSaveCollisionHandler = mock[ImageSaveCollisionHandler]
  val storageFormat: StorageFormat = new SimpleStorageFormat

  test("save should return false if the image doesn't exist") {
    val image = ImageStorage.fromBGColor(Color.Black, 2)
    assertEquals(new ImagePool().save(image, null), false)
  }

  test("save should return false if the saver reports failure") {
    val image = ImageStorage.fromBGColor(Color.Black, 2)
    val location = SaveLocation(new File("a.png"))
    val format = new SimpleStorageFormat
    val info = ImagePool.SaveInfo(format)

    val f = new ImagePool()
    f.move(image, location, info)

    val fs = FileSystem.createNull(new FileSystem.NullArgs(supportedImageFormats = Set()))
    assertEquals(f.save(image, fs), false)
  }

  test("save should notify listeners and return true if the saver reports success") {
    val tracker = Tracker.withStorage[ImagePool.Event]
    val image = ImageStorage.fromBGColor(Color.Black, 2)
    val location = SaveLocation(new File("a.png"))
    val format = new SimpleStorageFormat
    val info = ImagePool.SaveInfo(format)

    val f = new ImagePool()
    f.trackChanges(tracker)
    f.move(image, location, info)

    assertEquals(f.save(image, FileSystem.createNull()), true)
    assertEquals(tracker.events, Seq(ImagePool.Event.ImageSaved(image)))
  }

  test("save should write image if it does not exist") {
    val image = ImageStorage.fromBGColor(Color.Blue, 2)
    val path = "a.png"
    val location = SaveLocation(new File(path))
    val format = new SimpleStorageFormat
    val info = ImagePool.SaveInfo(format)

    val imagePool = new ImagePool()
    imagePool.move(image, location, info)

    val fs = FileSystem.createNull()
    val tracker = Tracker.withStorage[FileSystem.Event]
    fs.trackChanges(tracker)

    imagePool.save(image, fs)

    assertEquals(
      tracker.events,
      Seq(
        FileSystem.Event.ImageWritten(image.toRegularImage(format), new File(path))
      )
    )
  }

  test("save should overwrite image if it exists and has the same size") {
    val image = ImageStorage.fromBGColor(Color.Blue, 2)
    val path = "a.png"
    val location = SaveLocation(new File(path))
    val format = new SimpleStorageFormat
    val info = ImagePool.SaveInfo(format)

    val imagePool = new ImagePool()
    imagePool.move(image, location, info)

    val existingImage = RegularImage.fill(2, 2, Color.Red)
    val fs = FileSystem.createNull(
      new FileSystem.NullArgs(initialImages = Map(new File(path) -> existingImage))
    )
    val tracker = Tracker.withStorage[FileSystem.Event]
    fs.trackChanges(tracker)

    imagePool.save(image, fs)

    assertEquals(
      tracker.events,
      Seq(
        FileSystem.Event.ImageWritten(image.toRegularImage(format), new File(path))
      )
    )
  }

  test("save should overwrite part of image if there already exists a bigger image") {
    val image = ImageStorage.fromBGColor(Color.Blue, 2)
    val path = "a.png"
    val offset = StorageCoords(1, 2)
    val location = SaveLocation(new File(path), offset)
    val format = new SimpleStorageFormat
    val info = ImagePool.SaveInfo(format)

    val imagePool = new ImagePool()
    imagePool.move(image, location, info)

    val existingImage = RegularImage.fill(3, 5, Color.Red)
    val fs = FileSystem.createNull(
      new FileSystem.NullArgs(initialImages = Map(new File(path) -> existingImage))
    )
    val tracker = Tracker.withStorage[FileSystem.Event]
    fs.trackChanges(tracker)

    imagePool.save(image, fs)

    val expectedImage = RegularImage.fill(3, 5, Color.Red)
    expectedImage.pasteImage(offset, RegularImage.fill(2, 2, Color.Blue))

    assertEquals(tracker.events, Seq(FileSystem.Event.ImageWritten(expectedImage, new File(path))))
  }

  test(
    "save should overwrite part of image if there already exists an image even if it is too small"
  ) {
    val image = ImageStorage.fromBGColor(Color.Blue, 2)
    val path = "a.png"
    val offset = StorageCoords(1, 2)
    val location = SaveLocation(new File(path), offset)
    val format = new SimpleStorageFormat
    val info = ImagePool.SaveInfo(format)

    val imagePool = new ImagePool()
    imagePool.move(image, location, info)

    val existingImage = RegularImage.fill(3, 2, Color.Red)
    val fs = FileSystem.createNull(
      new FileSystem.NullArgs(initialImages = Map(new File(path) -> existingImage))
    )
    val tracker = Tracker.withStorage[FileSystem.Event]
    fs.trackChanges(tracker)

    imagePool.save(image, fs)

    val expectedImage = RegularImage.ofSize(3, 4)
    expectedImage.pasteImage(StorageCoords(0, 0), RegularImage.fill(3, 2, Color.Red))
    expectedImage.pasteImage(offset, RegularImage.fill(2, 2, Color.Blue))

    assertEquals(tracker.events, Seq(FileSystem.Event.ImageWritten(expectedImage, new File(path))))
  }

  test("locationOf should return None if the image doesn't exist") {
    val image = ImageStorage.fromBGColor(Color.Black, 2)
    assertEquals(new ImagePool().locationOf(image), None)
  }

  test("locationOf should return the location of the image if it exists") {
    val image = ImageStorage.fromBGColor(Color.Black, 2)
    val location = SaveLocation(null)
    val info = ImagePool.SaveInfo(null)

    val f = new ImagePool()
    f.move(image, location, info)

    assertEquals(f.locationOf(image), Some(location))
  }

  test("fromFile should return the image at that location if it exists") {
    val image = ImageStorage.fromBGColor(FXColor.Blue, 16)
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

    val image = ImageStorage.fromBGColor(FXColor.Orange, imageSize)
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

    val image = ImageStorage.fromBGColor(FXColor.Orange, imageSize)
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
    val image = ImageStorage.fromBGColor(Color.Blue, 8)
    val location = SaveLocation(null)
    val info = ImagePool.SaveInfo(null)
    assertEquals(p.move(image, location, info), true)
    assertEquals(p.locationOf(image), Some(location))
  }

  test("move should simply return true if the image is already there") {
    val p = new ImagePool()
    val image = ImageStorage.fromBGColor(Color.Blue, 8)
    val location = SaveLocation(null)
    val info = ImagePool.SaveInfo(null)
    p.move(image, location, info)
    assertEquals(p.move(image, location, info), true)
    assertEquals(p.locationOf(image), Some(location))
  }

  test("move should return false if the handler doesn't accept the collision") {
    val handler = collisionHandler
    val p = new ImagePool()
    val currentImage = ImageStorage.fromBGColor(Color.Blue, 8)
    val newImage = ImageStorage.fromBGColor(Color.Yellow, 8)
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
    val currentImage = ImageStorage.fromBGColor(Color.Blue, 8)
    val newImage = ImageStorage.fromBGColor(Color.Yellow, 8)
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
    val currentImage = ImageStorage.fromBGColor(Color.Blue, 8)
    val newImage = ImageStorage.fromBGColor(Color.Yellow, 8)
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
