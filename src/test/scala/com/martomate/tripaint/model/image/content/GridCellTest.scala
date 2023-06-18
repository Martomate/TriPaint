package com.martomate.tripaint.model.image.content

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model.ImageGrid
import com.martomate.tripaint.model.coords.{GridCoords, TriangleCoords}
import com.martomate.tripaint.model.image.{GridCell, ImageChange, ImagePool, ImageStorage}
import com.martomate.tripaint.model.image.format.SimpleStorageFormat
import com.martomate.tripaint.util.Tracker
import munit.FunSuite
import scalafx.scene.paint.Color

import java.io.File

class GridCellTest extends FunSuite {

  test("onImageChangedALot should tell the listeners that a lot has changed") {
    val image = ImageStorage.fill(2, Color.Black)
    val f = new GridCell(GridCoords(0, 0), image)

    val tracker = Tracker.withStorage[GridCell.Event]
    f.trackChanges(tracker)

    f.onImageChangedALot()

    assertEquals(tracker.events, Seq(GridCell.Event.ImageChangedALot))
  }

  test("changed should return false if nothing has happened") {
    val image = ImageStorage.fill(2, Color.Black)
    val f = new GridCell(GridCoords(0, 0), image)
    assert(!f.changed)
  }

  test("changed should return true if the image has been modified since the last save") {
    val image = ImageStorage.fill(2, Color.Black)
    val f = new GridCell(GridCoords(0, 0), image)

    image.setColor(TriangleCoords(0, 0), Color.Blue)

    assert(f.changed)
  }

  test("changed should return false if the image was just saved") {
    val image = ImageStorage.fill(2, Color.Black)
    val location = ImagePool.SaveLocation(new File("a.png"))
    val format = SimpleStorageFormat
    val info = ImagePool.SaveInfo(format)

    val pool = new ImagePool()
    pool.move(image, location, info)(using null)

    val grid = new ImageGrid(2)
    grid.listenToImagePool(pool)

    val f = new GridCell(GridCoords(0, 0), image)
    grid.set(f)

    image.setColor(TriangleCoords(0, 0), Color.Blue)
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
