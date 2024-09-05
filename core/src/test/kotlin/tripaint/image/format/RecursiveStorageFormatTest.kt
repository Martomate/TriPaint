package tripaint.image.format

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class RecursiveStorageFormatTest : StorageFormatTest() {
    @Nested
    inner class TransformToStorage {
        @Test
        fun `transformToStorage should transform correctly`() {
            // upper triangle
            triToStorage(Pair(0, 0), Pair(0, 0))
            triToStorage(Pair(0, 1), Pair(0, 1))
            triToStorage(Pair(1, 1), Pair(1, 1))
            triToStorage(Pair(2, 1), Pair(1, 0))

            // left triangle
            triToStorage(Pair(0, 2), Pair(0, 2))
            triToStorage(Pair(0, 3), Pair(0, 3))
            triToStorage(Pair(1, 3), Pair(1, 3))
            triToStorage(Pair(2, 3), Pair(1, 2))

            // right triangle
            triToStorage(Pair(4, 2), Pair(2, 0))
            triToStorage(Pair(4, 3), Pair(2, 1))
            triToStorage(Pair(5, 3), Pair(3, 1))
            triToStorage(Pair(6, 3), Pair(3, 0))

            // center triangle
            triToStorage(Pair(3, 3), Pair(3, 3))
            triToStorage(Pair(3, 2), Pair(3, 2))
            triToStorage(Pair(2, 2), Pair(2, 2))
            triToStorage(Pair(1, 2), Pair(2, 3))

            // next row
            triToStorage(Pair(0, 4), Pair(0, 4))
            triToStorage(Pair(1, 4), Pair(4, 7))
            triToStorage(Pair(2, 4), Pair(4, 6))
            triToStorage(Pair(3, 4), Pair(5, 6))
            triToStorage(Pair(4, 4), Pair(4, 4))
            triToStorage(Pair(5, 4), Pair(6, 5))
            triToStorage(Pair(6, 4), Pair(6, 4))
            triToStorage(Pair(7, 4), Pair(7, 4))
            triToStorage(Pair(8, 4), Pair(4, 0))
        }
    }

    private fun triToStorage(from: Pair<Int, Int>, to: Pair<Int, Int>) {
        assertEquals(make().transform(trCoords(from.first, from.second)), stCoords(to.first, to.second))
    }

    override fun make(): StorageFormat = RecursiveStorageFormat
}