package tripaint.view.image

import tripaint.model.coords.TriangleCoords

class TriangleCoordsToReal(imageSize: Int, normToReal: (Double, Double) => (Double, Double)) {
  private val xs: Array[Double] = new Array(3)
  private val ys: Array[Double] = new Array(3)

  def triangleCornerPoints(coords: TriangleCoords): (Array[Double], Array[Double]) = {
    val yp = coords.y
    val xp = coords.x * 0.5 - (yp - imageSize + 1) * 0.5
    storeAllCoords(xp, yp, coords.x % 2 == 1)
    (xs, ys)
  }

  private def storeAllCoords(xp: Double, yp: Double, upsideDown: Boolean): Unit = {
    if upsideDown then {
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
    val (rx, ry) = normToReal(xPos / imageSize, yPos / imageSize)
    xs(index) = rx
    ys(index) = ry
  }
}
