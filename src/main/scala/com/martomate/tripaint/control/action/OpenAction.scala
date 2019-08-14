package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.{SaveLocation, TriPaintModel}
import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.view.TriPaintView

import scala.util.{Failure, Success}

object OpenAction extends Action {
  override def perform(model: TriPaintModel, view: TriPaintView): Unit = {
    view.askForFileToOpen() foreach { file =>
      val imageSize = model.imageGrid.imageSize
      val offset = view.askForOffset().getOrElse(0, 0)

      model.imagePool.fromFile(SaveLocation(file, offset), imageSize) match {
        case Success(storage) =>
          view.askForWhereToPutImage() foreach { coords =>
            val image = makeImageContent(model, TriImageCoords(coords._1, coords._2), storage)
            addImage(model, image)
          }
        case Failure(exc) =>
          exc.printStackTrace()
      }
    }
  }
}
