package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.model.image.SaveLocation
import com.martomate.tripaint.view.{FileOpenSettings, TriPaintView}

import java.io.File
import scala.util.{Failure, Success}

class OpenAction(
    model: TriPaintModel,
    askForFileToOpen: () => Option[File],
    askForFileOpenSettings: (File, Int, Int, Int) => Option[FileOpenSettings],
    askForWhereToPutImage: () => Option[(Int, Int)]
) extends Action {
  override def perform(): Unit = {
    val imageSize = model.imageGrid.imageSize
    for {
      file <- askForFileToOpen()
      FileOpenSettings(offset, format) <- askForFileOpenSettings(file, imageSize, 1, 1)
      coords <- askForWhereToPutImage()
    } model.imagePool.fromFile(
      SaveLocation(file, offset),
      format,
      imageSize,
      model.fileSystem
    ) match {
      case Success(storage) =>
        val imageCoords = TriImageCoords(coords._1, coords._2)
        val image = makeImageContent(model, imageCoords, storage)
        addImage(model, image)
      case Failure(exc) =>
        exc.printStackTrace()
    }
  }
}
