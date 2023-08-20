package com.martomate.tripaint.model

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model
import com.martomate.tripaint.model.coords.{GridCoords, StorageCoords, TriangleCoords}
import com.martomate.tripaint.model.image.{
  GridCell,
  ImageChange,
  ImagePool,
  ImageSaveCollisionHandler,
  ImageStorage,
  RegularImage
}
import com.martomate.tripaint.model.image.ImagePool.{SaveInfo, SaveLocation}
import com.martomate.tripaint.model.image.format.SimpleStorageFormat
import com.martomate.tripaint.util.Tracker

import munit.FunSuite
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock

import java.io.File

class ImageGridTest extends FunSuite with MockitoSugar {
  given collisionHandler: ImageSaveCollisionHandler = mock[ImageSaveCollisionHandler]

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

  test("save should return false if the saver reports failure") {
    val image = ImageStorage.fill(2, Color.Black)
    val location = SaveLocation(new File("a.png"))
    val format = SimpleStorageFormat
    val info = SaveInfo(format)

    val grid = new ImageGrid(2)

    val fs = FileSystem.createNull(FileSystem.NullArgs(supportedImageFormats = Set()))
    assertEquals(grid.save(image, fs, location, info), false)
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

    grid.save(image, fs, location, info)

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

    grid.save(image, fs, location, info)

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

    grid.save(image, fs, location, info)

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

    grid.save(image, fs, location, info)

    val expectedImage = RegularImage.ofSize(3, 4)
    expectedImage.pasteImage(StorageCoords(0, 0), RegularImage.fill(3, 2, Color.Red))
    expectedImage.pasteImage(offset, RegularImage.fill(2, 2, Color.Blue))

    assertEquals(tracker.events, Seq(FileSystem.Event.ImageWritten(expectedImage, new File(path))))
  }

  test("setImageSource should set the image and return true if the location is empty") {
    val image = ImageStorage.fill(8, Color.Blue)
    val location = SaveLocation(null)
    val info = ImagePool.SaveInfo(null)

    val p = new ImagePool()
    val grid = new ImageGrid(8)

    assertEquals(grid.setImageSource(image, location, info)(p, collisionHandler), true)
    assertEquals(p.locationOf(image), Some(location))
  }

  test("setImageSource should simply return true if the image is already there") {
    val image = ImageStorage.fill(8, Color.Blue)
    val location = SaveLocation(null)
    val info = ImagePool.SaveInfo(null)

    val p = new ImagePool()
    val grid = new ImageGrid(8)

    grid.setImageSource(image, location, info)(p, collisionHandler)
    assertEquals(grid.setImageSource(image, location, info)(p, collisionHandler), true)
    assertEquals(p.locationOf(image), Some(location))
  }

  test("setImageSource should return false if the handler doesn't accept the collision") {
    val handler = collisionHandler
    val currentImage = ImageStorage.fill(8, Color.Blue)
    val newImage = ImageStorage.fill(8, Color.Yellow)
    val location = SaveLocation(null)
    val info = ImagePool.SaveInfo(null)

    val p = new ImagePool()
    val grid = new ImageGrid(8)

    when(handler.shouldReplaceImage(currentImage, newImage, location)).thenReturn(None)

    grid.setImageSource(currentImage, location, info)(p, handler)
    assertEquals(grid.setImageSource(newImage, location, info)(p, handler), false)
  }

  test(
    "setImageSource should replace the current image and return true if the handler wants to replace it"
  ) {
    val handler = collisionHandler

    val currentImage = ImageStorage.fill(8, Color.Blue)
    val newImage = ImageStorage.fill(8, Color.Yellow)
    val location = SaveLocation(null)
    val info = ImagePool.SaveInfo(null)

    val p = new ImagePool()
    val grid = new ImageGrid(8)

    when(handler.shouldReplaceImage(currentImage, newImage, location)).thenReturn(Some(true))

    grid.setImageSource(currentImage, location, info)(p, handler)
    assertEquals(grid.setImageSource(newImage, location, info)(p, handler), true)
    assertEquals(p.locationOf(currentImage), None)
    assertEquals(p.locationOf(newImage), Some(location))
  }

  test(
    "setImageSource should keep the current image and return true if the handler wants to keep it"
  ) {
    val handler = collisionHandler

    val currentImage = ImageStorage.fill(8, Color.Blue)
    val newImage = ImageStorage.fill(8, Color.Yellow)
    val location = SaveLocation(null)
    val info = ImagePool.SaveInfo(null)

    val p = new ImagePool()
    val grid = new ImageGrid(8)

    when(handler.shouldReplaceImage(currentImage, newImage, location)).thenReturn(Some(false))

    grid.setImageSource(currentImage, location, info)(p, handler)
    assertEquals(grid.setImageSource(newImage, location, info)(p, handler), true)
    assertEquals(p.locationOf(currentImage), Some(location))
    assertEquals(p.locationOf(newImage), None)

  }
}
