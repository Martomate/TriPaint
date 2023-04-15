package com.martomate.tripaint.control.action

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.model.image.{ImagePool, ImageSaveCollisionHandler, ImageStorage}
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.view.{FileSaveSettings, TriPaintView}

import java.io.File

abstract class Action {
  def perform(): Unit

  protected def save(imagePool: ImagePool, images: Seq[ImageContent], fileSystem: FileSystem)(
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

  protected def saveAs(imagePool: ImagePool, image: ImageContent, fileSystem: FileSystem)(
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
}
