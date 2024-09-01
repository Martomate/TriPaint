package tripaint.coords

import kotlin.math.floor
import kotlin.math.sqrt

data class GridCoords(val value: Int) {
    val x: Int
        get() = value shr 16
    val y: Int
        get() = (value shl 16) shr 16

    val center: Pair<Double, Double>
        get() {
            val xDiv2 = floor(x.toDouble() / 2)
            val pts = if (x % 2 == 0) listOf(
                            Pair(xDiv2, y),
                Pair(xDiv2 + 1, y),
                Pair(xDiv2, y+1)
            )
            else listOf(
                Pair(xDiv2 + 1, y + 1),
                Pair(xDiv2, y+1),
                Pair(xDiv2 + 1, y),
            )
            val vertices: List<Pair<Double, Double>> = pts.map {
                val (xx, yy) = it
                Pair(xx+yy * 0.5, -yy * sqrt(3.0) / 2)
            }

            val centerX: Double = vertices.sumOf { it.first } / 3
            val centerY: Double = vertices.sumOf { it.second } / 3

            return Pair(centerX, centerY)
        }

    companion object {
        fun from(x: Int, y: Int): GridCoords =
            GridCoords((x shl 16) or (y and 0xffff))
    }
}

