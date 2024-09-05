package tripaint.image.format

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import kotlin.test.Test

class SimpleStorageFormatTest : StorageFormatTest() {
    @Nested
    inner class TransformToStorage {
        @Test
        fun `transforms correctly`() {
            triToStorage(Pair(0, 0), Pair(0, 0))
            triToStorage(Pair(0, 10), Pair(0, 10))
            triToStorage(Pair(10, 10), Pair(10, 10))
            triToStorage(Pair(20, 10), Pair(10, 0))
        }
    }

    @Nested
    inner class TransformFromStorage {
        @Test
        fun `transforms correctly`() {
            storageToTri(Pair(0, 0), Pair(0, 0))
            storageToTri(Pair(0, 10), Pair(0, 10))
            storageToTri(Pair(10, 10), Pair(10, 10))
            storageToTri(Pair(10, 0), Pair(20, 10))
        }
    }

    private fun triToStorage(from: Pair<Int, Int>, to: Pair<Int, Int>) {
        assertEquals(make().transform(trCoords(from.first, from.second)), stCoords(to.first, to.second))
    }

    private fun storageToTri(from: Pair<Int, Int>, to: Pair<Int, Int>) {
        assertEquals(make().reverse(stCoords(from.first, from.second)), trCoords(to.first, to.second))
    }

    override fun make(): StorageFormat = SimpleStorageFormat
}