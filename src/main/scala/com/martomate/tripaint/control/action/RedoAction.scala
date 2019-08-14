package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.view.TriPaintView

object RedoAction extends Action {
  override def perform(model: TriPaintModel, view: TriPaintView): Unit = {
    model.imageGrid.images.foreach(_.redo())
  }
}
