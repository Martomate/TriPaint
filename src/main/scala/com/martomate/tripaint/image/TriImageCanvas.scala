package com.martomate.tripaint.image

import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color

class TriImageCanvas(init_width: Double) extends Canvas(init_width, init_width * Math.sqrt(3) / 2) {
  object points {
    val x: Array[Double] = new Array(3)
    val y: Array[Double] = new Array(3)
  }

  def clearCanvas(): Unit = graphicsContext2D.clearRect(0, 0, width(), height())

  def storeCoords(index: Int, xx: Double, yy: Double): Unit = {
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

class TriImageActualCanvas(init_width: Double) extends TriImageCanvas(init_width) {
  def updateLocation(panX: Double, panY: Double): Unit = {
    // adjustment caused by canvas center not being the wanted rotation center (i.e. the centroid)
    val adjLen = height() / 6
    val angle = rotate() / 180 * math.Pi
    val (dx, dy) = (-adjLen * math.sin(angle), -adjLen * math.cos(angle))
    relocate(-width() / 2 + panX + dx, -height() / 2 + panY + dy)
  }

  def updateCanvasSize(imageSize: Int, zoom: Double): Unit = {
    width = (imageSize * 2 + 1) * zoom
    height = width() * Math.sqrt(3) / 2
  }
}
