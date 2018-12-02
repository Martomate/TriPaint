package com.martomate.tripaint.model.format

import com.martomate.tripaint.model.coords.{StorageCoords, TriangleCoords}

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
