package tripaint.grid

import tripaint.Change
import tripaint.coords.GridCoords

class ImageGridChange(val changes: Map<GridCoords, ImageChange>) : Change {
    override fun undo() {
        for ((_, change) in changes) {
            change.undo()
        }
    }

    override fun redo() {
        for ((_, change) in changes) {
            change.redo()
        }
    }
}