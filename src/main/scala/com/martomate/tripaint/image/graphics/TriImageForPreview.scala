package com.martomate.tripaint.image.graphics

import com.martomate.tripaint.image.content.ImageContent
import com.martomate.tripaint.image.coords.TriangleCoords
import com.martomate.tripaint.image.storage.ImageStorage
import scalafx.scene.SnapshotParameters
import scalafx.scene.image.Image
import scalafx.scene.layout.Pane

class TriImageForPreview (val content: ImageContent, previewWidth: Double) extends Pane with ITriImage {

  private def storage: ImageStorage = content.storage

  private val canvas: TriImageCanvas = new TriImageCanvas(previewWidth)

  content.changeTracker.addListener(this)
  children add canvas

  redraw()

  override protected def drawTriangle(coords: TriangleCoords): Unit = drawTriangleImpl(coords)

  private[image] def drawTriangleImpl(coords: TriangleCoords, strokeInstead: Boolean = false): Unit = {
    val yp = coords.y
    val xp = coords.x * 0.5 - (yp - storage.imageSize + 1) * 0.5

    storeAllCoords(xp, yp, coords.x % 2 == 1)

    canvas.drawTriangle(storage(coords), strokeInstead)
  }

  override def redraw(): Unit = {
    canvas.clearCanvas()

    for (y <- 0 until storage.imageSize) {
      for (x <- 0 until y * 2 + 1) {
        drawTriangleImpl(TriangleCoords(x, y), strokeInstead = true)
      }
    }

    for (y <- 0 until storage.imageSize) {
      for (x <- 0 until y * 2 + 1) {
        drawTriangleImpl(TriangleCoords(x, y))
      }
    }
  }

  private def storeAllCoords(xp: Double, yp: Double, upsideDown: Boolean): Unit = {
    if (upsideDown) {
      storeCoords(0, xp,       yp)
      storeCoords(1, xp + 1.0, yp)
      storeCoords(2, xp + 0.5, yp + 1.0)
    } else {
      storeCoords(0, xp,       yp + 1.0)
      storeCoords(1, xp + 1.0, yp + 1.0)
      storeCoords(2, xp + 0.5, yp)
    }
  }

  private def storeCoords(index: Int, xPos: Double, yPos: Double): Unit = {
    val xx = xPos / storage.imageSize
    val yy = yPos / storage.imageSize

    canvas.storeNormalizedCoords(index, xx, yy)
  }

  def toImage(params: SnapshotParameters): Image = canvas.snapshot(params, null)
}
