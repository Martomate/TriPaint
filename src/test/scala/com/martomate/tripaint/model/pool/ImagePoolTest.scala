package com.martomate.tripaint.model.pool

import com.martomate.tripaint.model.{SaveInfo, SaveLocation}
import com.martomate.tripaint.model.format.{SimpleStorageFormat, StorageFormat}
import com.martomate.tripaint.model.save.ImageSaver
import com.martomate.tripaint.model.storage.{ImageStorage, ImageStorageFactory}
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scalafx.scene.paint.Color

import scala.util.{Failure, Success}

abstract class ImagePoolTest extends AnyFlatSpec with Matchers with MockFactory {
  implicit val collisionHandler: ImageSaveCollisionHandler = mock[ImageSaveCollisionHandler]
  val storageFormat: StorageFormat = new SimpleStorageFormat

  def make(factory: ImageStorageFactory = null): ImagePool

  "save" should "return false if the image doesn't exist" in {
    make().save(stub[ImageStorage], null) shouldBe false
  }

  it should "return false if the saver reports failure" in {
    val saver = mock[ImageSaver]
    val listener = mock[ImagePoolListener]
    val image = stub[ImageStorage]
    val location = SaveLocation(null)
    val format = stub[StorageFormat]
    val info = SaveInfo(format)

    val f = make()
    f.addListener(listener)
    f.move(image, location, info)

    saver.save _ expects (image, format, location) returns false

    f.save(image, saver) shouldBe false
  }

  it should "notify listeners and return true if the saver reports success" in {
    val saver = mock[ImageSaver]
    val listener = mock[ImagePoolListener]
    val image = stub[ImageStorage]
    val location = SaveLocation(null)
    val format = stub[StorageFormat]
    val info = SaveInfo(format)

    val f = make()
    f.addListener(listener)
    f.move(image, location, info)

    saver.save _ expects (image, format, location) returns true
    listener.onImageSaved _ expects(image, saver)

    f.save(image, saver) shouldBe true
  }

  "locationOf" should "return None if the image doesn't exist" in {
    make().locationOf(stub[ImageStorage]) shouldBe None
  }

  it should "return the location of the image if it exists" in {
    val image = stub[ImageStorage]
    val location = SaveLocation(null)
    val info = SaveInfo(null)

    val f = make()
    f.move(image, location, info)

    f.locationOf(image) shouldBe Some(location)
  }

  "fromBGColor" should "return what the factory returns" in {
    val factory = mock[ImageStorageFactory]
    val bgColor = Color.Blue
    val imageSize = 16
    val returnImage = stub[ImageStorage]

    factory.fromBGColor _ expects(bgColor, imageSize) returns returnImage

    make(factory).fromBGColor(bgColor, imageSize) shouldBe returnImage
  }

  "fromFile" should "return the image at that location if it exists" in {
    val image = stub[ImageStorage]
    val location = SaveLocation(null)
    val info = SaveInfo(null)

    val f = make()
    f.move(image, location, info)

    f.fromFile(location, storageFormat, 16) shouldBe Success(image)
  }

  it should "return Failure if there is no image there and the loading failed" in {
    val factory = mock[ImageStorageFactory]
    val location = SaveLocation(null)
    val imageSize = 16
    val failure = Failure(null)

    factory.fromFile _ expects(location, storageFormat, imageSize) returns failure

    val f = make(factory)

    f.fromFile(location, storageFormat, imageSize) shouldBe failure
  }

  it should "save and return the newly loaded image if there was none already" in {
    val factory = mock[ImageStorageFactory]
    val location = SaveLocation(null)
    val imageSize = 16
    val image = stub[ImageStorage]

    factory.fromFile _ expects(location, storageFormat, imageSize) returns Success(image)

    val f = make(factory)

    f.fromFile(location, storageFormat, imageSize) shouldBe Success(image)
    f.locationOf(image) shouldBe Some(location)
  }
}
