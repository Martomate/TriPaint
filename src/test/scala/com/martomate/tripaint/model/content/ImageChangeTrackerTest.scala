package com.martomate.tripaint.model.content

import com.martomate.tripaint.model.SaveLocation
import com.martomate.tripaint.model.coords.TriangleCoords
import com.martomate.tripaint.model.pool.{ImagePool, ImagePoolImpl}
import com.martomate.tripaint.model.save.ImageSaver
import com.martomate.tripaint.model.storage.ImageStorage
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import scalafx.scene.paint.Color

class ImageChangeTrackerTest extends FlatSpec with Matchers with MockFactory {
  def make(init_image: ImageStorage = stub[ImageStorage], pool: ImagePool = stub[ImagePool], saver: ImageSaver = stub[ImageSaver]): ImageChangeTracker =
    new ImageChangeTrackerImpl(init_image, pool, saver)

  "tellListenersAboutBigChange" should "tell the listeners that a lot has changed" in {
    val f = make()
    val listener = mock[ImageChangeListener]
    f.addListener(listener)

    listener.onImageChangedALot _ expects ()
    f.tellListenersAboutBigChange()
  }

  "changed" should "return false if nothing has happened" in {
    val f = make()
    f.changed shouldBe false
  }

  it should "return true if the image has been modified since the last save" in {
    val image = stub[ImageStorage]
    val f = make(image)

    image.update(TriangleCoords(0, 0), Color.Blue)

    f.changed shouldBe true
  }

  it should "return false if the image was just saved" in {
    val image = stub[ImageStorage]
    val location = SaveLocation(null)
    val saver = stub[ImageSaver]
    val pool = new ImagePoolImpl(null, null)
    pool.move(image, location)
    val f = make(image, pool, saver)

    saver.save _ when(image, *) returns true

    image.update(TriangleCoords(0, 0), Color.Blue)
    pool.save(image, saver)

    f.changed shouldBe false
  }

  "changedProperty" should "return a property with the same value as 'changed'"

  "image" should "return the initial image if the image hasn't been replaced in the pool"

  it should "return the new image if the image was replaced in the pool"
  // TODO: maybe it should take a coordinate instead of an initial image
}
