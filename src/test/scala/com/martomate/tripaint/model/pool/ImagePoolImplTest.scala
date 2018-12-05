package com.martomate.tripaint.model.pool

import com.martomate.tripaint.model.SaveLocation
import com.martomate.tripaint.model.storage.{ImageStorage, ImageStorageFactory}

class ImagePoolImplTest extends ImagePoolTest {
  override def make(factory: ImageStorageFactory = null): ImagePool = new ImagePoolImpl(factory)

  "move" should "set the image and return true if the location is empty" in {
    val p = make()
    val image = stub[ImageStorage]
    val location = SaveLocation(null)
    p.move(image, location) shouldBe true
    p.locationOf(image) shouldBe Some(location)
  }

  it should "simply return true if the image is already there" in {
    val p = make()
    val image = stub[ImageStorage]
    val location = SaveLocation(null)
    p.move(image, location)
    p.move(image, location) shouldBe true
    p.locationOf(image) shouldBe Some(location)
  }

  it should "return false if the handler doesn't accept the collision" in {
    val handler = collisionHandler
    val p = new ImagePoolImpl(null)
    val currentImage = stub[ImageStorage]
    val newImage = stub[ImageStorage]
    val location = SaveLocation(null)

    (handler.shouldReplaceImage _).expects(currentImage, newImage, location).returns(None)

    p.move(currentImage, location)
    p.move(newImage, location) shouldBe false
  }

  it should "replace the current image, notify listeners, and return true if the handler wants to replace it" in {
    val handler = collisionHandler
    val listener = mock[ImagePoolListener]

    val p = new ImagePoolImpl(null)
    p.addListener(listener)
    val currentImage = stub[ImageStorage]
    val newImage = stub[ImageStorage]
    val location = SaveLocation(null)

    (handler.shouldReplaceImage _).expects(currentImage, newImage, location).returns(Some(true))
    (listener.onImageReplaced _).expects(currentImage, newImage, location)

    p.move(currentImage, location)
    p.move(newImage, location) shouldBe true
    p.locationOf(currentImage) shouldBe None
    p.locationOf(newImage) shouldBe Some(location)
  }

  it should "keep the current image, notify listeners, and return true if the handler wants to keep it" in {
    val handler = collisionHandler
    val listener = mock[ImagePoolListener]

    val p = new ImagePoolImpl(null)
    p.addListener(listener)
    val currentImage = stub[ImageStorage]
    val newImage = stub[ImageStorage]
    val location = SaveLocation(null)

    (handler.shouldReplaceImage _).expects(currentImage, newImage, location).returns(Some(false))
    (listener.onImageReplaced _).expects(newImage, currentImage, location)

    p.move(currentImage, location)
    p.move(newImage, location) shouldBe true
    p.locationOf(currentImage) shouldBe Some(location)
    p.locationOf(newImage) shouldBe None
  }
}
