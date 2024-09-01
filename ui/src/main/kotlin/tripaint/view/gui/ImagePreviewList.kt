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
import tripaint.view.JavaFxExt.toFXColor
import tripaint.view.image.TriImageForPreview

object ImagePreviewList {
    fun fromImageContent(
        images: List<GridCell>,
        previewSize: Int,
        locationOfImage: (ImageStorage) -> ImagePool.SaveLocation?
    ): Pair<ScrollPane, ((ImageGrid) -> Unit) -> Unit> {
        val imageSize = if (images.isNotEmpty()) images.first().storage.imageSize else 8

        fun makeContent(effect: (ImageGrid) -> Unit): List<ImageView> {
            val previewImages = images.map { cloneImageContent(it) }

            val previewImageGrid = ImageGrid(imageSize)
            for (im in previewImages) {
                previewImageGrid.set(im)
            }

            effect(previewImageGrid)

            return previewImages.map { im -> ImagePreview.fromImageContent(im, previewSize, locationOfImage) }
        }

        val p = ScrollPane()
        p.maxWidth = previewSize * 5.0
        p.content = HBox(*makeContent { _ -> }.toTypedArray<ImageView>())
        p.minViewportHeight = previewSize * Math.sqrt(3.0) / 2

        val updatePreview: ((ImageGrid) -> Unit) -> Unit = { effect ->
            p.content = HBox(*makeContent(effect).toTypedArray())
        }

        return Pair(p, updatePreview)
    }

    private fun cloneImageContent(content: GridCell): GridCell {
        val format = SimpleStorageFormat
        val image = content.storage.toRegularImage(format)
        val imageSize = content.storage.imageSize
        val storage = ImageStorage.fromRegularImage(image, StorageCoords.from(0, 0), format, imageSize).getOrThrow()
        return GridCell(content.coords, storage)
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
