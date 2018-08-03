package com.martomate.tripaint.image.coords

case class TriangleCoords(x: Int, y: Int) {
  def distanceSq(c2: TriangleCoords): Double = {
    ???
  }

  def toInt: Int = x << 12 | y
}

object TriangleCoords {
  def fromInt(repr: Int): TriangleCoords = if (repr != -1) TriangleCoords(repr >>> 12, repr & 0xfff) else null
}
