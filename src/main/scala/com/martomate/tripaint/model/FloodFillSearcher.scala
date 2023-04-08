package com.martomate.tripaint.model

import com.martomate.tripaint.model.coords.GlobalPixCoords

class FloodFillSearcher(colorLookup: ColorLookup) {
  def search(
      startPos: GlobalPixCoords,
      predicate: (GlobalPixCoords, Color) => Boolean
  ): Seq[GlobalPixCoords] = {
    val visited = collection.mutable.Set.empty[GlobalPixCoords]
    val result = collection.mutable.ArrayBuffer.empty[GlobalPixCoords]
    val q = collection.mutable.Queue(startPos)
    visited += startPos

    while (q.nonEmpty) {
      val p = q.dequeue()
      colorLookup.lookup(p) foreach { color =>
        if (predicate(p, color)) {
          result += p

          val newOnes = p.neighbours.filter(!visited(_))
          visited ++= newOnes
          q ++= newOnes
        }
      }
    }
    result.toSeq
  }
}
