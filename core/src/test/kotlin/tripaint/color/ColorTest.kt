package tripaint.color

import kotlin.test.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

class ColorTest {
    fun compareRgb(actual: Color, expected: Color) {
        assertEquals(expected.r, actual.r, 0.001)
        assertEquals(expected.g, actual.g, 0.001)
        assertEquals(expected.b, actual.b, 0.001)
        assertEquals(1.0, actual.a)
    }

    fun compareHsb(actual: HsbColor, expected: HsbColor) {
        assertEquals(expected.s, actual.s, 0.001)
        assertEquals(expected.b, actual.b, 0.001)
        assertEquals(expected.h, actual.h, 0.1)
        assertEquals(1.0, actual.a)
    }

    fun hsbEqual(obtained: HsbColor, expected: HsbColor) =
        runCatching { compareHsb(obtained, expected) }.isSuccess

    fun rgbEqual(obtained: Color, expected: Color) =
        runCatching { compareRgb(obtained, expected) }.isSuccess

    val cases = listOf(
        Pair(Color(1.000, 1.000, 1.000, 1.0), HsbColor(000.0, 0.000, 1.000, 1.0)),
        Pair(Color(0.500, 0.500, 0.500, 1.0), HsbColor(000.0, 0.000, 0.500, 1.0)),
        Pair(Color(0.000, 0.000, 0.000, 1.0), HsbColor(000.0, 0.000, 0.000, 1.0)),
        Pair(Color(1.000, 0.000, 0.000, 1.0), HsbColor(000.0, 1.000, 1.000, 1.0)),
        Pair(Color(0.750, 0.750, 0.000, 1.0), HsbColor(060.0, 1.000, 0.750, 1.0)),
        Pair(Color(0.000, 0.500, 0.000, 1.0), HsbColor(120.0, 1.000, 0.500, 1.0)),
        Pair(Color(0.500, 1.000, 1.000, 1.0), HsbColor(180.0, 0.500, 1.000, 1.0)),
        Pair(Color(0.500, 0.500, 1.000, 1.0), HsbColor(240.0, 0.500, 1.000, 1.0)),
        Pair(Color(0.750, 0.250, 0.750, 1.0), HsbColor(300.0, 0.667, 0.750, 1.0)),
        Pair(Color(0.628, 0.643, 0.142, 1.0), HsbColor(061.8, 0.779, 0.643, 1.0)),
        Pair(Color(0.255, 0.104, 0.918, 1.0), HsbColor(251.1, 0.887, 0.918, 1.0)),
        Pair(Color(0.116, 0.675, 0.255, 1.0), HsbColor(134.9, 0.828, 0.675, 1.0)),
        Pair(Color(0.941, 0.785, 0.053, 1.0), HsbColor(049.5, 0.944, 0.941, 1.0)),
        Pair(Color(0.704, 0.187, 0.897, 1.0), HsbColor(283.7, 0.792, 0.897, 1.0)),
        Pair(Color(0.931, 0.463, 0.316, 1.0), HsbColor(014.3, 0.661, 0.931, 1.0)),
        Pair(Color(0.998, 0.974, 0.532, 1.0), HsbColor(056.9, 0.467, 0.998, 1.0)),
        Pair(Color(0.099, 0.795, 0.591, 1.0), HsbColor(162.4, 0.875, 0.795, 1.0)),
        Pair(Color(0.211, 0.149, 0.597, 1.0), HsbColor(248.3, 0.750, 0.597, 1.0)),
        Pair(Color(0.495, 0.493, 0.721, 1.0), HsbColor(240.5, 0.316, 0.721, 1.0))
    )

    @Test
    fun testRgbToHsb() {
        for ((rgb, hsb) in cases) {
            assertTrue(hsbEqual(hsb, rgb.toHsb()))
        }
    }

    @Test
    fun testHsbToRgb() {
        for ((rgb, hsb) in cases) {
            assertTrue(rgbEqual(rgb, hsb.toRgb()))
        }
    }
}
