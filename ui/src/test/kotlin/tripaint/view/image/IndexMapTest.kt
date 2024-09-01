package tripaint.view.image

import org.junit.jupiter.api.Assertions.assertEquals
import tripaint.coords.TriangleCoords
import kotlin.test.Test

class IndexMapTest {

    private fun make(imageSize: Int): IndexMap = IndexMap(imageSize)

    @Test
    fun `coordsAt should return null outside of the image`() {
        val indexMap = make(12)

        assertEquals(null, indexMap.coordsAt(10.0, 0.0))
        assertEquals(null, indexMap.coordsAt(-10.0, 0.0))
        assertEquals(null, indexMap.coordsAt(0.0, 10.0))
        assertEquals(null, indexMap.coordsAt(0.0, -10.0))

        assertEquals(null, indexMap.coordsAt(0.49, 0.0))
        assertEquals(null, indexMap.coordsAt(0.51, 0.0))

        assertEquals(null, indexMap.coordsAt(0.0, 1 / 80.0))
        assertEquals(null, indexMap.coordsAt(1.0, 1 / 80.0))
    }

    @Test
    fun `coordsAt should be correct for size 2`() {
        val indexMap = make(2)

        assertEquals(TriangleCoords.from(1, 1), indexMap.coordsAt(0.50, 79 / 80.0))
        assertEquals(TriangleCoords.from(0, 1), indexMap.coordsAt(0.49, 79 / 80.0))
        assertEquals(TriangleCoords.from(2, 1), indexMap.coordsAt(0.51, 79 / 80.0))

        assertEquals(null, indexMap.coordsAt(0.2475, 39 / 80.0))
        assertEquals(TriangleCoords.from(0, 0), indexMap.coordsAt(0.26, 39 / 80.0))
        assertEquals(TriangleCoords.from(0, 0), indexMap.coordsAt(0.50, 39 / 80.0))
        assertEquals(TriangleCoords.from(0, 0), indexMap.coordsAt(0.74, 39 / 80.0))
        assertEquals(null, indexMap.coordsAt(0.7525, 39 / 80.0))

        assertEquals(TriangleCoords.from(0, 1), indexMap.coordsAt(0.2475, 41 / 80.0))
        assertEquals(TriangleCoords.from(1, 1), indexMap.coordsAt(0.26, 41 / 80.0))
        assertEquals(TriangleCoords.from(1, 1), indexMap.coordsAt(0.50, 41 / 80.0))
        assertEquals(TriangleCoords.from(1, 1), indexMap.coordsAt(0.74, 41 / 80.0))
        assertEquals(TriangleCoords.from(2, 1), indexMap.coordsAt(0.7525, 41 / 80.0))
    }

    @Test
    fun `coordsAt should return correct y levels`() {
        val indexMap = make(20)

        for (y in 0 until 20) {
            val steps = 23
            val stepSize = 0.05 / steps
            for (d in 1 until steps) {
            val yReal = y * 0.05 + d * stepSize
            assertEquals(Triple(y, d, TriangleCoords.from(y, y)), Triple(y, d, indexMap.coordsAt(0.500, yReal)))
            if (y > 10) {
                assertEquals(
                    Triple(y, d, TriangleCoords.from(y - 10, y)),
                    Triple(y, d, indexMap.coordsAt(0.250, yReal)),
                )
                assertEquals(
                    Triple(y, d, TriangleCoords.from(y + 10, y)),
                    Triple(y, d, indexMap.coordsAt(0.750, yReal)),
                )
            }
        }
        }
    }

    @Test
    fun `coordsAt should return correct x values`() {
        val indexMap = make(20)

        for (x in 0 until 20) {
            val steps = 23
            val stepSize = 0.05 / steps
            for (d in 1 until steps) {
                val xReal = x * 0.05 + d * stepSize
                assertEquals(
                    Triple(x, d, TriangleCoords.from(2 * x, 19)),
                    Triple(x, d, indexMap.coordsAt(xReal, 0.999999)),
                )
            }
        }
    }
}