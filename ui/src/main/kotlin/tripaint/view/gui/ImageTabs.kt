package tripaint.view.gui

import javafx.scene.layout.StackPane
import javafx.scene.layout.TilePane
import tripaint.grid.GridCell
import tripaint.grid.ImageGrid
import tripaint.image.ImagePool
import tripaint.view.image.TriImageForPreview

class ImageTabs(private val imagePool: ImagePool, private val requestImageRemoval: (GridCell) -> Unit) : TilePane() {
    init {
        this.maxWidth = TriImageForPreview.previewSize.toDouble()
    }

    private val imageTabMap: MutableMap<GridCell, StackPane> = mutableMapOf()

    fun onImageGridEvent(event: ImageGrid.Event) {
        when (event) {
            is ImageGrid.Event.ImageAdded -> {
                val (image) = event
                val tab = ImageTabPane.apply(image, requestImageRemoval, imagePool)
                this.children.add(tab)
                imageTabMap[image] = tab
            }
            is ImageGrid.Event.ImageRemoved -> {
                val (image) = event
                val pane = imageTabMap.remove(image)
                if (pane != null) {
                    this.children.remove(pane)
                }
            }
            else -> {}
        }
    }

    companion object {
        fun fromImagePool(
            imageGrid: ImageGrid,
            imagePool: ImagePool,
            requestImageRemoval: (GridCell) -> Unit
        ): TilePane {
            val tilePane = ImageTabs(imagePool, requestImageRemoval)
            imageGrid.trackChanges { tilePane.onImageGridEvent(it) }
            return tilePane
        }
    }
}
