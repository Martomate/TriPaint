package tripaint.view

import javafx.scene.paint.Color as FXColor
import tripaint.color.Color

object JavaFxExt {
  extension (c: Color) {
    def toFXColor: FXColor = FXColor.color(clamp(c.r), clamp(c.g), clamp(c.b), clamp(c.a))
  }

  def fromFXColor(c: FXColor): Color = Color(c.getRed, c.getGreen, c.getBlue, c.getOpacity)

  given Conversion[FXColor, Color] with {
    override def apply(c: FXColor): Color = fromFXColor(c)
  }

  private def clamp(v: Double): Double = math.min(math.max(v, 0), 1)
}
