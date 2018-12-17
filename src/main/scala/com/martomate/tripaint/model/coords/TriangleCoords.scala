package com.martomate.tripaint.model.coords

case class TriangleCoords(x: Int, y: Int) {
  require(x >= 0, s"x >= 0, x = $x")
  require(x <= 2 * y, s"x <= 2 * y, x = $x, y = $y")

  def toInt: Int = x << 12 | y
}

object TriangleCoords {
  def fromInt(repr: Int): TriangleCoords = if (repr != -1) TriangleCoords(repr >>> 12, repr & 0xfff) else null
}
