package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.model.image.SaveLocation
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.model.image.pool.{ImageSaveCollisionHandler, SaveInfo}
import com.martomate.tripaint.model.image.storage.ImageStorage
import com.martomate.tripaint.view.{FileSaveSettings, TriPaintView}

import java.io.File

abstract class Action {
  def perform(): Unit

  protected def addImage(model: TriPaintModel, newImage: ImageContent): Unit = {
    if (newImage != null) {
      model.imageGrid.set(newImage)
    }
  }

  protected def makeImageContent(
      model: TriPaintModel,
      coords: TriImageCoords,
      storage: ImageStorage
  ): ImageContent = {
    val image = new ImageContent(coords, storage)
    model.imagePool.addListener(image)
    image
  }

  protected def allImages(model: TriPaintModel): Seq[ImageContent] = {
    model.imageGrid.images
  }

  protected def allSelectedImages(model: TriPaintModel): Seq[ImageContent] = {
    model.imageGrid.selectedImages
  }

  protected def save(model: TriPaintModel, images: ImageContent*)(
      askForSaveFile: ImageContent => Option[File],
      askForFileSaveSettings: (File, ImageContent) => Option[FileSaveSettings],
      imageSaveCollisionHandler: ImageSaveCollisionHandler
  ): Boolean = {
    images
      .filter(im => !model.imagePool.save(im.storage, model.imageSaver, model.fileSystem))
      .forall(im =>
        model.imagePool.save(im.storage, model.imageSaver, model.fileSystem) ||
          saveAs(model, im)(askForSaveFile, askForFileSaveSettings, imageSaveCollisionHandler)
      )
  }

  protected def saveAs(model: TriPaintModel, image: ImageContent)(
      askForSaveFile: ImageContent => Option[File],
      askForFileSaveSettings: (File, ImageContent) => Option[FileSaveSettings],
      imageSaveCollisionHandler: ImageSaveCollisionHandler
  ): Boolean = {
    askForSaveFile(image) flatMap { file =>
      askForFileSaveSettings(file, image) map { settings =>
        saveImageAs(model, image, file, settings)(imageSaveCollisionHandler)
      }
    } getOrElse false
  }

  private def saveImageAs(
      model: TriPaintModel,
      image: ImageContent,
      file: File,
      settings: FileSaveSettings
  )(imageSaveCollisionHandler: ImageSaveCollisionHandler) = {
    val location = SaveLocation(file, settings.offset)
    val info = SaveInfo(settings.format)

    if (model.imagePool.move(image.storage, location, info)(imageSaveCollisionHandler)) {
      val saved = model.imagePool.save(image.storage, model.imageSaver, model.fileSystem)
      if (!saved) println("Image could not be saved!!")
      saved
    } else false
  }

  protected def saveBeforeClosing(
      askSaveBeforeClosing: Seq[ImageContent] => Option[Boolean],
      images: ImageContent*
  ): Option[Boolean] = {
    askSaveBeforeClosing(images)
  }
}
