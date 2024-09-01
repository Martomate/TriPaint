package tripaint.grid

import tripaint.Change
import tripaint.color.Color
import tripaint.coords.TriangleCoords
import tripaint.image.ImageStorage

class ImageChange(private val image: ImageStorage, private val pixelsChanged: List<PixelChange>) : Change {
    override fun redo() {
        for (ch in pixelsChanged) {
            image.setColor(ch.coords, ch.after)
        }
    }

    override fun undo() {
        for (ch in pixelsChanged) {
            image.setColor(ch.coords, ch.before)
        }
    }

    data class PixelChange(val coords: TriangleCoords, val before: Color, val after: Color)

    class Builder {
        private val pixelsChanged = mutableListOf<PixelChange>()

        fun done(image: ImageStorage): ImageChange {
            val change = ImageChange (image, pixelsChanged.reversed())
            pixelsChanged.clear()
            return change
        }

        fun addChange(index: TriangleCoords, oldColor: Color, newColor: Color): Builder {
            pixelsChanged += PixelChange(index, oldColor, newColor)
            return this
        }

        fun nonEmpty(): Boolean = !pixelsChanged.isEmpty()
    }

    companion object {
        fun builder(): Builder = Builder()
    }
}
