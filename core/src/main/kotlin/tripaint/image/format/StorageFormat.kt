package tripaint.image.format

import tripaint.coords.StorageCoords
import tripaint.coords.TriangleCoords

interface StorageFormat {
    fun transform(coords: TriangleCoords): StorageCoords

    fun reverse(coords: StorageCoords): TriangleCoords
}
