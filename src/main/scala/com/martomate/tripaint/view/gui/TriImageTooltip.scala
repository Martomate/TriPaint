package com.martomate.tripaint.view.gui

import com.martomate.tripaint.model.coords.StorageCoords
import com.martomate.tripaint.model.image.{ImagePool, ImageStorage}
import com.martomate.tripaint.model.image.content.GridCell
import scalafx.scene.control.Tooltip

object TriImageTooltip:
  def fromImagePool(
                     content: GridCell,
                     locationOfImage: ImageStorage => Option[ImagePool.SaveLocation]
  ): Tooltip =
    val tooltip = new Tooltip()

    tooltip.text = makeText(content, locationOfImage)
    tooltip.activated.onChange(tooltip.text = makeText(content, locationOfImage))

    tooltip

  private def makeText(
                        content: GridCell,
                        locationOfImage: ImageStorage => Option[ImagePool.SaveLocation]
  ) =
    val storage = content.storage
    val startText = locationOfImage(storage) match
      case Some(location) =>
        val fileName = location.file.getName
        val offsetText =
          if location.offset == StorageCoords(0, 0)
          then ""
          else s"\nOffset: ${location.offset}"
        s"File: $fileName" + offsetText
      case None => "Not saved"

    val newText = s"$startText\nSize: ${storage.imageSize}"
    newText
