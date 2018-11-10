package com.martomate.tripaint.image.graphics

class TriImageActualCanvas(init_width: Double, _imageSize: Int) extends TriImageCanvas(init_width, _imageSize) {
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
