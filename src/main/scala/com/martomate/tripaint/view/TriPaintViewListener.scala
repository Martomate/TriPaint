package com.martomate.tripaint.view

import com.martomate.tripaint.model.image.content.GridCell
import com.martomate.tripaint.view.gui.UIAction

trait TriPaintViewListener {
  def perform(action: UIAction): Unit

  /** Returns whether to exit or not */
  def requestExit(): Boolean
  def requestImageRemoval(image: GridCell): Unit
}
