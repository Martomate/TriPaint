package tripaint.model

import tripaint.model.coords.GridCoords
import tripaint.model.image.ImageChange

class ImageGridChange(val changes: Map[GridCoords, ImageChange]) extends Change {
  override def undo(): Unit = for (_, change) <- changes do change.undo()

  override def redo(): Unit = for (_, change) <- changes do change.redo()
}
