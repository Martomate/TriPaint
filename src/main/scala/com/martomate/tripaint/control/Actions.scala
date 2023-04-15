package com.martomate.tripaint.control

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model.{Color, ImageGrid, TriPaintModel}
import com.martomate.tripaint.model.coords.{StorageCoords, TriImageCoords}
import com.martomate.tripaint.model.effects.Effect
import com.martomate.tripaint.model.image.{ImagePool, ImageSaveCollisionHandler, ImageStorage}
import com.martomate.tripaint.model.image.content.{ImageChange, ImageContent, PixelChange}
import com.martomate.tripaint.view.{FileOpenSettings, FileSaveSettings}

import java.io.File
import scala.util.{Failure, Success}

object Actions {
  def save(imagePool: ImagePool, images: Seq[ImageContent], fileSystem: FileSystem)(
      askForSaveFile: ImageContent => Option[File],
      askForFileSaveSettings: (File, ImageContent) => Option[FileSaveSettings],
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

  def saveAs(imagePool: ImagePool, image: ImageContent, fileSystem: FileSystem)(
      askForSaveFile: ImageContent => Option[File],
      askForFileSaveSettings: (File, ImageContent) => Option[FileSaveSettings],
      imageSaveCollisionHandler: ImageSaveCollisionHandler
  ): Boolean = {
    askForSaveFile(image) flatMap { file =>
      askForFileSaveSettings(file, image) map { settings =>
        if (
          imagePool.move(
            image.storage,
            ImagePool.SaveLocation(file, settings.offset),
            ImagePool.SaveInfo(settings.format)
          )(imageSaveCollisionHandler)
        ) {
          val saved = imagePool.save(image.storage, fileSystem)
          if (!saved) println("Image could not be saved!!")
          saved
        } else false
      }
    } getOrElse false
  }

  def createNewImage(imageGrid: ImageGrid, backgroundColor: Color, coords: TriImageCoords): Unit =
    imageGrid.set(
      new ImageContent(coords, ImageStorage.fromBGColor(backgroundColor, imageGrid.imageSize))
    )

  def openImage(
      model: TriPaintModel,
      file: File,
      fileOpenSettings: FileOpenSettings,
      whereToPutImage: TriImageCoords
  ): Unit =
    val FileOpenSettings(offset, format) = fileOpenSettings
    val location = ImagePool.SaveLocation(file, offset)
    val imageSize = model.imageGrid.imageSize

    model.imagePool.fromFile(location, format, imageSize, model.fileSystem) match {
      case Success(storage) => model.imageGrid.set(new ImageContent(whereToPutImage, storage))
      case Failure(exc)     => exc.printStackTrace()
    }

  def openHexagon(
      model: TriPaintModel,
      file: File,
      fileOpenSettings: FileOpenSettings,
      coords: TriImageCoords
  ): Unit =
    val imageSize = model.imageGrid.imageSize
    val FileOpenSettings(offset, format) = fileOpenSettings

    def coordOffset(idx: Int): (Int, Int) = {
      idx match {
        case 0 => (0, 0)
        case 1 => (-1, 0)
        case 2 => (-2, 0)
        case 3 => (-1, -1)
        case 4 => (0, -1)
        case 5 => (1, -1)
      }
    }

    for (idx <- 0 until 6) {
      val imageOffset = StorageCoords(offset.x + idx * imageSize, offset.y)
      val location = ImagePool.SaveLocation(file, imageOffset)

      model.imagePool.fromFile(location, format, imageSize, model.fileSystem) match {
        case Success(storage) =>
          val off = coordOffset(idx)
          val imageCoords = TriImageCoords(coords.x + off._1, coords.y + off._2)
          model.imageGrid.set(new ImageContent(imageCoords, storage))
        case Failure(exc) =>
          exc.printStackTrace()
      }
    }

  def applyEffect(model: TriPaintModel, effect: Effect): Unit =
    val grid = model.imageGrid
    val im = grid.selectedImages

    val storages = im.map(_.storage)
    val allPixels = storages.map(_.allPixels)
    val before = allPixels.zip(storages).map(a => a._1.map(a._2(_)))

    effect.action(im.map(_.coords), grid)

    val after = allPixels.zip(storages).map(a => a._1.map(a._2(_)))

    for (here <- storages.indices) {
      val changed = for {
        neigh <- allPixels(here).indices
        if before(here)(neigh) != after(here)(neigh)
      } yield PixelChange(allPixels(here)(neigh), before(here)(neigh), after(here)(neigh))

      if (changed.nonEmpty) {
        val change = new ImageChange(effect.name, im(here), changed)
        im(here).undoManager.append(change)
        im(here).tellListenersAboutBigChange()
      }
    }
}
