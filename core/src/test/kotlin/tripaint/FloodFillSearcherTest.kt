package tripaint

import org.junit.jupiter.api.Assertions.assertEquals
import tripaint.color.Color
import tripaint.coords.GlobalPixCoords
import kotlin.test.Test

class FloodFillSearcherTest {
    private fun makeWhite() = FloodFillSearcher { _ -> Color.White }

    @Test
    fun testFalsePredicateGivesNothing() {
        val startPos = GlobalPixCoords.from(4, 7)
        val s = makeWhite()
        assertEquals(listOf<GlobalPixCoords>(), s.search(startPos) { _, _ -> false })
    }

    @Test
    fun testFalsePredicateForNeighborsGivesOnlyTheStartPos() {
        val startPos = GlobalPixCoords.from(4, 7)
        val s = makeWhite()
        assertEquals(
            listOf(startPos),
            s.search(startPos) { c, _ -> c == startPos || c.distanceSq(startPos) > 0.34 },
        )
    }
}
