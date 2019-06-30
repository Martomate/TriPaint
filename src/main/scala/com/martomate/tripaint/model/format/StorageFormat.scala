package com.martomate.tripaint.model.format

import com.martomate.tripaint.model.coords.{StorageCoords, TriangleCoords}

trait StorageFormat {
  def transformToStorage(coords: TriangleCoords): StorageCoords

  def transformFromStorage(coords: StorageCoords): TriangleCoords
}
