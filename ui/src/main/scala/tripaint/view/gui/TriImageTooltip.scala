package tripaint.view.gui

import tripaint.coords.StorageCoords
import tripaint.grid.GridCell
import tripaint.image.{ImagePool, ImageStorage}

import javafx.scene.control.Tooltip

object TriImageTooltip {
  def fromImagePool(
      content: GridCell,
      locationOfImage: ImageStorage => Option[ImagePool.SaveLocation]
  ): Tooltip = {
    val getText = () => makeText(content.storage.imageSize, locationOfImage(content.storage))

    val tooltip = new Tooltip()
    tooltip.setText(getText())
    tooltip.activatedProperty.addListener(_ => tooltip.setText(getText()))

    tooltip
  }

  private def makeText(imageSize: Int, saveLocation: Option[ImagePool.SaveLocation]) = {
    val startText = saveLocation match {
      case Some(location) =>
        val fileName = location.file.getName

        val hasOffset = location.offset != StorageCoords(0, 0)
        val offsetText = if hasOffset then s"\nOffset: ${location.offset}" else ""

        s"File: $fileName" + offsetText
      case None =>
        "Not saved"
    }

    s"$startText\nSize: $imageSize"
  }
}
