package tripaint.color

data class Color(val r: Double, val g: Double, val b: Double, val a: Double) {
    fun withAlpha(a: Double): Color = Color(r, g, b, a)

    operator fun plus(c2: Color): Color = Color(r + c2.r, g + c2.g, b + c2.b, a + c2.a)

    operator fun minus(c2: Color): Color = Color(r - c2.r, g - c2.g, b - c2.b, a - c2.a)

    operator fun times(d: Double): Color = Color(r * d, g * d, b * d, a * d)

    operator fun div(d: Double): Color = Color(r / d, g / d, b / d, a / d)

    fun toInt(): Int = (asInt(a) shl 24) or (asInt(r) shl 16) or (asInt(g) shl 8) or asInt(b)

    private fun clamp(v: Double): Double = Math.min(Math.max(v, 0.0), 1.0)

    private fun asInt(v: Double): Int = (clamp(v) * 255).toInt()

    fun toHsb(): HsbColor {
        val red = this.r
        val green = this.g
        val blue = this.b

        val hi = Math.max(Math.max(red, green), blue)
        if (hi == 0.0) {
            return HsbColor(0.0, 0.0, 0.0, this.a)
        }

        val lo = Math.min(Math.min(red, green), blue)
        if (lo == hi) {
            return HsbColor(0.0, 0.0, hi, this.a)
        }

        val brightness = hi
        val chroma = hi - lo

        val scaledHue =
            if (red == brightness) {
                ((green - blue) / chroma + 6.0) % 6.0
            } else if (green == brightness) {
                (blue - red) / chroma + 2.0
            } else {
                (red - green) / chroma + 4.0
            }

        val hue = scaledHue / 6.0 * 360.0
        val saturation = chroma / brightness

        return HsbColor(hue, saturation, brightness, this.a)
    }

    companion object {
        fun fromInt(value: Int): Color = Color(
            ((value shr 16) and 0xff) / 255.0,
            ((value shr 8) and 0xff) / 255.0,
            ((value shr 0) and 0xff) / 255.0,
            ((value shr 24) and 0xff) / 255.0
        )

        fun fromHsb(color: HsbColor): Color {
            val (h, s, b, a) = color

            if (s == 0.0) { // gray color
                return Color(b, b, b, a)
            }

            val scaledHue = (((h % 360.0 + 360.0) % 360.0) / 360.0) * 6.0
            val hueRegion = scaledHue.toInt()
            val hueFraction = scaledHue - Math.floor(scaledHue)

            val chroma = b * s

            val u = chroma * hueFraction
            val v = chroma * (1.0 - hueFraction)
            val w = u + v

            val sub = when (hueRegion) {
                0 -> Color(0.0, v, w, 0.0)
                1 -> Color(u, 0.0, w, 0.0)
                2 -> Color(w, 0.0, v, 0.0)
                3 -> Color(w, u, 0.0, 0.0)
                4 -> Color(v, w, 0.0, 0.0)
                5 -> Color(0.0, w, u, 0.0)
                else -> throw RuntimeException()
            }

            return Color(b, b, b, a) - sub
        }

        val Black: Color = Color(0.0, 0.0, 0.0, 1.0)
        val Red: Color = Color(1.0, 0.0, 0.0, 1.0)
        val Green: Color = Color(0.0, 1.0, 0.0, 1.0)
        val Blue: Color = Color(0.0, 0.0, 1.0, 1.0)
        val Yellow: Color = Color(1.0, 1.0, 0.0, 1.0)
        val Magenta: Color = Color(1.0, 0.0, 1.0, 1.0)
        val Cyan: Color = Color(0.0, 1.0, 1.0, 1.0)
        val White: Color = Color(1.0, 1.0, 1.0, 1.0)
    }
}