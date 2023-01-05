package com.martomate.tripaint.model.image.pool

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.StorageCoords
import com.martomate.tripaint.model.image.format.{SimpleStorageFormat, StorageFormat}
import com.martomate.tripaint.model.image.save.ImageSaverToFile
import com.martomate.tripaint.model.image.storage.ImageStorage
import com.martomate.tripaint.model.image.{RegularImage, SaveLocation, pool}
import com.martomate.tripaint.util.Tracker
import org.mockito.Mockito.{verify, when}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import scalafx.scene.paint.Color as FXColor

import java.io.File
import scala.util.{Failure, Success}

class ImagePoolTest extends AnyFlatSpec with Matchers with MockitoSugar {
  implicit val collisionHandler: ImageSaveCollisionHandler = mock[ImageSaveCollisionHandler]
  val storageFormat: StorageFormat = new SimpleStorageFormat

  "save" should "return false if the image doesn't exist" in {
    val image = ImageStorage.fromBGColor(Color.Black, 2)
    new ImagePool().save(image, null) shouldBe false
  }

  it should "return false if the saver reports failure" in {
    val image = ImageStorage.fromBGColor(Color.Black, 2)
    val location = SaveLocation(new File("a.png"))
    val format = new SimpleStorageFormat
    val info = pool.SaveInfo(format)

    val f = new ImagePool()
    f.move(image, location, info)

    val fs = FileSystem.createNull(new FileSystem.NullArgs(supportedImageFormats = Set()))
    f.save(image, fs) shouldBe false
  }

  it should "notify listeners and return true if the saver reports success" in {
    val listener = mock[ImagePoolListener]
    val image = ImageStorage.fromBGColor(Color.Black, 2)
    val location = SaveLocation(new File("a.png"))
    val format = new SimpleStorageFormat
    val info = pool.SaveInfo(format)

    val f = new ImagePool()
    f.addListener(listener)
    f.move(image, location, info)

    f.save(image, FileSystem.createNull()) shouldBe true
    verify(listener).onImageSaved(image)
  }

  it should "write image if it does not exist" in {
    val image = ImageStorage.fromBGColor(Color.Blue, 2)
    val path = "a.png"
    val location = SaveLocation(new File(path))
    val format = new SimpleStorageFormat
    val info = pool.SaveInfo(format)

    val imagePool = new ImagePool()
    imagePool.move(image, location, info)

    val fs = FileSystem.createNull()
    val tracker = Tracker.withStorage[FileSystem.Event]
    fs.trackChanges(tracker)

    imagePool.save(image, fs)

    tracker.events shouldBe Seq(
      FileSystem.Event.ImageWritten(image.toRegularImage(format), new File(path))
    )
  }

  it should "overwrite image if it exists and has the same size" in {
    val image = ImageStorage.fromBGColor(Color.Blue, 2)
    val path = "a.png"
    val location = SaveLocation(new File(path))
    val format = new SimpleStorageFormat
    val info = pool.SaveInfo(format)

    val imagePool = new ImagePool()
    imagePool.move(image, location, info)

    val existingImage = RegularImage.fill(2, 2, Color.Red)
    val fs = FileSystem.createNull(
      new FileSystem.NullArgs(initialImages = Map(new File(path) -> existingImage))
    )
    val tracker = Tracker.withStorage[FileSystem.Event]
    fs.trackChanges(tracker)

    imagePool.save(image, fs)

    tracker.events shouldBe Seq(
      FileSystem.Event.ImageWritten(image.toRegularImage(format), new File(path))
    )
  }

  it should "overwrite part of image if there already exists a bigger image" in {
    val image = ImageStorage.fromBGColor(Color.Blue, 2)
    val path = "a.png"
    val offset = StorageCoords(1, 2)
    val location = SaveLocation(new File(path), offset)
    val format = new SimpleStorageFormat
    val info = pool.SaveInfo(format)

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

    tracker.events shouldBe Seq(FileSystem.Event.ImageWritten(expectedImage, new File(path)))
  }

  it should "overwrite part of image if there already exists an image even if it is too small" in {
    val image = ImageStorage.fromBGColor(Color.Blue, 2)
    val path = "a.png"
    val offset = StorageCoords(1, 2)
    val location = SaveLocation(new File(path), offset)
    val format = new SimpleStorageFormat
    val info = pool.SaveInfo(format)

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

    tracker.events shouldBe Seq(FileSystem.Event.ImageWritten(expectedImage, new File(path)))
  }

