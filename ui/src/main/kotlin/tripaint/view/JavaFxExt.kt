package tripaint.view

import tripaint.color.Color
import kotlin.math.max
import kotlin.math.min

import javafx.scene.paint.Color as FXColor

object JavaFxExt {
    fun Color.toFXColor(): FXColor = FXColor.color(clamp(this.r), clamp(this.g), clamp(this.b), clamp(this.a))

    fun fromFXColor(c: FXColor): Color = Color(c.red, c.green, c.blue, c.opacity)

    private fun clamp(v: Double): Double = min(max(v, 0.0), 1.0)
}
