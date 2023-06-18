package com.martomate.tripaint.model

import com.martomate.tripaint.model
import com.martomate.tripaint.model.ImageGrid
import com.martomate.tripaint.model.coords.{GridCoords, TriangleCoords}
import com.martomate.tripaint.model.image.{GridCell, ImageChange, ImageStorage}
import com.martomate.tripaint.util.Tracker
import munit.FunSuite

class ImageGridTest extends FunSuite {
  test("setImageSizeIfEmpty should set the image size and return true if the grid is new") {
    val f = new ImageGrid(16)
    val initSize = f.imageSize
    assertEquals(f.setImageSizeIfEmpty(initSize + 16), true)
    assertEquals(f.imageSize, initSize + 16)
  }

  test(
    "setImageSizeIfEmpty should not set the image size and return false if the grid contains images"
  ) {
    val f = new ImageGrid(16)
    val initSize = f.imageSize

    f.set(new GridCell(GridCoords(0, 0), ImageStorage.fill(4, Color.Black)))

    assertEquals(f.setImageSizeIfEmpty(initSize + 16), false)
    assertEquals(f.imageSize, initSize)
  }

  test(
    "setImageSizeIfEmpty should set the image size and return true if the grid no longer contains images"
  ) {
    val f = new ImageGrid(16)
    val initSize = f.imageSize

    f.set(new GridCell(GridCoords(0, 0), ImageStorage.fill(4, Color.Black)))
    f -= GridCoords(0, 0)

    assertEquals(f.setImageSizeIfEmpty(initSize + 16), true)
    assertEquals(f.imageSize, initSize + 16)
  }

  test("apply should return None if there is no image there") {
    val f = new ImageGrid(16)
    val image = new GridCell(GridCoords(1, 0), ImageStorage.fill(4, Color.Black))

    assertEquals(f(GridCoords(0, 0)), None)
    f.set(image)
    assertEquals(f(GridCoords(0, 0)), None)
  }

  test("apply should return the image at the given location") {
    val f = new ImageGrid(16)
    val image = new GridCell(GridCoords(1, 0), ImageStorage.fill(4, Color.Black))

    assertEquals(f(GridCoords(1, 0)), None)
    f.set(image)
    assertEquals(f(GridCoords(1, 0)), Some(image))
    assertEquals(f(GridCoords(0, 1)), None)
    assertEquals(f(GridCoords(1, 0)), Some(image))
  }

  test("update should add the image if it doesn't already exist") {
    val f = new ImageGrid(16)
    val image = new GridCell(GridCoords(1, 0), ImageStorage.fill(4, Color.Black))

    f.set(image)
    assertEquals(f(GridCoords(1, 0)), Some(image))
  }

  test("update should replace the image if there is already one at that location") {
    val f = new ImageGrid(16)
    val image = new GridCell(GridCoords(1, 0), ImageStorage.fill(4, Color.Black))
    val image2 = new GridCell(GridCoords(1, 0), ImageStorage.fill(4, Color.Black))

    f.set(image)
    f.set(image2)
    assertEquals(f(GridCoords(1, 0)), Some(image2))
  }

  test("update should notify listeners about image addition, and image removal if there was one") {
    val f = new ImageGrid(16)
    val image = new GridCell(GridCoords(1, 0), ImageStorage.fill(4, Color.Black))
    val image2 = new GridCell(GridCoords(1, 0), ImageStorage.fill(4, Color.Black))

    val tracker = Tracker.withStorage[ImageGrid.Event]
    f.trackChanges(tracker)

    f.set(image)
    assertEquals(tracker.events, Seq(ImageGrid.Event.ImageAdded(image)))

    f.set(image2)
    assertEquals(
      tracker.events,
      Seq(
        ImageGrid.Event.ImageAdded(image),
        ImageGrid.Event.ImageRemoved(image),
        ImageGrid.Event.ImageAdded(image2)
      )
    )
  }

  test("-= should return null if there is no image there") {
    val f = new ImageGrid(16)
    assertEquals(f -= GridCoords(1, 2), null)
  }

  test("-= should remove the image and return it if it exists") {
    val f = new ImageGrid(16)
    val image = new GridCell(GridCoords(1, 0), ImageStorage.fill(4, Color.Black))

    f.set(image)
    assertEquals(f -= GridCoords(1, 0), image)
    assertEquals(f(GridCoords(1, 0)), None)
  }

  test("-= should notify listeners if there was a removal") {
    val f = new ImageGrid(16)
    val image = new GridCell(GridCoords(1, 0), ImageStorage.fill(4, Color.Black))

    f.set(image)

    val tracker = Tracker.withStorage[ImageGrid.Event]
    f.trackChanges(tracker)

    f -= GridCoords(0, 0)
    assertEquals(tracker.events, Seq())

    f -= GridCoords(1, 0)
    assertEquals(tracker.events, Seq(ImageGrid.Event.ImageRemoved(image)))
  }

  test("selectedImages should return all images that are currently selected") {
    val f = new ImageGrid(16)
    val image = new GridCell(GridCoords(1, 0), ImageStorage.fill(4, Color.Black))
    val image2 = new GridCell(GridCoords(2, 0), ImageStorage.fill(4, Color.Black))

    f.set(image)
    f.set(image2)

    assertEquals(f.selectedImages.sortBy(_.##), Seq(image, image2).sortBy(_.##))
    image.editableProperty() = false
    assertEquals(f.selectedImages.sortBy(_.##), Seq(image2).sortBy(_.##))
    image.editableProperty() = true
    image2.editableProperty() = false
    assertEquals(f.selectedImages.sortBy(_.##), Seq(image).sortBy(_.##))
    image2.editableProperty() = true
    assertEquals(f.selectedImages.sortBy(_.##), Seq(image, image2).sortBy(_.##))
  }

  test("undo should remove the last action") {
    val grid = new ImageGrid(16)

    val storage = ImageStorage.fill(16, Color.Black)
    storage.setColor(TriangleCoords(1, 2), Color.Blue)

    grid.set(new GridCell(GridCoords(1, 0), storage))

    grid.performChange(
      new ImageGridChange(
        Map(
          GridCoords(1, 0) ->
            ImageChange
              .builder()
              .addChange(TriangleCoords(1, 2), Color.Blue, Color.Red)
              .addChange(TriangleCoords(3, 5), Color.Black, Color.Cyan)
              .done(storage)
        )
      )
    )

    assertEquals(storage.getColor(TriangleCoords(1, 2)), Color.Red)
    assertEquals(storage.getColor(TriangleCoords(3, 5)), Color.Cyan)

    grid.undo()

    assertEquals(storage.getColor(TriangleCoords(1, 2)), Color.Blue)
    assertEquals(storage.getColor(TriangleCoords(3, 5)), Color.Black)
  }

}
