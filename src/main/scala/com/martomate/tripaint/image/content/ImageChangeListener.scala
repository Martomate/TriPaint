package com.martomate.tripaint.image.content

import com.martomate.tripaint.image.storage.{ImageStorage, ImageStorageListener}

trait ImageChangeListener extends ImageStorageListener {
  def onImageChangedALot(): Unit
}
