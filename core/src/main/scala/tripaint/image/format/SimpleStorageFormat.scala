package tripaint.image.format

import tripaint.coords.{StorageCoords, TriangleCoords}

object SimpleStorageFormat extends StorageFormat {
  override def transform(coords: TriangleCoords): StorageCoords =
    val x = coords.x
    val y = coords.y
    if y < x
    then StorageCoords(y, y + y - x)
    else StorageCoords(x, y)

  override def reverse(coords: StorageCoords): TriangleCoords =
    val x = coords.x
    val y = coords.y
    if y < x
    then TriangleCoords(x + x - y, x)
    else TriangleCoords(x, y)
}
