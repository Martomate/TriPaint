package com.martomate.tripaint.view.image

import java.awt.image.BufferedImage

import com.martomate.tripaint.model.coords.TriangleCoords
import scalafx.scene.canvas.Canvas

class IndexMap(canvas: Canvas, init_zoom: Double, imageSize: Int) extends IndexMapper {
  private val image: BufferedImage = new BufferedImage(
    Math.ceil(canvas.width()  / init_zoom * 3).toInt,
    Math.ceil(canvas.height() / init_zoom * 3).toInt,
    BufferedImage.TYPE_INT_RGB
  )

  private val coordsToRealConverter = new TriangleCoordsToReal[Int](imageSize, new Array(_), (xx, yy) => (
    Math.round(xx * image.getWidth).toInt,
    Math.round(yy * image.getHeight).toInt
  ))

  def coordsAt(x: Double, y: Double): TriangleCoords = {
    val xx = (x / canvas.width()  * image.getWidth ).toInt
    val yy = (y / canvas.height() * image.getHeight).toInt

    if (xx >= 0 && xx < image.getWidth() && yy >= 0 && yy < image.getHeight())
      TriangleCoords.fromInt((image.getRGB(xx, yy) & 0xffffff) - 1)
    else null
  }

  def drawTriangle(coords: TriangleCoords): Unit = {
    val (xInt, yInt) = coordsToRealConverter.triangleCornerPoints(coords)

    val indexGraphics = image.getGraphics
    val indexColor = new java.awt.Color(coords.toInt + 1)
    indexGraphics.setColor(indexColor)
    indexGraphics.drawPolygon(xInt, yInt, 3)
    indexGraphics.fillPolygon(xInt, yInt, 3)
  }
}
