package tripaint

import scala.annotation.targetName

case class HsbColor(h: Double, s: Double, b: Double, a: Double) {
  def toRgb: Color = {
    Color.fromHsb(this)
  }
}

case class Color(r: Double, g: Double, b: Double, a: Double) {
  def withAlpha(a: Double): Color = Color(r, g, b, a)

  @targetName("add")
  def +(c2: Color): Color = Color(r + c2.r, g + c2.g, b + c2.b, a + c2.a)

  @targetName("sub")
  def -(c2: Color): Color = Color(r - c2.r, g - c2.g, b - c2.b, a - c2.a)

  @targetName("mul")
  def *(d: Double): Color = Color(r * d, g * d, b * d, a * d)

  @targetName("div")
  def /(d: Double): Color = Color(r / d, g / d, b / d, a / d)

  def toInt: Int = asInt(a) << 24 | asInt(r) << 16 | asInt(g) << 8 | asInt(b)

  private def clamp(v: Double): Double = math.min(math.max(v, 0), 1)

  private def asInt(v: Double): Int = (clamp(v) * 255).toInt

  def toHsb: HsbColor = {
    val red = this.r
    val green = this.g
    val blue = this.b

    val hi = red.max(green).max(blue)
    if hi == 0.0 then {
      return HsbColor(0.0, 0.0, 0.0, this.a)
    }

    val lo = red.min(green).min(blue)
    if lo == hi then {
      return HsbColor(0.0, 0.0, hi, this.a)
    }

    val brightness = hi
    val chroma = hi - lo

    val scaledHue =
      if red == brightness then {
        ((green - blue) / chroma + 6.0) % 6.0
      } else if green == brightness then {
        (blue - red) / chroma + 2.0
      } else {
        (red - green) / chroma + 4.0
      }

    val hue = scaledHue / 6.0 * 360.0
    val saturation = chroma / brightness

    HsbColor(hue, saturation, brightness, this.a)
  }
}

object Color {
  def fromInt(value: Int): Color = Color(
    (value >> 16 & 0xff) / 255.0,
    (value >> 8 & 0xff) / 255.0,
    (value >> 0 & 0xff) / 255.0,
    (value >> 24 & 0xff) / 255.0
  )

  def fromHsb(color: HsbColor): Color = {
    val HsbColor(h, s, b, a) = color

    if (s == 0.0) { // gray color
      return Color(b, b, b, a)
    }

    val scaledHue = (((h % 360.0 + 360.0) % 360.0) / 360.0) * 6.0
    val hueRegion = scaledHue.toInt
    val hueFraction = scaledHue - Math.floor(scaledHue)

    val chroma = b * s

    val u = chroma * hueFraction
    val v = chroma * (1.0 - hueFraction)
    val w = u + v

    val sub = hueRegion match {
      case 0 => Color(0, v, w, 0)
      case 1 => Color(u, 0, w, 0)
      case 2 => Color(w, 0, v, 0)
      case 3 => Color(w, u, 0, 0)
      case 4 => Color(v, w, 0, 0)
      case 5 => Color(0, w, u, 0)
    }

    Color(b, b, b, a) - sub
  }

  val Black: Color = Color(0, 0, 0, 1)
  val Red: Color = Color(1, 0, 0, 1)
  val Green: Color = Color(0, 1, 0, 1)
  val Blue: Color = Color(0, 0, 1, 1)
  val Yellow: Color = Color(1, 1, 0, 1)
  val Magenta: Color = Color(1, 0, 1, 1)
  val Cyan: Color = Color(0, 1, 1, 1)
  val White: Color = Color(1, 1, 1, 1)
}
