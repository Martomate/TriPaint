package tripaint.app

import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import tripaint.coords.GridCoords
import tripaint.grid.GridCell
import tripaint.grid.ImageGrid
import tripaint.view.image.ImageGridCanvas

object PreviewScene {
    fun create(imageGrid: ImageGrid): Scene {
        val previewImageGrid = ImageGrid(imageGrid.imageSize)
        val canvas = ImageGridCanvas(previewImageGrid)
        canvas.width = 400.0
        canvas.height = 400.0

        val updatePreview = {
            previewImageGrid.setImageSizeIfEmpty(imageGrid.imageSize)

            previewImageGrid.images().forEach {
                previewImageGrid.remove(it.coords)
            }

            val images = imageGrid.images()
            if (images.size == 1) {
                val image = images.first()
                for (x in -12..12) {
                    for (y in -4..4) {
                        val coords = GridCoords.from(x, y)
                        previewImageGrid.set(GridCell(coords, image.storage))
                    }
                }
            }
        }
        imageGrid.trackChanges { when (it) {
            is ImageGrid.Event.ImageAdded -> {
                updatePreview()
                canvas.redraw()
            }
            is ImageGrid.Event.ImageRemoved -> {
                updatePreview()
                canvas.redraw()
            }
            is ImageGrid.Event.ImageChangedALot -> {
                canvas.redraw()
            }
            is ImageGrid.Event.PixelChanged -> {}
        } }

        updatePreview()
        canvas.redraw()

        val sceneContents = BorderPane(canvas, null, null, null, null)

        return Scene(sceneContents)
    }
}
