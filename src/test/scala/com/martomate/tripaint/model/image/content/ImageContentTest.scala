package com.martomate.tripaint.model.image.content

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model.coords.{TriImageCoords, TriangleCoords}
import com.martomate.tripaint.model.grid.ImageGrid
import com.martomate.tripaint.model.image.SaveLocation
import com.martomate.tripaint.model.image.format.SimpleStorageFormat
import com.martomate.tripaint.model.image.pool.{ImagePool, SaveInfo}
import com.martomate.tripaint.model.image.save.ImageSaverToFile
import com.martomate.tripaint.model.image.storage.ImageStorage
import munit.FunSuite
import org.mockito.Mockito.verify
import org.scalatestplus.mockito.MockitoSugar.mock
import scalafx.scene.paint.Color

import java.io.File

class ImageContentTest extends FunSuite {

  test("tellListenersAboutBigChange should tell the listeners that a lot has changed") {
    val image = ImageStorage.fromBGColor(Color.Black, 2)
    val f = new ImageContent(TriImageCoords(0, 0), image)
    val listener = mock[ImageContent.Event => Unit]
    f.addListener(listener)

    f.tellListenersAboutBigChange()

    import ImageContent.Event.*
    verify(listener).apply(ImageChangedALot)
  }

  test("changed should return false if nothing has happened") {
    val image = ImageStorage.fromBGColor(Color.Black, 2)
    val f = new ImageContent(TriImageCoords(0, 0), image)
    assert(!f.changed)
  }

  test("changed should return true if the image has been modified since the last save") {
    val image = ImageStorage.fromBGColor(Color.Black, 2)
    val f = new ImageContent(TriImageCoords(0, 0), image)

    image.update(TriangleCoords(0, 0), Color.Blue)

    assert(f.changed)
  }

  test("changed should return false if the image was just saved") {
    val image = ImageStorage.fromBGColor(Color.Black, 2)
    val location = SaveLocation(new File("a.png"))
    val format = new SimpleStorageFormat
    val info = SaveInfo(format)

    val pool = new ImagePool()
    pool.move(image, location, info)(null)

    val grid = new ImageGrid(2)
    grid.listenToImagePool(pool)

    val f = new ImageContent(TriImageCoords(0, 0), image)
    grid.set(f)

    image.update(TriangleCoords(0, 0), Color.Blue)
    pool.save(image, FileSystem.createNull())

    assert(!f.changed)
  }

  test("changedProperty should return a property with the same value as 'changed'".ignore) {}

  test(
    "image should return the initial image if the image hasn't been replaced in the pool".ignore
  ) {}

  test("image should return the new image if the image was replaced in the pool".ignore) {}
  // TODO: maybe it should take a coordinate instead of an initial image
}
