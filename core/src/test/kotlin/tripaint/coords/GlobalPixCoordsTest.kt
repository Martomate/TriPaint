package tripaint.coords

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class GlobalPixCoordsTest {
    private fun make(x: Int, y: Int): GlobalPixCoords = GlobalPixCoords.from(x, y)

    private val neighborDistSq = 1.0 / 3 // dist = sqrt(3) / 2 * (2.0 / 3)
    private val eps = 0.0001

    @Nested
    inner class DistanceSq {
        @Test
        fun `is 0 for the same coordinate`() {
            assertEquals(0.0, make(4, 5) distanceSq make(4, 5), eps)
            assertEquals(0.0, GlobalPixCoords.from(11, -5) distanceSq make(11, -5), eps)
        }

        @Test
        fun `is a third for a direct neighbor`() {
            assertEquals(neighborDistSq, make(4, 5) distanceSq make(5, 5), eps)
            assertEquals(neighborDistSq, make(4, 5) distanceSq make(3, 5), eps)
            assertEquals(neighborDistSq, make(4, 5) distanceSq make(5, 4), eps)

            assertEquals(neighborDistSq, make(-3, 15) distanceSq make(-2, 15), eps)
            assertEquals(neighborDistSq, make(-3, 15) distanceSq make(-4, 15), eps)
            assertEquals(neighborDistSq, make(-3, 15) distanceSq make(-4, 16), eps)
        }

        @Test
        fun `is 1 for offset (+-2, 0)`() {
            assertEquals(1.0, make(4, 5) distanceSq make(6, 5), eps)
            assertEquals(1.0, make(4, 5) distanceSq make(2, 5), eps)

            assertEquals(1.0, make(-3, 15) distanceSq make(-1, 15), eps)
            assertEquals(1.0, make(-3, 15) distanceSq make(-5, 15), eps)
        }

        @Test
        fun `is symmetric`() {
            for (i in 1..10) {
                for (j in 1..10) {
                    val c1 = make(i * 13 % 29 - 17, j * 17 % 29 - 13)
                    val c2 = make(j * 17 % 29 - 13, i * 13 % 29 - 17)
                    assertEquals(c1 distanceSq c2, c2 distanceSq c1)
                }
            }
        }
    }

    @Nested
    inner class Cell {
        @Test
        fun `works for bottom triangles`() {
            assertEquals(GlobalPixCoords.from(0, 0), GlobalPixCoords.from(0, 1).cell())
            assertEquals(GlobalPixCoords.from(0, 0), GlobalPixCoords.from(0, 0).cell())
            assertEquals(GlobalPixCoords.from(0, 0), GlobalPixCoords.from(1, 0).cell())
            assertEquals(GlobalPixCoords.from(0, 0), GlobalPixCoords.from(2, 0).cell())

            assertEquals(GlobalPixCoords.from(6, 2), GlobalPixCoords.from(12, 5).cell())
            assertEquals(GlobalPixCoords.from(6, 2), GlobalPixCoords.from(12, 4).cell())
            assertEquals(GlobalPixCoords.from(6, 2), GlobalPixCoords.from(13, 4).cell())
            assertEquals(GlobalPixCoords.from(6, 2), GlobalPixCoords.from(14, 4).cell())

            assertEquals(GlobalPixCoords.from(-4, -2), GlobalPixCoords.from(-8, -3).cell())
            assertEquals(GlobalPixCoords.from(-4, -2), GlobalPixCoords.from(-8, -4).cell())
            assertEquals(GlobalPixCoords.from(-4, -2), GlobalPixCoords.from(-7, -4).cell())
            assertEquals(GlobalPixCoords.from(-4, -2), GlobalPixCoords.from(-6, -4).cell())
        }

        @Test
        fun `works for top triangles`() {
            assertEquals(GlobalPixCoords.from(1, 0), GlobalPixCoords.from(3, 0).cell())
            assertEquals(GlobalPixCoords.from(1, 0), GlobalPixCoords.from(1, 1).cell())
            assertEquals(GlobalPixCoords.from(1, 0), GlobalPixCoords.from(2, 1).cell())
            assertEquals(GlobalPixCoords.from(1, 0), GlobalPixCoords.from(3, 1).cell())

            assertEquals(GlobalPixCoords.from(7, 2), GlobalPixCoords.from(15, 4).cell())
            assertEquals(GlobalPixCoords.from(7, 2), GlobalPixCoords.from(13, 5).cell())
            assertEquals(GlobalPixCoords.from(7, 2), GlobalPixCoords.from(14, 5).cell())
            assertEquals(GlobalPixCoords.from(7, 2), GlobalPixCoords.from(15, 5).cell())

            assertEquals(GlobalPixCoords.from(-3, -2), GlobalPixCoords.from(-5, -4).cell())
            assertEquals(GlobalPixCoords.from(-3, -2), GlobalPixCoords.from(-7, -3).cell())
            assertEquals(GlobalPixCoords.from(-3, -2), GlobalPixCoords.from(-6, -3).cell())
            assertEquals(GlobalPixCoords.from(-3, -2), GlobalPixCoords.from(-5, -3).cell())
        }
    }
}