package tripaint.view

import tripaint.grid.GridCell
import tripaint.view.gui.UIAction

trait TriPaintViewListener {
  def perform(action: UIAction): Unit

  /** Returns whether to exit or not */
  def requestExit(): Boolean
  def requestImageRemoval(image: GridCell): Unit
}
