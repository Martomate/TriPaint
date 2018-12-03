package com.martomate.tripaint.view.image.grid

import com.martomate.tripaint.model.coords.PixelCoords
import scalafx.scene.paint.Color

class ImageGridSearcher(imageGrid: ImageGrid) {
  def search(startPos: PixelCoords, predicate: (PixelCoords, Color) => Boolean): Seq[PixelCoords] = {
    val visited = collection.mutable.Set.empty[PixelCoords]
    val result = collection.mutable.ArrayBuffer.empty[PixelCoords]
    val q = collection.mutable.Queue(startPos)
    visited += startPos

    while (q.nonEmpty) {
      val p = q.dequeue
      imageGrid(p.image) foreach { image =>
        val color = image.storage(p.pix)
        if (predicate(p, color)) {
          result += p

          val newOnes = p.neighbours(imageGrid.imageSize).filter(!visited(_))
          visited ++= newOnes
          q ++= newOnes
        }
      }
    }
    result
  }
}
