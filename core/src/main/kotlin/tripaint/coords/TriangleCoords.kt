package tripaint.coords

data class TriangleCoords(val value: Int) {
    val x: Int
        get() = value shr 12
    val y: Int
        get() = value and 0xfff

    fun toInt(): Int = (x shl 12) or y

    companion object {
        fun from(x: Int, y: Int): TriangleCoords {
            assert(x >= 0)
            assert(x <= 2 * y)
            assert(y < 0x1000)
            return TriangleCoords((x shl 12) or y)
        }

        fun fromInt(repr: Int): TriangleCoords {
            assert(repr != -1)
            return from(repr ushr 12, repr and 0xfff)
        }
    }

}