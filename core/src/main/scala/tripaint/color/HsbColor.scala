package tripaint.color

case class HsbColor(h: Double, s: Double, b: Double, a: Double) {
  def toRgb: Color = {
    Color.fromHsb(this)
  }
}
