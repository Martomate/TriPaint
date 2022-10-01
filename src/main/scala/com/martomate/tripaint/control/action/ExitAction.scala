package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.model.image.pool.ImageSaveCollisionHandler
import com.martomate.tripaint.view.{FileSaveSettings, TriPaintView}

import java.io.File

class ExitAction(
    model: TriPaintModel,
    askForSaveFile: (ImageContent) => Option[File],
    askForFileSaveSettings: (File, ImageContent) => Option[FileSaveSettings],
    imageSaveCollisionHandler: ImageSaveCollisionHandler,
    askSaveBeforeClosing: Seq[ImageContent] => Option[Boolean],
    close: () => Unit
) extends Action {
  override def perform(): Unit = {
    if (do_exit()) close()
  }

  def do_exit(): Boolean = {
    allImages(model).filter(_.changed) match {
      case Seq() => true
      case images =>
        saveBeforeClosing(askSaveBeforeClosing, images: _*) match {
          case Some(shouldSave) =>
            if (shouldSave)
              save(model, images: _*)(
                askForSaveFile,
                askForFileSaveSettings,
                imageSaveCollisionHandler
              )
            else true
          case None => false
        }
    }
  }
}
