package tripaint.model.image.format

import tripaint.model.coords.{StorageCoords, TriangleCoords}

object SimpleStorageFormat extends StorageFormat {
  override def transform(coords: TriangleCoords): StorageCoords =
    val TriangleCoords(x, y) = coords
    if y < x
    then StorageCoords(y, y + y - x)
    else StorageCoords(x, y)

  override def reverse(coords: StorageCoords): TriangleCoords =
    val StorageCoords(x, y) = coords
    if y < x
    then TriangleCoords(x + x - y, x)
    else TriangleCoords(x, y)
}
