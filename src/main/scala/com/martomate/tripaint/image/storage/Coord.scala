package com.martomate.tripaint.image.storage

class Coord private(val x: Int, val y: Int, val index: Int) {
  def distanceSq(p2: Coord): Double = {
    val dx = p2.x - x
    val dy = p2.y - y
    val xx = dx * 0.5 - dy * 0.5
    // It's like magic!
    val yy = dy * Coord.sqrt3 / 2
    xx * xx + yy * yy
  }

  def distance(p2: Coord): Double = math.sqrt(distanceSq(p2))

  override def equals(c2: Any): Boolean = c2 match {
    case coord: Coord => index == coord.index
    case _ => false
  }

  override def hashCode: Int = index.hashCode
}

object Coord {
  private val sqrt3 = math.sqrt(3)

  def fromXY(x: Int, y: Int, imageSize: Int) = new Coord(x, y, (if (x < y) x else y) + (y - (if (x > y) x - y else 0)) * imageSize)

  def fromIndex(index: Int, imageSize: Int): Coord = {
    val xx = index % imageSize
    val yy = index / imageSize
    if (yy < xx) new Coord(xx + xx - yy, xx, index)
    else new Coord(xx, yy, index)
  }

  def unapply(c: Coord) = Some(c.x, c.y, c.index)
}
