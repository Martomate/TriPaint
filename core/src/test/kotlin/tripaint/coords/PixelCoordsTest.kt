package tripaint.coords

import org.junit.jupiter.api.Assertions.assertEquals
import tripaint.ExtraAsserts
import kotlin.test.Test

class PixelCoordsTest {
    private fun tc(x: Int, y: Int): TriangleCoords = TriangleCoords.from(x, y)
    private fun ic(x: Int, y: Int): GridCoords = GridCoords.from(x, y)

    private fun testNeighbours(sz: Int, pc: PixelCoords, vararg result: PixelCoords): Unit =
        ExtraAsserts.assertSameElementsIgnoringOrder(result.toList(), pc.neighbours(sz))

    @Test
    fun `neighbours should work for upside up`() {
        val pc = PixelCoords.from(tc(2, 3), ic(0, 0))
        testNeighbours(8, pc,
            PixelCoords.from(tc(3, 4), ic(0, 0)),
            PixelCoords.from(tc(1, 3), ic(0, 0)),
            PixelCoords.from(tc(3, 3), ic(0, 0))
        )
    }

    @Test
    fun `neighbours should work for upside up, upside down pixel`() {
        val pc = PixelCoords.from(tc(3, 3), ic(0, 0))
        testNeighbours(8, pc,
            PixelCoords.from(tc(2, 2), ic(0, 0)),
            PixelCoords.from(tc(2, 3), ic(0, 0)),
            PixelCoords.from(tc(4, 3), ic(0, 0))
        )
    }

    @Test
    fun `neighbours should work for upside up, left edge`() {
        val sz = 8
        val pc = PixelCoords.from(tc(0, 3), ic(0, 0))
        testNeighbours(sz, pc,
            PixelCoords.from(tc(1, 4), ic(0, 0)),
            PixelCoords.from(tc(0, sz - 1 - 3), ic(-1, 0)),
            PixelCoords.from(tc(1, 3), ic(0, 0))
        )
    }

    @Test
    fun `neighbours should work for upside up, right edge`() {
        val sz = 8
        val yy = 3
        val xx = 2 * yy
        val pc = PixelCoords.from(tc(xx, yy), ic(0, 0))
        testNeighbours(sz, pc,
            PixelCoords.from(tc(xx + 1, yy + 1), ic(0, 0)),
            PixelCoords.from(tc(xx - 1, yy), ic(0, 0)),
            PixelCoords.from(tc(2 * (sz - 1 - yy), sz - 1 - yy), ic(1, 0))
        )
    }

    @Test
    fun `neighbours should work for upside up, down edge`() {
        val sz = 8
        val yy = sz - 1
        val xx = 2
        val pc = PixelCoords.from(tc(xx, yy), ic(0, 0))
        testNeighbours(sz, pc,
            PixelCoords.from(tc(2 * (sz - 1) - xx, yy), ic(1, -1)),
            PixelCoords.from(tc(xx - 1, yy), ic(0, 0)),
            PixelCoords.from(tc(xx + 1, yy), ic(0, 0))
        )
    }

    @Test
    fun `neighbours should work for upside down`() {
        val pc = PixelCoords.from(tc(2, 3), ic(1, 0))
        testNeighbours(8, pc,
            PixelCoords.from(tc(3, 4), ic(1, 0)),
            PixelCoords.from(tc(1, 3), ic(1, 0)),
            PixelCoords.from(tc(3, 3), ic(1, 0))
        )
    }

    @Test
    fun `neighbours should work for upside down, upside down pixel`() {
        val pc = PixelCoords.from(tc(3, 3), ic(1, 0))
        testNeighbours(8, pc,
            PixelCoords.from(tc(2, 2), ic(1, 0)),
            PixelCoords.from(tc(2, 3), ic(1, 0)),
            PixelCoords.from(tc(4, 3), ic(1, 0))
        )
    }

    @Test
    fun `neighbours should work for upside down, right edge`() {
        val sz = 8
        val pc = PixelCoords.from(tc(0, 3), ic(1, 0))
        testNeighbours(sz, pc,
            PixelCoords.from(tc(1, 4), ic(1, 0)),
            PixelCoords.from(tc(0, sz - 1 - 3), ic(2, 0)),
            PixelCoords.from(tc(1, 3), ic(1, 0))
        )
    }

    @Test
    fun `neighbours should work for upside down, left edge`() {
        val sz = 8
        val yy = 3
        val xx = 2 * yy
        val pc = PixelCoords.from(tc(xx, yy), ic(1, 0))
        testNeighbours(sz, pc,
            PixelCoords.from(tc(xx + 1, yy + 1), ic(1, 0)),
            PixelCoords.from(tc(xx - 1, yy), ic(1, 0)),
            PixelCoords.from(tc(2 * (sz - 1 - yy), sz - 1 - yy), ic(0, 0))
        )
    }

    @Test
    fun `neighbours should work for upside down, up edge`() {
        val sz = 8
        val yy = sz - 1
        val xx = 2
        val pc = PixelCoords.from(tc(xx, yy), ic(1, 0))
        testNeighbours(sz, pc,
            PixelCoords.from(tc(2 * (sz - 1) - xx, yy), ic(0, 1)),
            PixelCoords.from(tc(xx - 1, yy), ic(1, 0)),
            PixelCoords.from(tc(xx + 1, yy), ic(1, 0))
        )
    }

    fun makeG(sz: Int, i: Pair<Int, Int>, t: Pair<Int, Int>): GlobalPixCoords =
        makeP(i, t).toGlobal(sz)

    fun makeP(i: Pair<Int, Int>, t: Pair<Int, Int>): PixelCoords =
        PixelCoords.from(TriangleCoords.from(t.first, t.second), GridCoords.from(i.first, i.second))

