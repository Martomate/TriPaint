package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.model.coords.{StorageCoords, TriImageCoords}
import com.martomate.tripaint.model.image.ImagePool
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.view.{FileOpenSettings, TriPaintView}

import java.io.File
import scala.util.{Failure, Success}

class OpenHexagonAction(
    model: TriPaintModel,
    file: File,
    fileOpenSettings: FileOpenSettings,
    coords: TriImageCoords
) {
  def perform(): Unit = {
    val imageSize = model.imageGrid.imageSize
    val FileOpenSettings(offset, format) = fileOpenSettings

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
  }

  private def coordOffset(idx: Int): (Int, Int) = {
    idx match {
      case 0 => (0, 0)
      case 1 => (-1, 0)
      case 2 => (-2, 0)
      case 3 => (-1, -1)
      case 4 => (0, -1)
      case 5 => (1, -1)
    }
  }
}
