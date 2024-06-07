package tripaint.control.action

import tripaint.Color
import tripaint.control.Actions
import tripaint.coords.GridCoords
import tripaint.image.RegularImage
import tripaint.image.format.SimpleStorageFormat
import tripaint.model.TriPaintModel

import munit.FunSuite

class NewActionTest extends FunSuite {
  test("NewAction should add a new image to the grid") {
    val model = TriPaintModel.createNull(8)

    val imageSize = model.imageGrid.imageSize
    val backgroundColor = Color.Cyan

    Actions.createNewImage(model.imageGrid, backgroundColor, GridCoords(3, 4))

    val expectedImage = RegularImage.fill(imageSize, imageSize, backgroundColor)

    val actualImage = model
      .imageGrid(GridCoords(3, 4))
      .map(_.storage)
      .map(_.toRegularImage(SimpleStorageFormat))
      .orNull

    assertEquals(actualImage, expectedImage)
  }

  // TODO: Is this really how it should work?
  test("NewAction should replace any existing image at the location") {
    val model = TriPaintModel.createNull(8)

    val imageSize = model.imageGrid.imageSize
    val backgroundColor = Color.Cyan

    Actions.createNewImage(model.imageGrid, backgroundColor, GridCoords(3, 4))
    Actions.createNewImage(model.imageGrid, backgroundColor, GridCoords(3, 4))

    val expectedImage = RegularImage.fill(imageSize, imageSize, backgroundColor)

    val actualImage = model
      .imageGrid(GridCoords(3, 4))
      .map(_.storage)
      .map(_.toRegularImage(SimpleStorageFormat))
      .orNull

    assertEquals(actualImage, expectedImage)
  }
}
