package com.martomate.tripaint.model.image.content

import com.martomate.tripaint.model.image.storage.ImageStorageListener

trait ImageChangeListener extends ImageStorageListener {
  def onImageChangedALot(): Unit
}
