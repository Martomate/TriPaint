package com.martomate.tripaint.model.image.format

import com.martomate.tripaint.model.coords.{StorageCoords, TriangleCoords}

trait StorageFormat {
  def transform(coords: TriangleCoords): StorageCoords

  def reverse(coords: StorageCoords): TriangleCoords
}
