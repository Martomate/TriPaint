package com.martomate.tripaint.image2.format

import com.martomate.tripaint.image2.coords.{StorageCoords, TriangleCoords}

trait StorageFormat {
  def transformToStorage(coords: TriangleCoords): StorageCoords

  def transformFromStorage(coords: StorageCoords): TriangleCoords
}
