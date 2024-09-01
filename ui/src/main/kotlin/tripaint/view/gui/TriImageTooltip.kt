package tripaint.view.gui

import javafx.scene.control.Tooltip
import tripaint.coords.StorageCoords
import tripaint.grid.GridCell
import tripaint.image.ImagePool
import tripaint.image.ImageStorage

object TriImageTooltip {
    fun fromImagePool(
        content: GridCell,
        locationOfImage: (ImageStorage) -> ImagePool.SaveLocation?
    ): Tooltip {
        val getText = { makeText(content.storage.imageSize, locationOfImage(content.storage)) }

        val tooltip = Tooltip()
        tooltip.text = getText()
        tooltip.activatedProperty().addListener { _ -> tooltip.setText(getText()) }

        return tooltip
    }

    private fun makeText(imageSize: Int, saveLocation: ImagePool.SaveLocation?): String {
        val startText = if (saveLocation != null) {
            val fileName = saveLocation.file.getName()

            val hasOffset = saveLocation.offset != StorageCoords.from(0, 0)
            val offsetText = if (hasOffset) "\nOffset: ${saveLocation.offset}" else ""

            "File: $fileName$offsetText"
        } else {
            "Not saved"
        }

        return "$startText\nSize: $imageSize"
    }
}