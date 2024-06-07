package tripaint.model.image

import tripaint.Color
import tripaint.coords.{GridCoords, TriangleCoords}
import tripaint.image.ImageStorage
import tripaint.util.Tracker

import munit.FunSuite

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

    val cell = new GridCell(GridCoords(0, 0), image)

    image.setColor(TriangleCoords(0, 0), Color.Blue)
    assert(cell.changed)

    cell.setImageSaved()
    assert(!cell.changed)
  }

  test("changedProperty should return a property with the same value as 'changed'".ignore) {}

  test(
    "image should return the initial image if the image hasn't been replaced in the pool".ignore
  ) {}

  test("image should return the new image if the image was replaced in the pool".ignore) {}
  // TODO: maybe it should take a coordinate instead of an initial image
}
