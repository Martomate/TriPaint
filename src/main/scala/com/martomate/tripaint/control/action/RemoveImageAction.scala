package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.model.image.pool.ImageSaveCollisionHandler
import com.martomate.tripaint.view.{FileSaveSettings, TriPaintView}

import java.io.File

class RemoveImageAction(image: ImageContent,
                        model: TriPaintModel,
                        askForSaveFile: (ImageContent) => Option[File],
                        askForFileSaveSettings: (File, ImageContent) => Option[FileSaveSettings],
                        imageSaveCollisionHandler: ImageSaveCollisionHandler,
                        askSaveBeforeClosing: Seq[ImageContent] => Option[Boolean]
                       ) extends Action {
  override def perform(): Unit = {
    var abortRemoval = false
    if (image.changed) {
      saveBeforeClosing(askSaveBeforeClosing, image) match {
        case Some(shouldSave) =>
          if (shouldSave && !save(model, image)(askForSaveFile, askForFileSaveSettings, imageSaveCollisionHandler)) abortRemoval = true
        case None => abortRemoval = true
      }
    }

    if (!abortRemoval) {
      removeImageAt(model, image.coords)
    }
  }

  private def removeImageAt(model: TriPaintModel, coords: TriImageCoords): Unit = model.imageGrid -= coords
}
