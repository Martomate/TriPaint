package tripaint.coords

case class TriangleCoords(value: Int) extends AnyVal {
  inline def x: Int = value >> 12
  inline def y: Int = value & 0xfff

  def toInt: Int = x << 12 | y
}

object TriangleCoords {
  inline def apply(x: Int, y: Int): TriangleCoords = {
    new TriangleCoords(x << 12 | y)
  }

  def fromInt(repr: Int): TriangleCoords = {
    if repr == -1 then {
      throw new IllegalArgumentException()
    }
    TriangleCoords(repr >>> 12, repr & 0xfff)
  }
}
