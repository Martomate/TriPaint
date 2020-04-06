package com.martomate.tripaint.view

import com.martomate.tripaint.model.TriPaintModel

trait TriPaintViewFactory {
  def createView(controls: TriPaintViewListener, model: TriPaintModel): TriPaintView
}
