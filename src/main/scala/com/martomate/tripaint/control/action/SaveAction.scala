package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.model.image.ImageSaveCollisionHandler
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.view.{FileSaveSettings, TriPaintView}

import java.io.File

class SaveAction(
    model: TriPaintModel,
    askForSaveFile: ImageContent => Option[File],
    askForFileSaveSettings: (File, ImageContent) => Option[FileSaveSettings],
    imageSaveCollisionHandler: ImageSaveCollisionHandler
) extends Action {
  override def perform(): Unit = {
    save(model, allSelectedImages(model).filter(_.changed): _*)(
      askForSaveFile,
      askForFileSaveSettings,
      imageSaveCollisionHandler
    )
  }
}
