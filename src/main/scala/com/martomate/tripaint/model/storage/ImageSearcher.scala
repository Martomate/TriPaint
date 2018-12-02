package com.martomate.tripaint.model.storage

import com.martomate.tripaint.model.coords.TriangleCoords

class ImageSearcher(image: ImageStorage) {

  def neighbours(coords: TriangleCoords): Seq[TriangleCoords] = {
    val TriangleCoords(x, y) = coords
    Seq(
      TriangleCoords(x - 1, y),
      if (x % 2 == 0) TriangleCoords(x + 1, y + 1)
      else TriangleCoords(x - 1, y - 1),
      TriangleCoords(x + 1, y)
    ).filter(t => t.x >= 0 && t.y >= 0 && t.x < 2 * t.y + 1 && t.y < image.imageSize)
  }

  def search(from: TriangleCoords, predicate: TriangleCoords => Boolean): Seq[TriangleCoords] = {
    val visited = collection.mutable.Set.empty[TriangleCoords]
    val result = collection.mutable.ArrayBuffer.empty[TriangleCoords]
    val q = collection.mutable.Queue(from)

    if (image.contains(from)) {
      visited += from

      while (q.nonEmpty) {
        val p = q.dequeue

        if (predicate(p)) {
          result += p

          val newOnes = neighbours(p).filter(!visited(_))
          visited ++= newOnes
          q ++= newOnes
        }
      }
    }
    result
  }
}
