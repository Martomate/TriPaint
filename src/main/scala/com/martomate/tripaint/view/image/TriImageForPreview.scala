package com.martomate.tripaint.view.image

import com.martomate.tripaint.model.content.ImageContent
import com.martomate.tripaint.model.coords.TriangleCoords
import com.martomate.tripaint.model.storage.ImageStorage
import scalafx.scene.SnapshotParameters
import scalafx.scene.image.Image
import scalafx.scene.layout.Pane

class TriImageForPreview (val content: ImageContent, previewWidth: Double) extends Pane with ITriImage {

  private def storage: ImageStorage = content.storage

  private val canvas: TriImageCanvas = new TriImageCanvas(previewWidth, storage.imageSize)

  content.changeTracker.addListener(this)
  children add canvas

  redraw()

  override protected def drawTriangle(coords: TriangleCoords): Unit = drawTriangleImpl(coords)

  private def drawTriangleImpl(coords: TriangleCoords, strokeInstead: Boolean = false): Unit = {
    canvas.drawTriangle(coords, storage(coords), strokeInstead)
  }

  override def redraw(): Unit = {
    canvas.clearCanvas()

    storage.allPixels.foreach(c => drawTriangleImpl(c, strokeInstead = true))
    storage.allPixels.foreach(c => drawTriangleImpl(c))
  }

  def toImage(params: SnapshotParameters): Image = canvas.snapshot(params, null)

  override def onDrawActionFinished(): Unit = ()
}
