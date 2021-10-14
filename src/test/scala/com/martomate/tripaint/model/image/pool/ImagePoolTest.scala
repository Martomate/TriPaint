package com.martomate.tripaint.model.image.pool

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.image.format.{SimpleStorageFormat, StorageFormat}
import com.martomate.tripaint.model.image.save.ImageSaverToFile
import com.martomate.tripaint.model.image.storage.{ImageStorage, ImageStorageFactory, ImageStorageImpl}
import com.martomate.tripaint.model.image.{SaveLocation, pool}
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scalafx.scene.paint.{Color => FXColor}

import java.io.File
import scala.util.{Failure, Success}

class ImagePoolTest extends AnyFlatSpec with Matchers with MockFactory {
  implicit val collisionHandler: ImageSaveCollisionHandler = mock[ImageSaveCollisionHandler]
  val storageFormat: StorageFormat = new SimpleStorageFormat

  def make(factory: ImageStorageFactory = null): ImagePool = new ImagePool(factory)

  "save" should "return false if the image doesn't exist" in {
    val image = ImageStorageImpl.fromBGColor(FXColor.Black, 2)
    make().save(image, null, null) shouldBe false
  }

  it should "return false if the saver reports failure" in {
    val listener = mock[ImagePoolListener]
    val image = ImageStorageImpl.fromBGColor(FXColor.Black, 2)
    val location = SaveLocation(new File("a.png"))
    val format = new SimpleStorageFormat
    val info = pool.SaveInfo(format)

    val f = make()
    f.addListener(listener)
    f.move(image, location, info)

    f.save(image, new ImageSaverToFile, FileSystem.createNull(supportedImageFormats = Set())) shouldBe false
  }

  it should "notify listeners and return true if the saver reports success" in {
    val listener = mock[ImagePoolListener]
    val image = ImageStorageImpl.fromBGColor(FXColor.Black, 2)
    val location = SaveLocation(new File("a.png"))
    val format = new SimpleStorageFormat
    val info = pool.SaveInfo(format)

    val f = make()
    f.addListener(listener)
    f.move(image, location, info)

    val saver = new ImageSaverToFile
    listener.onImageSaved _ expects(image, saver)

    f.save(image, saver, FileSystem.createNull()) shouldBe true
  }

  "locationOf" should "return None if the image doesn't exist" in {
    val image = ImageStorageImpl.fromBGColor(FXColor.Black, 2)
    make().locationOf(image) shouldBe None
  }

  it should "return the location of the image if it exists" in {
    val image = ImageStorageImpl.fromBGColor(FXColor.Black, 2)
    val location = SaveLocation(null)
    val info = pool.SaveInfo(null)

    val f = make()
    f.move(image, location, info)

    f.locationOf(image) shouldBe Some(location)
  }

  "fromBGColor" should "return what the factory returns" in {
    val factory = mock[ImageStorageFactory]
    val bgColor = Color.fromFXColor(FXColor.Blue)
    val imageSize = 16
    val returnImage = ImageStorageImpl.fromBGColor(FXColor.Orange, imageSize)

    factory.fromBGColor _ expects(bgColor, imageSize) returns returnImage

    make(factory).fromBGColor(bgColor, imageSize) shouldBe returnImage
  }

  "fromFile" should "return the image at that location if it exists" in {
    val image = ImageStorageImpl.fromBGColor(FXColor.Blue, 16)
    val location = SaveLocation(null)
    val info = pool.SaveInfo(null)

    val f = make()
    f.move(image, location, info)

    f.fromFile(location, storageFormat, 16, FileSystem.createNull()) shouldBe Success(image)
  }

  it should "return Failure if there is no image there and the loading failed" in {
    val factory = mock[ImageStorageFactory]
    val location = SaveLocation(null)
    val imageSize = 16
    val failure = Failure(null)
    val fs = FileSystem.createNull()

    factory.fromFile _ expects(location, storageFormat, imageSize, fs) returns failure

    val f = make(factory)

    f.fromFile(location, storageFormat, imageSize, fs) shouldBe failure
  }

  it should "save and return the newly loaded image if there was none already" in {
    val factory = mock[ImageStorageFactory]
    val location = SaveLocation(null)
    val imageSize = 16
    val image = ImageStorageImpl.fromBGColor(FXColor.Orange, imageSize)
    val fs = FileSystem.createNull()

    factory.fromFile _ expects(location, storageFormat, imageSize, fs) returns Success(image)

    val f = make(factory)

    f.fromFile(location, storageFormat, imageSize, fs) shouldBe Success(image)
    f.locationOf(image) shouldBe Some(location)
  }


  "move" should "set the image and return true if the location is empty" in {
    val p = make()
    val image = stub[ImageStorage]
    val location = SaveLocation(null)
    val info = pool.SaveInfo(null)
    p.move(image, location, info) shouldBe true
    p.locationOf(image) shouldBe Some(location)
  }

  it should "simply return true if the image is already there" in {
    val p = make()
    val image = stub[ImageStorage]
    val location = SaveLocation(null)
    val info = pool.SaveInfo(null)
    p.move(image, location, info)
    p.move(image, location, info) shouldBe true
    p.locationOf(image) shouldBe Some(location)
  }

  it should "return false if the handler doesn't accept the collision" in {
    val handler = collisionHandler
    val p = make()
    val currentImage = stub[ImageStorage]
    val newImage = stub[ImageStorage]
    val location = SaveLocation(null)
    val info = pool.SaveInfo(null)

    (handler.shouldReplaceImage _).expects(currentImage, newImage, location).returns(None)

    p.move(currentImage, location, info)
    p.move(newImage, location, info) shouldBe false
  }

  it should "replace the current image, notify listeners, and return true if the handler wants to replace it" in {
    val handler = collisionHandler
    val listener = mock[ImagePoolListener]

    val p = make()
    p.addListener(listener)
    val currentImage = stub[ImageStorage]
    val newImage = stub[ImageStorage]
    val location = SaveLocation(null)
    val info = pool.SaveInfo(null)

    (handler.shouldReplaceImage _).expects(currentImage, newImage, location).returns(Some(true))
    (listener.onImageReplaced _).expects(currentImage, newImage, location)

    p.move(currentImage, location, info)
    p.move(newImage, location, info) shouldBe true
    p.locationOf(currentImage) shouldBe None
    p.locationOf(newImage) shouldBe Some(location)
  }

  it should "keep the current image, notify listeners, and return true if the handler wants to keep it" in {
    val handler = collisionHandler
    val listener = mock[ImagePoolListener]

    val p = make()
    p.addListener(listener)
    val currentImage = stub[ImageStorage]
    val newImage = stub[ImageStorage]
    val location = SaveLocation(null)
    val info = pool.SaveInfo(null)

    (handler.shouldReplaceImage _).expects(currentImage, newImage, location).returns(Some(false))
    (listener.onImageReplaced _).expects(newImage, currentImage, location)

    p.move(currentImage, location, info)
    p.move(newImage, location, info) shouldBe true
    p.locationOf(currentImage) shouldBe Some(location)
    p.locationOf(newImage) shouldBe None
  }
}
