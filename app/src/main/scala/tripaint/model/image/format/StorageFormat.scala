package tripaint.model.image.format

import tripaint.coords.{StorageCoords, TriangleCoords}

trait StorageFormat {
  def transform(coords: TriangleCoords): StorageCoords

  def reverse(coords: StorageCoords): TriangleCoords
}
