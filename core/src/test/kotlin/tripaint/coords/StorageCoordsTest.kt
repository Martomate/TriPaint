package tripaint.coords

import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class StorageCoordsTest {
    @Test
    fun `constructor should require x is at least 0`() {
        assertThrows<IllegalArgumentException> { StorageCoords.from(-1, 0) }
        assertThrows<IllegalArgumentException> { StorageCoords.from(-10, 0) }
        assertThrows<IllegalArgumentException> { StorageCoords.from(-10, 100) }
        StorageCoords.from(0, 10)
    }

    @Test
    fun `constructor should require y is at least 0`() {
        assertThrows<IllegalArgumentException> { StorageCoords.from(0, -1) }
        assertThrows<IllegalArgumentException> { StorageCoords.from(0, -10) }
        assertThrows<IllegalArgumentException> { StorageCoords.from(100, -10) }
        StorageCoords.from(10, 0)
    }
}
