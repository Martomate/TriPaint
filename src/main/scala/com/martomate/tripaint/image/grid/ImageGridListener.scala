package com.martomate.tripaint.image.grid

import com.martomate.tripaint.image.graphics.TriImage

trait ImageGridListener {
  def onAddImage(image: TriImage): Unit
  def onRemoveImage(image: TriImage): Unit
}
