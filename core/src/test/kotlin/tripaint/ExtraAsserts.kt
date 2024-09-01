package tripaint

import org.junit.jupiter.api.Assertions.assertEquals

object ExtraAsserts {
    private fun <T> List<T>.occurrences(): Map<T, Int> {
        val m = mutableMapOf<T, Int>()
        for (elem in this) {
            val k = elem
            val b = m[k]
            val v = if (b != null) b + 1 else 1
            m[k] = v
        }
        return m.toMap()
    }

    fun <T> assertSameElementsIgnoringOrder(left: List<T>, right: List<T>) = assertEquals(left.occurrences(), right.occurrences())
}