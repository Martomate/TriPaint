package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.{SaveLocation, TriPaintModel}
import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.view.TriPaintView

import scala.util.{Failure, Success}

object OpenAction extends Action {
  override def perform(model: TriPaintModel, view: TriPaintView): Unit = {
    val imageSize = model.imageGrid.imageSize
    for {
      file <- view.askForFileToOpen()
      offset <- view.askForOffset(file, imageSize, imageSize)
      coords <- view.askForWhereToPutImage()
    } model.imagePool.fromFile(SaveLocation(file, offset), imageSize) match {
      case Success(storage) =>
        val imageCoords = TriImageCoords(coords._1, coords._2)
        val image = makeImageContent(model, imageCoords, storage)
        addImage(model, image)
      case Failure(exc) =>
        exc.printStackTrace()
    }
  }
}
