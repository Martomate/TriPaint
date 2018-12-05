package com.martomate.tripaint.model.content

import com.martomate.tripaint.model.storage.ImageStorageListener

trait ImageChangeListener extends ImageStorageListener {
  def onImageChangedALot(): Unit
}
