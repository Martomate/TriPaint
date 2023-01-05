package com.martomate.tripaint.model.image.content

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model.coords.{TriImageCoords, TriangleCoords}
import com.martomate.tripaint.model.image.SaveLocation
import com.martomate.tripaint.model.image.format.SimpleStorageFormat
import com.martomate.tripaint.model.image.pool.{ImagePool, SaveInfo}
import com.martomate.tripaint.model.image.save.ImageSaverToFile
import com.martomate.tripaint.model.image.storage.ImageStorage
import org.mockito.Mockito.verify
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock
import scalafx.scene.paint.Color

import java.io.File

class ImageContentTest extends AnyFlatSpec with Matchers {

  "tellListenersAboutBigChange" should "tell the listeners that a lot has changed" in {
    val image = ImageStorage.fromBGColor(Color.Black, 2)
    val f = new ImageContent(TriImageCoords(0, 0), image)
    val listener = mock[ImageChangeListener]
    f.addListener(listener)

    f.tellListenersAboutBigChange()

    verify(listener).onImageChangedALot()
  }

  "changed" should "return false if nothing has happened" in {
    val image = ImageStorage.fromBGColor(Color.Black, 2)
    val f = new ImageContent(TriImageCoords(0, 0), image)
    f.changed shouldBe false
  }

  it should "return true if the image has been modified since the last save" in {
    val image = ImageStorage.fromBGColor(Color.Black, 2)
    val f = new ImageContent(TriImageCoords(0, 0), image)

    image.update(TriangleCoords(0, 0), Color.Blue)

    f.changed shouldBe true
  }

  it should "return false if the image was just saved" in {
    val image = ImageStorage.fromBGColor(Color.Black, 2)
    val location = SaveLocation(new File("a.png"))
    val format = new SimpleStorageFormat
    val info = SaveInfo(format)
    val pool = new ImagePool()
    pool.move(image, location, info)(null)
    val f = new ImageContent(TriImageCoords(0, 0), image)
    pool.addListener(f)

    image.update(TriangleCoords(0, 0), Color.Blue)
    pool.save(image, FileSystem.createNull())

    f.changed shouldBe false
  }

  "changedProperty" should "return a property with the same value as 'changed'"

  "image" should "return the initial image if the image hasn't been replaced in the pool"

  it should "return the new image if the image was replaced in the pool"
  // TODO: maybe it should take a coordinate instead of an initial image
}
