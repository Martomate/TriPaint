package tripaint.view.gui

import javafx.scene.SnapshotParameters
import javafx.scene.control.ScrollPane
import javafx.scene.control.Tooltip
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import tripaint.color.Color
import tripaint.coords.StorageCoords
import tripaint.grid.GridCell
import tripaint.grid.ImageGrid
import tripaint.image.ImagePool
import tripaint.image.ImageStorage
import tripaint.image.format.SimpleStorageFormat
import tripaint.util.Resource
import tripaint.view.JavaFxExt.toFXColor
import tripaint.view.image.TriImageForPreview
import kotlin.math.sqrt

object ImagePreviewList {
    fun fromImageContent(
        images: List<GridCell>,
        previewSize: Int,
        locationOfImage: (ImageStorage) -> ImagePool.SaveLocation?
    ): Pair<ScrollPane, (effect: (ImageGrid) -> Unit) -> Unit> {
        val (currentEffect, setEffect) = Resource.createResource<(ImageGrid) -> Unit> { _ -> }

        val p = ScrollPane()
        p.maxWidth = previewSize * 5.0
        p.minViewportHeight = previewSize * sqrt(3.0) / 2

        currentEffect.onChange { (_, effect) ->
            p.content = images
                .withEffect(effect)
                .map { ImagePreview.fromImageContent(it, previewSize, locationOfImage) }
                .let { HBox(*it.toTypedArray()) }
        }

        setEffect { _ -> } // call handler once to set the initial content

        return Pair(p, setEffect)
    }

    private fun cloneImageContent(content: GridCell): GridCell {
        val format = SimpleStorageFormat
        val image = content.storage.toRegularImage(format)
        val imageSize = content.storage.imageSize
        val storage = ImageStorage.fromRegularImage(image, StorageCoords.from(0, 0), format, imageSize).getOrThrow()
        return GridCell(content.coords, storage)
    }

    private fun List<GridCell>.withEffect(effect: (ImageGrid) -> Unit): List<GridCell> {
        if (this.isEmpty()) return listOf()

        val imageSize = this.first().storage.imageSize

        val previewImages = this.map { cloneImageContent(it) }

        val previewImageGrid = ImageGrid(imageSize)
        for (im in previewImages) {
            previewImageGrid.set(im)
        }

        effect(previewImageGrid)

        return previewImages
    }
}


object ImagePreview {
    fun fromImageContent(
        content: GridCell,
        previewSize: Int,
        locationOfImage: (ImageStorage) -> ImagePool.SaveLocation?
    ): ImageView {
        val scale = 4.0 // this make the image look good on high dpi displays

        val preview = TriImageForPreview(content, previewSize * scale)
        val tooltip = TriImageTooltip.fromImagePool(content, locationOfImage)

        val snapshotParams = SnapshotParameters()
        snapshotParams.fill = Color.Black.withAlpha(0.0).toFXColor()

        val view = ImageView()
        val image = preview.toImage(snapshotParams)
        view.image = image
        view.fitWidth = image.width / scale
        view.fitHeight = image.height / scale
        Tooltip.install(view, tooltip)
        return view
    }
}
