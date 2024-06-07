package tripaint.model.image.format

import tripaint.model.coords.{StorageCoords, TriangleCoords}

trait StorageFormat {
  def transform(coords: TriangleCoords): StorageCoords

  def reverse(coords: StorageCoords): TriangleCoords
}
