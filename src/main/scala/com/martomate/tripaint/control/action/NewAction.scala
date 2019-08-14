package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.view.TriPaintView
import scalafx.scene.paint.Color

object NewAction extends Action {
  override def perform(model: TriPaintModel, view: TriPaintView): Unit = {
    view.askForWhereToPutImage() match {
      case Some((x, y)) =>
        addImage(model, makeImageContent(model, TriImageCoords(x, y), model.imagePool.fromBGColor(new Color(view.backgroundColor), model.imageGrid.imageSize)))
      case _ =>
    }
  }
}
