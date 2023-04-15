package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.model.image.ImagePool
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.view.{FileOpenSettings, TriPaintView}

import java.io.File
import scala.util.{Failure, Success}

class OpenAction(
    model: TriPaintModel,
    file: File,
    fileOpenSettings: FileOpenSettings,
    whereToPutImage: TriImageCoords
) extends Action {
  override def perform(): Unit = {
    val FileOpenSettings(offset, format) = fileOpenSettings
    val location = ImagePool.SaveLocation(file, offset)
    val imageSize = model.imageGrid.imageSize

    model.imagePool.fromFile(location, format, imageSize, model.fileSystem) match {
      case Success(storage) => model.imageGrid.set(new ImageContent(whereToPutImage, storage))
      case Failure(exc)     => exc.printStackTrace()
    }
  }
}
