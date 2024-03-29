package com.martomate.tripaint.view.gui

import com.martomate.tripaint.model.coords.StorageCoords
import com.martomate.tripaint.model.image.{GridCell, ImagePool, ImageStorage}

import scalafx.scene.control.Tooltip

object TriImageTooltip {
  def fromImagePool(
      content: GridCell,
      locationOfImage: ImageStorage => Option[ImagePool.SaveLocation]
  ): Tooltip = {
    val getText = () => makeText(content.storage.imageSize, locationOfImage(content.storage))

    val tooltip = new Tooltip()
    tooltip.text = getText()
    tooltip.activated.onChange(tooltip.text = getText())

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
