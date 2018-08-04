package com.martomate.tripaint.image.graphics

import java.awt.image.BufferedImage

import com.martomate.tripaint.image.coords.TriangleCoords
import scalafx.scene.canvas.Canvas

class IndexMap(canvas: Canvas, init_zoom: Double) extends IndexMapper {
  val xInt:  Array[Int] = new Array(3)
  val yInt:  Array[Int] = new Array(3)
  val image: BufferedImage = new BufferedImage(
    Math.ceil(canvas.width()  / init_zoom * 3).toInt,
    Math.ceil(canvas.height() / init_zoom * 3).toInt,
    BufferedImage.TYPE_INT_RGB
  )

  def coordsAt(x: Double, y: Double): TriangleCoords = {
    val xx = (x / canvas.width()  * image.getWidth ).toInt
    val yy = (y / canvas.height() * image.getHeight).toInt

    if (xx >= 0 && xx < image.getWidth() && yy >= 0 && yy < image.getHeight())
      TriangleCoords.fromInt((image.getRGB(xx, yy) & 0xffffff) - 1)
    else null
  }

  def performIndexMapping(coords: TriangleCoords): Unit = {
    val indexGraphics = image.getGraphics
    val indexColor = new java.awt.Color(coords.toInt + 1)
    indexGraphics.setColor(indexColor)
    indexGraphics.drawPolygon(xInt, yInt, 3)
    indexGraphics.fillPolygon(xInt, yInt, 3)
  }

  def storeCoords(index: Int, xx: Double, yy: Double): Unit = {
    xInt(index) = Math.round(xx * image.getWidth).toInt
    yInt(index) = Math.round(yy * image.getHeight).toInt
  }
}
