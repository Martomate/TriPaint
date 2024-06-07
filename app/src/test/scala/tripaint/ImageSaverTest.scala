package tripaint

import tripaint.coords.StorageCoords
import tripaint.image.{ImageStorage, RegularImage}
import tripaint.image.format.SimpleStorageFormat
import tripaint.infrastructure.FileSystem
import tripaint.model.ImageGrid
import tripaint.model.image.ImagePool.{SaveInfo, SaveLocation}
import tripaint.util.Tracker

import munit.FunSuite

import java.io.File

class ImageSaverTest extends FunSuite {
  test("save should return false if the saver reports failure") {
    val image = ImageStorage.fill(2, Color.Black)
    val location = SaveLocation(new File("a.png"))
    val format = SimpleStorageFormat
    val info = SaveInfo(format)

    val grid = new ImageGrid(2)

    val fs = FileSystem.createNull(FileSystem.NullArgs(supportedImageFormats = Set()))
    assertEquals(ImageSaver.saveImage(grid, image, fs, location, info), false)
  }

  test("save should write image if it does not exist") {
    val image = ImageStorage.fill(2, Color.Blue)
    val path = "a.png"
    val location = SaveLocation(new File(path))
    val format = SimpleStorageFormat
    val info = SaveInfo(format)

    val fs = FileSystem.createNull()
    val tracker = Tracker.withStorage[FileSystem.Event]
    fs.trackChanges(tracker)

    val grid = new ImageGrid(2)

    ImageSaver.saveImage(grid, image, fs, location, info)

    assertEquals(
      tracker.events,
      Seq(
        FileSystem.Event.ImageWritten(image.toRegularImage(format), new File(path))
      )
    )
  }

  test("save should overwrite image if it exists and has the same size") {
    val image = ImageStorage.fill(2, Color.Blue)
    val path = "a.png"
    val location = SaveLocation(new File(path))
    val format = SimpleStorageFormat
    val info = SaveInfo(format)

    val existingImage = RegularImage.fill(2, 2, Color.Red)
    val fs = FileSystem.createNull(
      FileSystem.NullArgs(initialImages = Map(new File(path) -> existingImage))
    )
    val tracker = Tracker.withStorage[FileSystem.Event]
    fs.trackChanges(tracker)

    val grid = new ImageGrid(2)

    ImageSaver.saveImage(grid, image, fs, location, info)

    assertEquals(
      tracker.events,
      Seq(
        FileSystem.Event.ImageWritten(image.toRegularImage(format), new File(path))
      )
    )
  }

  test("save should overwrite part of image if there already exists a bigger image") {
    val image = ImageStorage.fill(2, Color.Blue)
    val path = "a.png"
    val offset = StorageCoords(1, 2)
    val location = SaveLocation(new File(path), offset)
    val format = SimpleStorageFormat
    val info = SaveInfo(format)

    val existingImage = RegularImage.fill(3, 5, Color.Red)
    val fs = FileSystem.createNull(
      FileSystem.NullArgs(initialImages = Map(new File(path) -> existingImage))
    )
    val tracker = Tracker.withStorage[FileSystem.Event]
    fs.trackChanges(tracker)

    val grid = new ImageGrid(2)

    ImageSaver.saveImage(grid, image, fs, location, info)

    val expectedImage = RegularImage.fill(3, 5, Color.Red)
    expectedImage.pasteImage(offset, RegularImage.fill(2, 2, Color.Blue))

    assertEquals(tracker.events, Seq(FileSystem.Event.ImageWritten(expectedImage, new File(path))))
  }

  test(
    "save should overwrite part of image if there already exists an image even if it is too small"
  ) {
    val image = ImageStorage.fill(2, Color.Blue)
    val path = "a.png"
    val offset = StorageCoords(1, 2)
    val location = SaveLocation(new File(path), offset)
    val format = SimpleStorageFormat
    val info = SaveInfo(format)

    val existingImage = RegularImage.fill(3, 2, Color.Red)
    val fs = FileSystem.createNull(
      FileSystem.NullArgs(initialImages = Map(new File(path) -> existingImage))
    )
    val tracker = Tracker.withStorage[FileSystem.Event]
    fs.trackChanges(tracker)

    val grid = new ImageGrid(2)

    ImageSaver.saveImage(grid, image, fs, location, info)

    val expectedImage = RegularImage.ofSize(3, 4)
    expectedImage.pasteImage(StorageCoords(0, 0), RegularImage.fill(3, 2, Color.Red))
    expectedImage.pasteImage(offset, RegularImage.fill(2, 2, Color.Blue))

    assertEquals(tracker.events, Seq(FileSystem.Event.ImageWritten(expectedImage, new File(path))))
  }
}
