package tripaint.coords

import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.math.sqrt
import kotlin.test.Test

class GridCoordsTest {
    private val epsilon: Double = 1e-6
    private val unitHeight: Double = sqrt(3.0) / 2
    private val aThirdHeight: Double = unitHeight / 3

    @Test
    fun `(xOff, yOff) should be the mass center of the triangle`() {
        assertEquals(0.5, GridCoords.from(0, 0).center.first, epsilon)
        assertEquals(3 * 0.5 + 0.5, GridCoords.from(0, 3).center.first, epsilon)
        assertEquals(-3 * 0.5 + 0.5, GridCoords.from(0, -3).center.first, epsilon)

        assertEquals(1.0, GridCoords.from(1, 0).center.first, epsilon)
        assertEquals(3 * 0.5 + 4, GridCoords.from(7, 3).center.first, epsilon)
        assertEquals(-3 * 0.5 + 4, GridCoords.from(7, -3).center.first, epsilon)
        assertEquals(3 * 0.5 + -3, GridCoords.from(-7, 3).center.first, epsilon)
        assertEquals(-3 * 0.5 + -3, GridCoords.from(-7, -3).center.first, epsilon)

        assertEquals(aThirdHeight, -GridCoords.from(0, 0).center.second, epsilon)
        assertEquals(3 * unitHeight + aThirdHeight, -GridCoords.from(0, 3).center.second, epsilon)
        assertEquals(-3 * unitHeight + aThirdHeight, -GridCoords.from(0, -3).center.second, epsilon)

        assertEquals(2 * aThirdHeight, -GridCoords.from(1, 0).center.second, epsilon)
        assertEquals(3 * unitHeight + 2 * aThirdHeight, -GridCoords.from(7, 3).center.second, epsilon)
        assertEquals(-3 * unitHeight + 2 * aThirdHeight, -GridCoords.from(7, -3).center.second, epsilon)
        assertEquals(3 * unitHeight + 2 * aThirdHeight, -GridCoords.from(-7, 3).center.second, epsilon)
        assertEquals(-3 * unitHeight + 2 * aThirdHeight, -GridCoords.from(-7, -3).center.second, epsilon)
    }
}