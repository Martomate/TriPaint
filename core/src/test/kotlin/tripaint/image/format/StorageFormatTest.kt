package tripaint.image.format

import org.junit.jupiter.api.Assertions.assertEquals
import tripaint.coords.StorageCoords
import tripaint.coords.TriangleCoords
import kotlin.test.Test

abstract class StorageFormatTest {
    abstract fun make(): StorageFormat

    @Test
    fun `transformToStorage should be the inverse of transformFromStorage`() {
        val f = make()

        fun comp(x: Int, y: Int) = assertEquals(f.transform(f.reverse(stCoords(x, y))), stCoords(x, y))

        comp(0, 0)
        comp(10, 0)
        comp(0, 10)
        comp(10, 10)
        comp(20, 10)

        for (y in 0..100) {
            for (x in 0..100) {
                comp(x, y)
            }
        }
    }

    @Test
    fun `transformFromStorage should be the inverse of transformToStorage`() {
        val f = make()

        fun comp(x: Int, y: Int) = assertEquals(f.reverse(f.transform(trCoords(x, y))), trCoords(x, y))

        comp(0, 0)
        comp(0, 10)
        comp(10, 10)
        comp(20, 10)

        for (y in 0..100) {
            for (x in 0..2 * y) {
                comp(x, y)
            }
        }
    }

    fun stCoords(x: Int, y: Int): StorageCoords = StorageCoords.from(x, y)
    fun trCoords(x: Int, y: Int): TriangleCoords = TriangleCoords.from(x, y)
}