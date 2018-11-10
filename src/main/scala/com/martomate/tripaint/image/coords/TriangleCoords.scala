package com.martomate.tripaint.image.coords

case class TriangleCoords(x: Int, y: Int) {
  def distanceSq(c2: TriangleCoords): Double = {
    val dx = c2.x - x
    val dy = c2.y - y
    val xx = dx * 0.5 - dy * 0.5
    // It's like magic!
    val yy = dy * TriangleCoords.sqrt3 / 2
    xx * xx + yy * yy
  }

  def toInt: Int = x << 12 | y
}

object TriangleCoords {
  private val sqrt3: Double = math.sqrt(3)

  def fromInt(repr: Int): TriangleCoords = if (repr != -1) TriangleCoords(repr >>> 12, repr & 0xfff) else null
}
