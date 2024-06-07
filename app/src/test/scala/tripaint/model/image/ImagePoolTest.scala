package tripaint.model.image

import tripaint.Color
import tripaint.ScalaFxExt.given
import tripaint.control.Actions
import tripaint.infrastructure.FileSystem
import tripaint.model.TriPaintModel
import tripaint.model.coords.{GridCoords, StorageCoords}
import tripaint.model.image.ImagePool.SaveLocation
import tripaint.model.image.format.{SimpleStorageFormat, StorageFormat}
import tripaint.view.FileOpenSettings

import munit.FunSuite
import scalafx.scene.paint.Color as FXColor

import java.io.File
import scala.language.implicitConversions

class ImagePoolTest extends FunSuite {
  val storageFormat: StorageFormat = SimpleStorageFormat

  test("locationOf should return None if the image doesn't exist") {
    val image = ImageStorage.fill(2, Color.Black)
    assertEquals(new ImagePool().locationOf(image), None)
  }

  test("locationOf should return the location of the image if it exists") {
    val image = ImageStorage.fill(2, Color.Black)
    val location = SaveLocation(null)
    val info = ImagePool.SaveInfo(null)

    val f = new ImagePool()
    f.set(image, location, info)

    assertEquals(f.locationOf(image), Some(location))
  }

  test("Actions.openImage should do nothing if loading failed") {
    val location = SaveLocation(null)
    val imageSize = 16

    val model = TriPaintModel.createNull(imageSize, FileSystem.NullArgs(initialImages = Map.empty))
    val pool = model.imagePool

    Actions.openImage(
      model,
      location.file,
      FileOpenSettings(location.offset, storageFormat),
      GridCoords(0, 0)
    )

    assertEquals(pool.imageAt(location), None)
  }

  test("Actions.openImage should store the loaded image in the image pool") {
    val file = new File("path/to/image.png")
    val location = SaveLocation(file)
    val imageSize = 16

    val image = ImageStorage.fill(imageSize, FXColor.Orange)
    val regularImage = image.toRegularImage(storageFormat)

    val model =
      TriPaintModel.createNull(
        imageSize,
        FileSystem.NullArgs(initialImages = Map(file -> regularImage))
      )

    Actions.openImage(
      model,
      location.file,
      FileOpenSettings(location.offset, storageFormat),
      GridCoords(0, 0)
    )
    val loadedImage = model.imagePool.imageAt(location).get

    assertEquals(loadedImage.toRegularImage(storageFormat), regularImage)
  }

  test("Actions.openImage should load image with offset") {
    val file = new File("path/to/image.png")
    val offset = StorageCoords(2, 3)
    val location = SaveLocation(file, offset)
    val imageSize = 16

    val image = ImageStorage.fill(imageSize, FXColor.Orange)
    val regularImage = image.toRegularImage(storageFormat)

    val storedImage = RegularImage.ofSize(imageSize + offset.x, imageSize + offset.y)
    storedImage.pasteImage(offset, regularImage)

    val model = TriPaintModel.createNull(
      imageSize,
      FileSystem.NullArgs(initialImages = Map(file -> storedImage))
    )
    val pool = model.imagePool

    Actions.openImage(
      model,
      location.file,
      FileOpenSettings(location.offset, storageFormat),
      GridCoords(0, 0)
    )
    val loadedImage = pool.imageAt(location).get

    assertEquals(loadedImage.toRegularImage(storageFormat), regularImage)
  }
}
