package com.martomate.tripaint.view.image.grid

import com.martomate.tripaint.view.image.TriImage

trait ImageGridListener {
  def onAddImage(image: TriImage): Unit
  def onRemoveImage(image: TriImage): Unit
}
