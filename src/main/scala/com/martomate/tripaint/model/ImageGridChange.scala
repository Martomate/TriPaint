package com.martomate.tripaint.model

import com.martomate.tripaint.model.coords.GridCoords
import com.martomate.tripaint.model.image.ImageChange

class ImageGridChange(val changes: Map[GridCoords, ImageChange]) extends Change {
  override def undo(): Unit = for (_, change) <- changes do change.undo()

  override def redo(): Unit = for (_, change) <- changes do change.redo()
}
