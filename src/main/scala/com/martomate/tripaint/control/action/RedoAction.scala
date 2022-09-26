package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.view.TriPaintView

class RedoAction(model: TriPaintModel) extends Action {
  override def perform(): Unit = {
    model.imageGrid.images.foreach(_.redo())
  }
}
