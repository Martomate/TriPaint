package tripaint.view.image

import javafx.scene.SnapshotParameters
import javafx.scene.image.Image
import javafx.scene.layout.Pane
import tripaint.coords.TriangleCoords
import tripaint.grid.GridCell
import tripaint.image.ImageStorage

import tripaint.grid.GridCell.Event.*

class TriImageForPreview(private val content: GridCell, previewWidth: Double) : Pane() {
    private val storage: ImageStorage
        get() = content.storage

    private val canvas: TriImageCanvas = TriImageCanvas(previewWidth, storage.imageSize)

    init {
        content.trackChanges { onImageChanged(it) }
        this.children.add(canvas)

        redraw()
    }

    private fun drawTriangle(coords: TriangleCoords) {
        canvas.drawTriangle(coords, storage.getColor(coords), storage)
    }

    private fun redraw() {
        canvas.clearCanvas()
        canvas.redraw(storage)
    }

    private fun onImageChanged(event: GridCell.Event) {
        when (event) {
            is PixelChanged ->
                drawTriangle(event.coords)
            is ImageChangedALot ->
                redraw()
            else -> {}
        }
    }

    fun toImage(params: SnapshotParameters): Image {
        return canvas.snapshot(params, null)
    }

    companion object {
        const val previewSize: Int = 64
    }
}
