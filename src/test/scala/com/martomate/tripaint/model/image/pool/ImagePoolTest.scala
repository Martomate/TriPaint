package com.martomate.tripaint.model.image.pool

import com.martomate.tripaint.model.image.format.{SimpleStorageFormat, StorageFormat}
import com.martomate.tripaint.model.image.save.ImageSaver
import com.martomate.tripaint.model.image.storage.{ImageStorageFactory, ImageStorageImpl}
import com.martomate.tripaint.model.image.{SaveLocation, pool}
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
    val image = ImageStorageImpl.fromBGColor(Color.Black, 2)
    make().save(image, null) shouldBe false
  }

  it should "return false if the saver reports failure" in {
    val listener = mock[ImagePoolListener]
    val image = ImageStorageImpl.fromBGColor(Color.Black, 2)
    val location = SaveLocation(null)
    val format = new SimpleStorageFormat
    val info = pool.SaveInfo(format)

    val f = make()
    f.addListener(listener)
    f.move(image, location, info)

    f.save(image, (_, _, _) => false) shouldBe false
  }

  it should "notify listeners and return true if the saver reports success" in {
    val listener = mock[ImagePoolListener]
    val image = ImageStorageImpl.fromBGColor(Color.Black, 2)
    val location = SaveLocation(null)
    val format = new SimpleStorageFormat
    val info = pool.SaveInfo(format)

    val f = make()
    f.addListener(listener)
    f.move(image, location, info)

    val saver: ImageSaver = (_, _, _) => true
    listener.onImageSaved _ expects(image, saver)

    f.save(image, saver) shouldBe true
  }

  "locationOf" should "return None if the image doesn't exist" in {
    val image = ImageStorageImpl.fromBGColor(Color.Black, 2)
    make().locationOf(image) shouldBe None
  }

  it should "return the location of the image if it exists" in {
    val image = ImageStorageImpl.fromBGColor(Color.Black, 2)
    val location = SaveLocation(null)
    val info = pool.SaveInfo(null)

    val f = make()
    f.move(image, location, info)

    f.locationOf(image) shouldBe Some(location)
  }

  "fromBGColor" should "return what the factory returns" in {
    val factory = mock[ImageStorageFactory]
    val bgColor = Color.Blue
    val imageSize = 16
    val returnImage = ImageStorageImpl.fromBGColor(Color.Orange, imageSize)

    factory.fromBGColor _ expects(bgColor, imageSize) returns returnImage

    make(factory).fromBGColor(bgColor, imageSize) shouldBe returnImage
  }

  "fromFile" should "return the image at that location if it exists" in {
    val image = ImageStorageImpl.fromBGColor(Color.Blue, 16)
    val location = SaveLocation(null)
    val info = pool.SaveInfo(null)

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
    val image = ImageStorageImpl.fromBGColor(Color.Orange, imageSize)

    factory.fromFile _ expects(location, storageFormat, imageSize) returns Success(image)

    val f = make(factory)

    f.fromFile(location, storageFormat, imageSize) shouldBe Success(image)
    f.locationOf(image) shouldBe Some(location)
  }
}
