package tripaint.control.action

import tripaint.Color
import tripaint.TriPaintModel
import tripaint.control.Actions
import tripaint.coords.{GridCoords, StorageCoords}
import tripaint.image.RegularImage
import tripaint.image.format.SimpleStorageFormat
import tripaint.infrastructure.FileSystem
import tripaint.view.FileOpenSettings

import munit.FunSuite

import java.io.File

class OpenActionTest extends FunSuite {
  test("OpenAction should open an image") {
    val file = new File("image.png")
    val image = RegularImage.fill(8, 8, Color.Yellow)
    image.setColor(5, 6, Color.Cyan)

    val model =
      TriPaintModel.createNull(8, FileSystem.NullArgs(initialImages = Map(file -> image)))

    Actions.openImage(
      model,
      file,
      FileOpenSettings(StorageCoords(0, 0), SimpleStorageFormat),
      GridCoords(3, 4)
    )

    val actualImage = model
      .imageGrid(GridCoords(3, 4))
      .map(_.storage)
      .map(_.toRegularImage(SimpleStorageFormat))
      .orNull

    assertEquals(actualImage, image)
  }

  test("OpenAction should open an image at an offset") {
    val file = new File("image.png")
    val image = RegularImage.fill(8, 8, Color.Yellow)
    image.setColor(5, 6, Color.Cyan)

    val storedImage = RegularImage.ofSize(9, 10)
    val offset = StorageCoords(1, 2)
    storedImage.pasteImage(offset, image)

    val model =
      TriPaintModel.createNull(8, FileSystem.NullArgs(initialImages = Map(file -> storedImage)))

    Actions.openImage(
      model,
      file,
      FileOpenSettings(offset, SimpleStorageFormat),
      GridCoords(3, 4)
    )

    val actualImage = model
      .imageGrid(GridCoords(3, 4))
      .map(_.storage)
      .map(_.toRegularImage(SimpleStorageFormat))
      .orNull

    assertEquals(actualImage, image)
  }
}
