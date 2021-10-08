package com.martomate.tripaint.model.image.content

import com.martomate.tripaint.model.coords.{TriImageCoords, TriangleCoords}
import com.martomate.tripaint.model.image.SaveLocation
import com.martomate.tripaint.model.image.format.SimpleStorageFormat
import com.martomate.tripaint.model.image.pool.{ImagePoolImpl, SaveInfo}
import com.martomate.tripaint.model.image.storage.ImageStorageImpl
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scalafx.scene.paint.Color

class ImageContentTest extends AnyFlatSpec with Matchers with MockFactory {

  "tellListenersAboutBigChange" should "tell the listeners that a lot has changed" in {
    val image = ImageStorageImpl.fromBGColor(Color.Black, 2)
    val f = new ImageContent(TriImageCoords(0, 0), image)
    val listener = mock[ImageChangeListener]
    f.addListener(listener)

    (listener.onImageChangedALot _).expects ()
    f.tellListenersAboutBigChange()
  }

  "changed" should "return false if nothing has happened" in {
    val image = ImageStorageImpl.fromBGColor(Color.Black, 2)
    val f = new ImageContent(TriImageCoords(0, 0), image)
    f.changed shouldBe false
  }

  it should "return true if the image has been modified since the last save" in {
    val image = ImageStorageImpl.fromBGColor(Color.Black, 2)
    val f = new ImageContent(TriImageCoords(0, 0), image)

    image.update(TriangleCoords(0, 0), Color.Blue)

    f.changed shouldBe true
  }

  it should "return false if the image was just saved" in {
    val image = ImageStorageImpl.fromBGColor(Color.Black, 2)
    val location = SaveLocation(null)
    val format = new SimpleStorageFormat
    val info = SaveInfo(format)
    val pool = new ImagePoolImpl(null)
    pool.move(image, location, info)(null)
    val f = new ImageContent(TriImageCoords(0, 0), image)
    pool.addListener(f)

    image.update(TriangleCoords(0, 0), Color.Blue)
    pool.save(image, (_, _, _) => true)

    f.changed shouldBe false
  }

  "changedProperty" should "return a property with the same value as 'changed'"

  "image" should "return the initial image if the image hasn't been replaced in the pool"

  it should "return the new image if the image was replaced in the pool"
  // TODO: maybe it should take a coordinate instead of an initial image
}
