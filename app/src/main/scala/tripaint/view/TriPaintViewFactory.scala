package tripaint.view

import tripaint.TriPaintModel

trait TriPaintViewFactory {
  def createView(controls: TriPaintViewListener, model: TriPaintModel): TriPaintView
}
