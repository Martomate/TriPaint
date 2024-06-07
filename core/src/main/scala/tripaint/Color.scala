package tripaint

case class Color(r: Double, g: Double, b: Double, a: Double) {
  def withAlpha(a: Double): Color = Color(r, g, b, a)

  def +(c2: Color): Color = Color(r + c2.r, g + c2.g, b + c2.b, a + c2.a)

  def -(c2: Color): Color = Color(r - c2.r, g - c2.g, b - c2.b, a - c2.a)

  def *(d: Double): Color = Color(r * d, g * d, b * d, a * d)

  def /(d: Double): Color = Color(r / d, g / d, b / d, a / d)

  def toInt: Int = asInt(a) << 24 | asInt(r) << 16 | asInt(g) << 8 | asInt(b)

  private def clamp(v: Double): Double = math.min(math.max(v, 0), 1)

  private def asInt(v: Double): Int = (clamp(v) * 255).toInt
}

object Color {
  def fromInt(value: Int): Color = Color(
    (value >> 16 & 0xff) / 255.0,
    (value >> 8 & 0xff) / 255.0,
    (value >> 0 & 0xff) / 255.0,
    (value >> 24 & 0xff) / 255.0
  )

  val Black: Color = Color(0, 0, 0, 1)
  val Red: Color = Color(1, 0, 0, 1)
  val Green: Color = Color(0, 1, 0, 1)
  val Blue: Color = Color(0, 0, 1, 1)
  val Yellow: Color = Color(1, 1, 0, 1)
  val Magenta: Color = Color(1, 0, 1, 1)
  val Cyan: Color = Color(0, 1, 1, 1)
  val White: Color = Color(1, 1, 1, 1)
}
