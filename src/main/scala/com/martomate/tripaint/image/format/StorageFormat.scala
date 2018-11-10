package com.martomate.tripaint.image.format

import com.martomate.tripaint.image.coords.{StorageCoords, TriangleCoords}

trait StorageFormat {
  def transformToStorage(coords: TriangleCoords): StorageCoords

  def transformFromStorage(coords: StorageCoords): TriangleCoords
}
