package com.martomate.tripaint.view.image

import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.model.coords.TriangleCoords
import com.martomate.tripaint.model.image.storage.ImageStorage
import scalafx.scene.SnapshotParameters
import scalafx.scene.image.Image
import scalafx.scene.layout.Pane

class TriImageForPreview (content: ImageContent, previewWidth: Double) extends Pane with ITriImage {

  private def storage: ImageStorage = content.storage

  private val canvas: TriImageCanvas = new TriImageCanvas(previewWidth, storage.imageSize)

  content.addListener(this)
  children add canvas

  redraw()

  override protected def drawTriangle(coords: TriangleCoords): Unit = drawTriangleImpl(coords)

  private def drawTriangleImpl(coords: TriangleCoords): Unit = {
    canvas.drawTriangle(coords, storage(coords), storage)
  }

  override def redraw(): Unit = {
    canvas.clearCanvas()

    storage.allPixels.foreach(c => drawTriangleImpl(c))
  }

  def toImage(params: SnapshotParameters): Image = canvas.snapshot(params, null)
}

object TriImageForPreview {
  val previewSize = 64
}