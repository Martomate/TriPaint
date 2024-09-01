package tripaint.color

data class HsbColor(val h: Double, val s: Double, val b: Double, val a: Double) {
    fun toRgb(): Color {
        return Color.fromHsb(this)
    }
}