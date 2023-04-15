package com.martomate.tripaint.control.action

import com.martomate.tripaint.model.{Color, ImageGrid, TriPaintModel}
import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.model.image.ImageStorage
import com.martomate.tripaint.model.image.content.ImageContent

class NewAction(imageGrid: ImageGrid, backgroundColor: Color, whereToPutImage: TriImageCoords)
    extends Action {
  override def perform(): Unit = {
    imageGrid.set(
      new ImageContent(
        whereToPutImage,
        ImageStorage.fromBGColor(backgroundColor, imageGrid.imageSize)
      )
    )
  }
}
