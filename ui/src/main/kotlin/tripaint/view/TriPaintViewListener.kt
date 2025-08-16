package tripaint.view

import tripaint.view.gui.UIAction

interface TriPaintViewListener {
    fun perform(action: UIAction)
}
