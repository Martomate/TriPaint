package tripaint.view

import tripaint.grid.GridCell
import tripaint.view.gui.UIAction

interface TriPaintViewListener {
    fun perform(action: UIAction)

    /** Returns whether to exit or not */
    fun requestExit(): Boolean
    fun requestImageRemoval(image: GridCell)
}