  "locationOf" should "return None if the image doesn't exist" in {
    val image = ImageStorage.fromBGColor(Color.Black, 2)
    new ImagePool().locationOf(image) shouldBe None
  }

  it should "return the location of the image if it exists" in {
    val image = ImageStorage.fromBGColor(Color.Black, 2)
    val location = SaveLocation(null)
    val info = pool.SaveInfo(null)

    val f = new ImagePool()
    f.move(image, location, info)

    f.locationOf(image) shouldBe Some(location)
  }

  "fromFile" should "return the image at that location if it exists" in {
    val image = ImageStorage.fromBGColor(FXColor.Blue, 16)
    val location = SaveLocation(null)
    val info = pool.SaveInfo(null)

    val f = new ImagePool()
    f.move(image, location, info)

    f.fromFile(location, storageFormat, 16, FileSystem.createNull()) shouldBe Success(image)
  }

  it should "return Failure if there is no image there and the loading failed" in {
    val location = SaveLocation(null)
    val imageSize = 16
    val fs = FileSystem.createNull(new FileSystem.NullArgs(initialImages = Map.empty))

    val pool = new ImagePool()

    pool.fromFile(location, storageFormat, imageSize, fs).isFailure shouldBe true
  }

  it should "save and return the newly loaded image if there was none already" in {
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
        actualImage.toRegularImage(storageFormat) shouldBe regularImage
        pool.locationOf(actualImage) shouldBe Some(location)
      case Failure(_) => fail()
    }
  }

  it should "load image with offset" in {
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
        actualImage.toRegularImage(storageFormat) shouldBe regularImage
        pool.locationOf(actualImage) shouldBe Some(location)
      case Failure(_) => fail()
    }
  }

  "move" should "set the image and return true if the location is empty" in {
    val p = new ImagePool()
    val image = ImageStorage.fromBGColor(Color.Blue, 8)
    val location = SaveLocation(null)
    val info = pool.SaveInfo(null)
    p.move(image, location, info) shouldBe true
    p.locationOf(image) shouldBe Some(location)
  }

  it should "simply return true if the image is already there" in {
    val p = new ImagePool()
    val image = ImageStorage.fromBGColor(Color.Blue, 8)
    val location = SaveLocation(null)
    val info = pool.SaveInfo(null)
    p.move(image, location, info)
    p.move(image, location, info) shouldBe true
    p.locationOf(image) shouldBe Some(location)
  }

  it should "return false if the handler doesn't accept the collision" in {
    val handler = collisionHandler
    val p = new ImagePool()
    val currentImage = ImageStorage.fromBGColor(Color.Blue, 8)
    val newImage = ImageStorage.fromBGColor(Color.Yellow, 8)
    val location = SaveLocation(null)
    val info = pool.SaveInfo(null)

    when(handler.shouldReplaceImage(currentImage, newImage, location)).thenReturn(None)

    p.move(currentImage, location, info)
    p.move(newImage, location, info) shouldBe false
  }

  it should "replace the current image, notify listeners, and return true if the handler wants to replace it" in {
    val handler = collisionHandler
    val listener = mock[ImagePoolListener]

    val p = new ImagePool()
    p.addListener(listener)
    val currentImage = ImageStorage.fromBGColor(Color.Blue, 8)
    val newImage = ImageStorage.fromBGColor(Color.Yellow, 8)
    val location = SaveLocation(null)
    val info = pool.SaveInfo(null)

    when(handler.shouldReplaceImage(currentImage, newImage, location)).thenReturn(Some(true))

    p.move(currentImage, location, info)
    p.move(newImage, location, info) shouldBe true
    p.locationOf(currentImage) shouldBe None
    p.locationOf(newImage) shouldBe Some(location)

    verify(listener).onImageReplaced(currentImage, newImage, location)
  }

  it should "keep the current image, notify listeners, and return true if the handler wants to keep it" in {
    val handler = collisionHandler
    val listener = mock[ImagePoolListener]

    val p = new ImagePool()
    p.addListener(listener)
    val currentImage = ImageStorage.fromBGColor(Color.Blue, 8)
    val newImage = ImageStorage.fromBGColor(Color.Yellow, 8)
    val location = SaveLocation(null)
    val info = pool.SaveInfo(null)

    when(handler.shouldReplaceImage(currentImage, newImage, location)).thenReturn(Some(false))

    p.move(currentImage, location, info)
    p.move(newImage, location, info) shouldBe true
    p.locationOf(currentImage) shouldBe Some(location)
    p.locationOf(newImage) shouldBe None

    verify(listener).onImageReplaced(newImage, currentImage, location)
  }
}
