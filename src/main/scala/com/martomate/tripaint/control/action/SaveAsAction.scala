package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.model.image.ImageSaveCollisionHandler
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.view.{FileSaveSettings, TriPaintView}

import java.io.File

class SaveAsAction(
    model: TriPaintModel,
    askForSaveFile: ImageContent => Option[File],
    askForFileSaveSettings: (File, ImageContent) => Option[FileSaveSettings],
    imageSaveCollisionHandler: ImageSaveCollisionHandler
) {
  def perform(): Unit = {
    model.imageGrid.selectedImages.foreach(im =>
      Action.saveAs(model.imagePool, im, model.fileSystem)(
        askForSaveFile,
        askForFileSaveSettings,
        imageSaveCollisionHandler
      )
    )
  }
}
