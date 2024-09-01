package tripaint.coords

import kotlin.math.sqrt

/** Like TriImageCoords but for pixels (on the entire area) */
data class GlobalPixCoords(val value: Long) {
    val x: Int
        get() = (value shr 32).toInt()
    val y: Int
        get() = (value and 0xffffffff).toInt()

    infix fun distanceSq(other: GlobalPixCoords): Double {
        val dx = other.x - x
        val dy = other.y - y
        val otherY = other.y + (if (other.x % 2 == 0) 1.0 / 3 else 2.0 / 3)
        val thisY = y + (if (x % 2 == 0) 1.0 / 3 else 2.0 / 3)
        val actualDy = otherY - thisY
        val xx = dx * 0.5 + dy * 0.5
        // It's like magic!
        val yy = actualDy * sqrt3 / 2
        return xx * xx + yy * yy
    }

    fun neighbours(): List<GlobalPixCoords> {
        return listOf(
            Pair(x - 1, y),
            if (x % 2 == 0) Pair(x + 1, y - 1) else Pair(x - 1, y + 1),
            Pair(x + 1, y)
        ).map {
            val (xx, yy) = it
            from(xx, yy)
        }
    }

    fun cell(): GlobalPixCoords {
        val cy = y shr 1
        val cx = x shr 2
        val cz = (x + 1 + (y shl 1)) shr 2

        return if (cx % 2 == cz % 2) {
            from(cx shl 1, cy)
        } else {
            from((cx shl 1) + 1, cy)
        }
    }

    companion object {
        private val sqrt3: Double = sqrt(3.0)

        fun from(x: Int, y: Int): GlobalPixCoords = GlobalPixCoords((x.toLong() shl 32) or (y.toLong() and 0xffffffffL))
    }

}