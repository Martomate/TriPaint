package tripaint.coords

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class TriangleCoordsTest {
    @Test
    fun `constructor should require x is at least 0`() {
        testApplyFail(-1, 0)
        testApplyFail(-1, -1)
    }

    @Test
    fun `constructor should require x is at most 2 times y`() {
        testApplyFail(0, -1)
        testApplyFail(1, 0)
        testApplyFail(3, 1)
        testApplyFail(-1, 4)
    }

    @Test
    fun `constructor should require y is less than 0x1000`() {
        testApplyFail(0, 0x1000)
        testApplyFail(0x1000, 0x1000)
        testApplyFail(0x1000, 0x10000)
        TriangleCoords.from(0x1000, 0xfff)
        TriangleCoords.from(0x1ffe, 0xfff)
    }

    @Test
    fun `toInt should return xxxxyyy`() {
        testToIntSuccess(0, 0, 0x000000)
        testToIntSuccess(0, 1, 0x000001)
        testToIntSuccess(1, 1, 0x001001)
        testToIntSuccess(0x011, 0x110, 0x011110)
        testToIntSuccess(0xfff, 0xfff, 0xffffff)
        testToIntSuccess(0xfff * 2, 0xfff, 0x1ffefff)
    }

    @Test
    fun `fromInt should throw an exception for -1`() {
        assertThrows<AssertionError> { TriangleCoords.fromInt(-1) }
    }

    @Test
    fun `fromInt should throw an exception for invalid input`() {
        testFromIntFail(0x001000)
        testFromIntFail(0x110011)
        testFromIntFail(0x1ffffff)
    }

    @Test
    fun `fromInt should be the inverse of 'toInt'`() {
        testFromIntSuccess(0x000000, 0, 0)
        testFromIntSuccess(0x000001, 0, 1)
        testFromIntSuccess(0x001001, 1, 1)
        testFromIntSuccess(0x111111, 0x111, 0x111)
        testFromIntSuccess(0x1ffefff, 0xfff * 2, 0xfff)
        testFromIntSuccess(0x011110, 0x011, 0x110)
    }

    private fun testApplyFail(x: Int, y: Int) =
        assertThrows<AssertionError> { TriangleCoords.from(x, y) }

    private fun testToIntSuccess(x: Int, y: Int, repr: Int) =
        assertEquals(TriangleCoords.from(x, y).toInt(), repr)

    private fun testFromIntFail(repr: Int) =
        assertThrows<AssertionError> { TriangleCoords.fromInt(repr) }

    private fun testFromIntSuccess(repr: Int, x: Int, y: Int) =
        assertEquals(TriangleCoords.fromInt(repr), TriangleCoords.from(x, y))
}