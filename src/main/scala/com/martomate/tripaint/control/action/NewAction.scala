package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.{Color, TriPaintModel}
import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.view.TriPaintView

object NewAction extends Action {
  override def perform(model: TriPaintModel, view: TriPaintView): Unit = {
    view.askForWhereToPutImage() match {
      case Some((x, y)) =>
        val storage = model.imagePool.fromBGColor(Color.fromFXColor(view.backgroundColor), model.imageGrid.imageSize)
        val content = makeImageContent(model, TriImageCoords(x, y), storage)
        addImage(model, content)
      case _ =>
    }
  }
}
