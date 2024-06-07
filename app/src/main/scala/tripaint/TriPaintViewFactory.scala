package tripaint

import tripaint.view.{TriPaintView, TriPaintViewListener}

trait TriPaintViewFactory {
  def createView(controls: TriPaintViewListener, model: TriPaintModel): TriPaintView
}
