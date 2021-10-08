package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.view.TriPaintView

object SaveAction extends Action {
  override def perform(model: TriPaintModel, view: TriPaintView): Unit = {
    save(model, view, allSelectedImages(model).filter(_.changed): _*)
  }
}
