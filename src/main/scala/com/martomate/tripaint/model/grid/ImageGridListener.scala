package com.martomate.tripaint.model.grid

import com.martomate.tripaint.model.content.ImageContent

trait ImageGridListener {
  def onAddImage(image: ImageContent): Unit
  def onRemoveImage(image: ImageContent): Unit
}
