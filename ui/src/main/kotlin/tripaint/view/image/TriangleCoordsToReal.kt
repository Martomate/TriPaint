package tripaint.view.image

import tripaint.coords.TriangleCoords

class TriangleCoordsToReal(private val imageSize: Int, private val normToReal: (Double, Double) -> Pair<Double, Double>) {
    private val xs: DoubleArray = DoubleArray(3)
    private val ys: DoubleArray = DoubleArray(3)

    fun triangleCornerPoints(coords: TriangleCoords): Pair<DoubleArray, DoubleArray> {
        val yp = coords.y.toDouble()
        val xp = coords.x * 0.5 - (yp - imageSize + 1) * 0.5
        storeAllCoords(xp, yp, coords.x % 2 == 1)
        return Pair(xs, ys)
    }

    private fun storeAllCoords(xp: Double, yp: Double, upsideDown: Boolean) {
        if (upsideDown) {
            storeCoords(0, xp, yp)
            storeCoords(1, xp + 1.0, yp)
            storeCoords(2, xp + 0.5, yp + 1.0)
        } else {
            storeCoords(0, xp, yp + 1.0)
            storeCoords(1, xp + 1.0, yp + 1.0)
            storeCoords(2, xp + 0.5, yp)
        }
    }

    private fun storeCoords(index: Int, xPos: Double, yPos: Double) {
        val (rx, ry) = normToReal(xPos / imageSize, yPos / imageSize)
        xs[index] = rx
        ys[index] = ry
    }
}