    @Test
    fun `toGlobal should scale even triangles simply`() {
        val sz = 8
        assertEquals(GlobalPixCoords.from(0, sz - 1), makeG(sz, i = Pair(0, 0), t = Pair(0, 0)))
        assertEquals(GlobalPixCoords.from(0, 5 * sz + sz - 1), makeG(sz, i = Pair(0, 5), t = Pair(0, 0)))
        assertEquals(GlobalPixCoords.from(-2 * sz, 5 * sz + sz - 1), makeG(sz, i = Pair(-2, 5), t = Pair(0, 0)))
        assertEquals(GlobalPixCoords.from(42 * sz, -51 * sz + sz - 1), makeG(sz, i = Pair(42, -51), t = Pair(0, 0)))
    }

    @Test
    fun `toGlobal should handle odd triangles`() {
        val sz = 8
        assertEquals(GlobalPixCoords.from(sz + sz - 1, 0), makeG(sz, i = Pair(1, 0), t = Pair(0, 0)))
        assertEquals(GlobalPixCoords.from(-sz + sz - 1, 0), makeG(sz, i = Pair(-1, 0), t = Pair(0, 0)))
        assertEquals(GlobalPixCoords.from(3 * sz + sz - 1, sz), makeG(sz, i = Pair(3, 1), t = Pair(0, 0)))
        assertEquals(GlobalPixCoords.from(-5 * sz + sz - 1, -2 * sz), makeG(sz, i = Pair(-5, -2), t = Pair(0, 0)))
    }

    @Test
    fun `toGlobal should handle triangle offset for even triangles`() {
        val sz = 8
        assertEquals(GlobalPixCoords.from(3, sz - 1 - 5), makeG(sz, i = Pair(0, 0), t = Pair(3, 5)))
        assertEquals(GlobalPixCoords.from(12, sz - 1 - 7), makeG(sz, i = Pair(0, 0), t = Pair(12, 7)))
        assertEquals(GlobalPixCoords.from(-2 * sz + 1, 5 * sz + sz - 1 - 4), makeG(sz, i = Pair(-2, 5), t = Pair(1, 4)))
    }

    @Test
    fun `toGlobal should handle triangle offset for odd triangles`() {
        val sz = 8
        assertEquals(GlobalPixCoords.from(sz + sz - 1, 5), makeG(sz, i = Pair(1, 0), t = Pair(0, 5)))
        assertEquals(GlobalPixCoords.from(3 * sz + sz - 1 - 3, sz + 4), makeG(sz, i = Pair(3, 1), t = Pair(3, 4)))
        assertEquals(GlobalPixCoords.from(-5 * sz + sz - 1 - 4, -2 * sz + 3), makeG(sz, i = Pair(-5, -2), t = Pair(4, 3)))
    }

    @Test
    fun `apply(GlobalPixCoords) should handle the origin`() {
        val sz = 8
        testPixelCoordsApply(sz, makeP(Pair(0, 0), Pair(0, 0)))
    }

    @Test
    fun `apply(GlobalPixCoords) should handle the (0, 0) image`() {
        val sz = 8
        testPixelCoordsApply(sz, makeP(Pair(0, 0), Pair(0, 2)))
        testPixelCoordsApply(sz, makeP(Pair(0, 0), Pair(1, 2)))
        testPixelCoordsApply(sz, makeP(Pair(0, 0), Pair(4, 2)))
        testPixelCoordsApply(sz, makeP(Pair(0, 0), Pair(4, 7)))
        testPixelCoordsApply(sz, makeP(Pair(0, 0), Pair(14, 7)))
    }

    @Test
    fun `apply(GlobalPixCoords) should handle (0, 0) in an image`() {
        val sz = 8
        testPixelCoordsApply(sz, makeP(Pair(0, 1), Pair(0, 0)))
        testPixelCoordsApply(sz, makeP(Pair(1, 0), Pair(0, 0)))
        testPixelCoordsApply(sz, makeP(Pair(0, -1), Pair(0, 0)))
        testPixelCoordsApply(sz, makeP(Pair(-1, 0), Pair(0, 0)))
        testPixelCoordsApply(sz, makeP(Pair(1, 1), Pair(0, 0)))
        testPixelCoordsApply(sz, makeP(Pair(1, -1), Pair(0, 0)))
        testPixelCoordsApply(sz, makeP(Pair(-1, 1), Pair(0, 0)))
        testPixelCoordsApply(sz, makeP(Pair(-1, -1), Pair(0, 0)))
    }

    @Test
    fun `apply(GlobalPixCoords) should handle some other points`() {
        val sz = 8
        testPixelCoordsApply(sz, makeP(Pair(0, 0), Pair(1, 7)))
        testPixelCoordsApply(sz, makeP(Pair(2, 0), Pair(3, 2)))
        testPixelCoordsApply(sz, makeP(Pair(2, 5), Pair(4, 2)))
        testPixelCoordsApply(sz, makeP(Pair(1, 5), Pair(4, 2)))
        testPixelCoordsApply(sz, makeP(Pair(1, 5), Pair(4, 7)))
        testPixelCoordsApply(sz, makeP(Pair(-1, 5), Pair(4, 7)))
        testPixelCoordsApply(sz, makeP(Pair(-11, 5), Pair(4, 7)))
        testPixelCoordsApply(sz, makeP(Pair(11, 5), Pair(14, 7)))
    }

    private fun testPixelCoordsApply(sz: Int, src: PixelCoords) {
        assertEquals(src, PixelCoords.from(src.toGlobal(sz), sz))
    }
}