package tripaint.grid

import tripaint.coords.GridCoords
import tripaint.model.Change

class ImageGridChange(val changes: Map[GridCoords, ImageChange]) extends Change {
  override def undo(): Unit = for (_, change) <- changes do change.undo()

  override def redo(): Unit = for (_, change) <- changes do change.redo()
}
