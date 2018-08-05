package com.martomate.tripaint.image.graphics

import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color

class TriImageCanvas(init_width: Double) extends Canvas(init_width, init_width * Math.sqrt(3) / 2) {
  object points {
    val x: Array[Double] = new Array(3)
    val y: Array[Double] = new Array(3)
  }

  def clearCanvas(): Unit = graphicsContext2D.clearRect(0, 0, width(), height())

  def storeNormalizedCoords(index: Int, xx: Double, yy: Double): Unit = {
    points.x(index) = xx * width()
    points.y(index) = yy * height()
  }

  def drawTriangle(color: Color, strokeInstead: Boolean): Unit = {
    val gc = graphicsContext2D
    if (strokeInstead) {
      gc.setStroke(color)
      gc.strokePolygon(points.x, points.y, 3)
    } else {
      gc.setFill(color)
      gc.fillPolygon(points.x, points.y, 3)
    }
  }
}

