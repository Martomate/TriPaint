package com.martomate.tripaint.view.image

import com.martomate.tripaint.model.coords.TriangleCoords
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color

class TriImageCanvas(init_width: Double, imageSize: Int) extends Canvas(init_width, init_width * Math.sqrt(3) / 2) {
  private val coordsToRealConverter = new TriangleCoordsToReal[Double](imageSize, new Array(_), (xx, yy) => (xx * width(), yy * height()))

  def clearCanvas(): Unit = graphicsContext2D.clearRect(0, 0, width(), height())

  def drawTriangle(coords: TriangleCoords, color: Color, strokeInstead: Boolean): Unit = {
    val (px, py) = coordsToRealConverter.triangleCornerPoints(coords)

    val gc = graphicsContext2D
    if (strokeInstead) {
      gc.setStroke(color)
      gc.strokePolygon(px, py, 3)
    } else {
      gc.setFill(color)
      gc.fillPolygon(px, py, 3)
    }
  }
}

