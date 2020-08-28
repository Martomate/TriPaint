package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.{SaveLocation, TriPaintModel}
import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.view.{FileOpenSettings, TriPaintView}

import scala.util.{Failure, Success}

object OpenHexagonAction extends Action {
  override def perform(model: TriPaintModel, view: TriPaintView): Unit = {
    val imageSize = model.imageGrid.imageSize
    for {
      file <- view.askForFileToOpen()
      FileOpenSettings(offset, format) <- view.askForFileOpenSettings(file, imageSize * 6, imageSize)
      coords <- view.askForWhereToPutImage()
    } for (idx <- 0 until 6) {
      model.imagePool.fromFile(SaveLocation(file, (offset._1 + idx * imageSize, offset._2)), format, imageSize) match {
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
