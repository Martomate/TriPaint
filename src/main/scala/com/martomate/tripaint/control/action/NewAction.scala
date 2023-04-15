package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.{Color, TriPaintModel}
import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.model.image.ImageStorage
import com.martomate.tripaint.model.image.content.ImageContent

class NewAction(
    model: TriPaintModel,
    backgroundColor: Color,
    askForWhereToPutImage: () => Option[(Int, Int)]
) extends Action {
  override def perform(): Unit = {
    askForWhereToPutImage() match {
      case Some((x, y)) =>
        val storage = ImageStorage.fromBGColor(backgroundColor, model.imageGrid.imageSize)
        val content = new ImageContent(TriImageCoords(x, y), storage)
        model.imageGrid.set(content)
      case _ =>
    }
  }
}
