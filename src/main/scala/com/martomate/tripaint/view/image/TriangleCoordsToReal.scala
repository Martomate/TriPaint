package com.martomate.tripaint.view.image

import com.martomate.tripaint.model.coords.TriangleCoords

class TriangleCoordsToReal[T](
    imageSize: Int,
    arrayFactory: Int => Array[T],
    normToReal: (Double, Double) => (T, T)
) {
  private object points {
    val x: Array[T] = arrayFactory(3)
    val y: Array[T] = arrayFactory(3)
  }

  def triangleCornerPoints(coords: TriangleCoords): (Array[T], Array[T]) = {
    val yp = coords.y
    val xp = coords.x * 0.5 - (yp - imageSize + 1) * 0.5

    storeAllCoords(xp, yp, coords.x % 2 == 1)

    (points.x, points.y)
  }

  private def storeAllCoords(xp: Double, yp: Double, upsideDown: Boolean): Unit = {
    if (upsideDown) {
      storeCoords(0, xp, yp)
      storeCoords(1, xp + 1.0, yp)
      storeCoords(2, xp + 0.5, yp + 1.0)
    } else {
      storeCoords(0, xp, yp + 1.0)
      storeCoords(1, xp + 1.0, yp + 1.0)
      storeCoords(2, xp + 0.5, yp)
    }
  }

  private def storeCoords(index: Int, xPos: Double, yPos: Double): Unit = {
    val xx = xPos / imageSize
    val yy = yPos / imageSize

    storeNormalizedCoords(index, xx, yy)
  }

  private def storeNormalizedCoords(index: Int, xx: Double, yy: Double): Unit = {
    val real = normToReal(xx, yy)
    points.x(index) = real._1
    points.y(index) = real._2
  }
}
