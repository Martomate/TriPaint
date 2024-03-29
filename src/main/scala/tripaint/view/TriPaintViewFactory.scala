package tripaint.view

import tripaint.model.TriPaintModel

trait TriPaintViewFactory {
  def createView(controls: TriPaintViewListener, model: TriPaintModel): TriPaintView
}
