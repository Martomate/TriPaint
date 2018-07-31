package com.martomate.tripaint.image2.format

import com.martomate.tripaint.image2.coords.{StorageCoords, TriangleCoords}

// current format
class SimpleStorageFormat(imageSize: Int) extends StorageFormat {
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
