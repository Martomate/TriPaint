package tripaint.app

import tripaint.view.TriPaintView
import tripaint.view.TriPaintViewListener

fun interface TriPaintViewFactory {
    fun createView(controls: TriPaintViewListener, model: TriPaintModel): TriPaintView
}
