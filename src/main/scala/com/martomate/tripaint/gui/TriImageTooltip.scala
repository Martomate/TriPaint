package com.martomate.tripaint.gui

import com.martomate.tripaint.image.content.ImageContent
import com.martomate.tripaint.image.pool.ImagePool
import scalafx.scene.control.Tooltip

class TriImageTooltip(content: ImageContent, imagePool: ImagePool) extends Tooltip {
  updateText()

  activated.onChange(updateText())

  def updateText(): Unit = {
    val storage = content.storage
    val startText = imagePool.locationOf(storage) map { location =>
      val fileName = location.file.getName
      val offsetText =
        if (location.offset == (0, 0)) ""
        else s"\nOffset: ${location.offset}"
      s"File: $fileName" + offsetText
    } getOrElse "Not saved"

    text = s"$startText\nSize: ${storage.imageSize}"
  }
}
