package com.martomate.tripaint.view.gui

import com.martomate.tripaint.model.coords.StorageCoords
import com.martomate.tripaint.model.image.{ImageStorage, SaveLocation}
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.model.image.pool.ImagePool
import scalafx.scene.control.Tooltip

object TriImageTooltip:
  def fromImagePool(
      content: ImageContent,
      locationOfImage: ImageStorage => Option[SaveLocation]
  ): Tooltip =
    val tooltip = new Tooltip()

    tooltip.text = makeText(content, locationOfImage)
    tooltip.activated.onChange(tooltip.text = makeText(content, locationOfImage))

    tooltip

  private def makeText(
      content: ImageContent,
      locationOfImage: ImageStorage => Option[SaveLocation]
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
