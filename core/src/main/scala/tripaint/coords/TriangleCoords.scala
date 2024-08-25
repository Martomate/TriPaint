package tripaint.coords

case class TriangleCoords(value: Int) extends AnyVal {
  inline def x: Int = value >> 12
  inline def y: Int = value & 0xfff

  def toInt: Int = x << 12 | y
}

object TriangleCoords {
  inline def apply(x: Int, y: Int): TriangleCoords = {
    assert(x >= 0)
    assert(x <= 2 * y)
    assert(y < 0x1000)
    new TriangleCoords(x << 12 | y)
  }

  def fromInt(repr: Int): TriangleCoords = {
    assert(repr != -1)
    TriangleCoords(repr >>> 12, repr & 0xfff)
  }
}
