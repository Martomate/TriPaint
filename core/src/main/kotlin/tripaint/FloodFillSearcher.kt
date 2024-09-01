package tripaint

import tripaint.color.Color
import tripaint.coords.GlobalPixCoords

class FloodFillSearcher(private val colorLookup: ColorLookup) {
    fun search(
        startPos: GlobalPixCoords,
        predicate: (GlobalPixCoords, Color) -> Boolean
    ): List<GlobalPixCoords> {
        val visited: MutableSet<GlobalPixCoords> = mutableSetOf()
        val result: MutableList<GlobalPixCoords> = mutableListOf()
        val q = ArrayDeque(listOf(startPos))
        visited += startPos

        while (!q.isEmpty()) {
            val p = q.removeFirst()
            val color = colorLookup.lookup(p)
            if (color != null) {
                if (predicate(p, color)) {
                    result += p

                    val newOnes = p.neighbours().filter { !visited.contains(it) }
                    visited.addAll(newOnes)
                    q.addAll(newOnes)
                }
            }
        }
        return result
    }
}
