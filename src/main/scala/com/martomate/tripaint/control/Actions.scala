package com.martomate.tripaint.control

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model.{Color, ImageGrid, TriPaintModel}
import com.martomate.tripaint.model.coords.{StorageCoords, GridCoords}
import com.martomate.tripaint.model.effects.Effect
import com.martomate.tripaint.model.image.{ImagePool, ImageSaveCollisionHandler, ImageStorage}
import com.martomate.tripaint.model.image.content.{ImageChange, GridCell, PixelChange}
import com.martomate.tripaint.view.{FileOpenSettings, FileSaveSettings}

import java.io.File
import scala.util.{Failure, Success}

object Actions {
  def save(imagePool: ImagePool, images: Seq[GridCell], fileSystem: FileSystem)(
      askForSaveFile: GridCell => Option[File],
      askForFileSaveSettings: (File, GridCell) => Option[FileSaveSettings],
      imageSaveCollisionHandler: ImageSaveCollisionHandler
  ): Boolean = {
    images
      .filter(im => !imagePool.save(im.storage, fileSystem))
      .forall(im =>
        imagePool.save(im.storage, fileSystem) ||
          saveAs(imagePool, im, fileSystem)(
            askForSaveFile,
            askForFileSaveSettings,
            imageSaveCollisionHandler
          )
      )
  }

  def saveAs(imagePool: ImagePool, image: GridCell, fileSystem: FileSystem)(
      askForSaveFile: GridCell => Option[File],
      askForFileSaveSettings: (File, GridCell) => Option[FileSaveSettings],
      imageSaveCollisionHandler: ImageSaveCollisionHandler
  ): Boolean =
    val didMoveOpt = for
      file <- askForSaveFile(image)
      settings <- askForFileSaveSettings(file, image)
    yield
      val location = ImagePool.SaveLocation(file, settings.offset)
      val info = ImagePool.SaveInfo(settings.format)
      imagePool.move(image.storage, location, info)(using imageSaveCollisionHandler)

    val didMove = didMoveOpt.getOrElse(false)

    if didMove then
      val saved = imagePool.save(image.storage, fileSystem)
      if (!saved) println("Image could not be saved!!")
      saved
    else false

  def createNewImage(imageGrid: ImageGrid, backgroundColor: Color, coords: GridCoords): Unit =
    val storage = ImageStorage.fill(imageGrid.imageSize, backgroundColor)
    imageGrid.set(new GridCell(coords, storage))

  def openImage(
      model: TriPaintModel,
      file: File,
      fileOpenSettings: FileOpenSettings,
      whereToPutImage: GridCoords
  ): Unit =
    val FileOpenSettings(offset, format) = fileOpenSettings
    val location = ImagePool.SaveLocation(file, offset)
    val imageSize = model.imageGrid.imageSize

    model.imagePool.fromFile(location, format, imageSize, model.fileSystem) match
      case Success(storage) => model.imageGrid.set(new GridCell(whereToPutImage, storage))
      case Failure(exc)     => exc.printStackTrace()

  def openHexagon(
      model: TriPaintModel,
      file: File,
      fileOpenSettings: FileOpenSettings,
      coords: GridCoords
  ): Unit =
    val imageSize = model.imageGrid.imageSize
    val FileOpenSettings(offset, format) = fileOpenSettings

    def coordOffset(idx: Int): (Int, Int) =
      idx match
        case 0 => (0, 0)
        case 1 => (-1, 0)
        case 2 => (-2, 0)
        case 3 => (-1, -1)
        case 4 => (0, -1)
        case 5 => (1, -1)

    for idx <- 0 until 6 do
      val imageOffset = StorageCoords(offset.x + idx * imageSize, offset.y)
      val location = ImagePool.SaveLocation(file, imageOffset)

      model.imagePool.fromFile(location, format, imageSize, model.fileSystem) match
        case Success(storage) =>
          val off = coordOffset(idx)
          val imageCoords = GridCoords(coords.x + off._1, coords.y + off._2)
          model.imageGrid.set(new GridCell(imageCoords, storage))
        case Failure(exc) =>
          exc.printStackTrace()

  def applyEffect(model: TriPaintModel, effect: Effect): Unit =
    val grid = model.imageGrid
    val im = grid.selectedImages

    val storages = im.map(_.storage)
    val allPixels = storages.map(_.allPixels)
    val before = allPixels.zip(storages).map((px, st) => px.map(st.getColor))

    effect.action(im.map(_.coords), grid)

    val after = allPixels.zip(storages).map((px, st) => px.map(st.getColor))

    for here <- storages.indices do
      val changed =
        for
          neigh <- allPixels(here).indices
          if before(here)(neigh) != after(here)(neigh)
        yield PixelChange(
          allPixels(here)(neigh),
          before(here)(neigh),
          after(here)(neigh)
        )

      if changed.nonEmpty then
        im(here).appendChange(new ImageChange(effect.name, im(here).storage, changed))
}
