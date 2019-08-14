package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.TriPaintModel
import com.martomate.tripaint.view.TriPaintView

object SaveAsAction extends Action {
  override def perform(model: TriPaintModel, view: TriPaintView): Unit = {
    allSelectedImages(model).foreach(im => saveAs(model, view, im))
  }
}
