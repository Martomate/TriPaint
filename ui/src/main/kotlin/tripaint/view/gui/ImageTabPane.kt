package tripaint.view.gui

import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ToggleButton
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.StackPane
import tripaint.grid.GridCell
import tripaint.image.ImagePool
import tripaint.view.image.TriImageForPreview

object ImageTabPane {
    fun apply(
        image: GridCell,
        requestImageRemoval: (GridCell) -> Unit,
        imagePool: ImagePool
    ): StackPane {
        val preview = TriImageForPreview(image, TriImageForPreview.previewSize.toDouble())

        val closeButton = run {
            val b = Button()
            b.text = "X"
            b.isVisible = false
            b.setOnAction { _ -> requestImageRemoval(image) }
            b
        }

        val previewButton = run {
            val b = ToggleButton()
            b.graphic = preview
            b.tooltip = TriImageTooltip.fromImagePool(image) { imagePool.locationOf(it) }

            image.trackChanges { event ->
                when (event) {
                    is GridCell.Event.StateUpdated ->
                        b.isSelected = event.editable
                    else -> {}
                }
            }
            b.selectedProperty().addListener { _, _, selected ->
                image.editable = selected
            }
            b.isSelected = image.editable

            b
        }

        val starView: ImageView = makeStarView(image)

        val stackPane = run {
            val p = StackPane(previewButton, closeButton, starView)
            StackPane.setAlignment(closeButton, Pos.TOP_RIGHT)
            StackPane.setAlignment(starView, Pos.TOP_LEFT)
            p.setOnMouseEntered { _ ->
                closeButton.isVisible = true
            }
            p.setOnMouseExited { _ ->
                closeButton.isVisible = false
            }
            p
        }

        return stackPane
    }

    private fun makeStarView(image: GridCell): ImageView {
        val star = ImageView()
        star.image = Image("/icons/star.png")
        star.isMouseTransparent = true

        image.trackChanges { event ->
            when (event) {
                is GridCell.Event.StateUpdated ->
                    star.isVisible = event.changed
                else -> {}
            }
        }
        star.isVisible = image.changed

        return star
    }
}
