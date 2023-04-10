package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.model.coords.{StorageCoords, TriImageCoords}
import com.martomate.tripaint.model.image.ImagePool
import com.martomate.tripaint.view.{FileOpenSettings, TriPaintView}

import java.io.File
import scala.util.{Failure, Success}

class OpenHexagonAction(
    model: TriPaintModel,
    askForFileToOpen: () => Option[File],
    askForFileOpenSettings: (File, Int, Int, Int) => Option[FileOpenSettings],
    askForWhereToPutImage: () => Option[(Int, Int)]
) extends Action {
  override def perform(): Unit = {
    val imageSize = model.imageGrid.imageSize
    for {
      file <- askForFileToOpen()
      FileOpenSettings(offset, format) <- askForFileOpenSettings(file, imageSize, 6, 1)
      coords <- askForWhereToPutImage()
    } for (idx <- 0 until 6) {
      model.imagePool.fromFile(
        ImagePool.SaveLocation(file, StorageCoords(offset.x + idx * imageSize, offset.y)),
        format,
        imageSize,
        model.fileSystem
      ) match {
        case Success(storage) =>
          val off = coordOffset(idx)
          val imageCoords = TriImageCoords(coords._1 + off._1, coords._2 + off._2)
          val image = makeImageContent(model, imageCoords, storage)
          addImage(model, image)
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
