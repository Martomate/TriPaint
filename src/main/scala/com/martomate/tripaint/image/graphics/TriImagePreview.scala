package com.martomate.tripaint.image.graphics

import scalafx.scene.SnapshotParameters
import scalafx.scene.image.Image
import scalafx.scene.layout.Pane

class TriImagePreview(width: Double, val image: TriImage) extends Pane with TriImageView {
  private[image] val canvas: TriImageCanvas = new TriImageCanvas(width)

  children add canvas
  image.addListener(this)
  image.redraw(false)

  def toImage(params: SnapshotParameters): Image = canvas.snapshot(params, null)
}
