package tripaint.image.format

import tripaint.coords.StorageCoords
import tripaint.coords.TriangleCoords

object SimpleStorageFormat : StorageFormat {
    override fun supportsImageSize(size: Int): Boolean {
        return true
    }

    override fun transform(coords: TriangleCoords): StorageCoords {
        val x = coords.x
        val y = coords.y
        return if (y < x) {
            StorageCoords.from(y, y + y - x)
        } else {
            StorageCoords.from(x, y)
        }
    }

    override fun reverse(coords: StorageCoords): TriangleCoords {
        val x = coords.x
        val y = coords.y
        return if (y < x) {
            TriangleCoords.from(x + x - y, x)
        } else {
            TriangleCoords.from(x, y)
        }
    }
}
