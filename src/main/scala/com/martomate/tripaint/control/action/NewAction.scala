package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.{Color, TriPaintModel}
import com.martomate.tripaint.model.coords.TriImageCoords

class NewAction(model: TriPaintModel, backgroundColor: Color, askForWhereToPutImage: () => Option[(Int, Int)]) extends Action {
  override def perform(): Unit = {
    askForWhereToPutImage() match {
      case Some((x, y)) =>
        val storage = model.imagePool.fromBGColor(backgroundColor, model.imageGrid.imageSize)
        val content = makeImageContent(model, TriImageCoords(x, y), storage)
        addImage(model, content)
      case _ =>
    }
  }
}
