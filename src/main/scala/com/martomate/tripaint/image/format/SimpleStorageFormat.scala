package com.martomate.tripaint.image.format

import com.martomate.tripaint.image.coords.{StorageCoords, TriangleCoords}

// current format
class SimpleStorageFormat extends StorageFormat {
  override def transformToStorage(coords: TriangleCoords): StorageCoords = {
    val TriangleCoords(x, y) = coords
    if (y < x) StorageCoords(y, y + y - x)
    else StorageCoords(x, y)
  }

  override def transformFromStorage(coords: StorageCoords): TriangleCoords = {
    val StorageCoords(x, y) = coords
    if (y < x) TriangleCoords(x + x - y, x)
    else TriangleCoords(x, y)
  }
}
