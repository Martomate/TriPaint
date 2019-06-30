package com.martomate.tripaint.model.pool

import com.martomate.tripaint.model.SaveLocation
import com.martomate.tripaint.model.storage.ImageStorage

trait ImageSaveCollisionHandler {
  def shouldReplaceImage(currentImage: ImageStorage, newImage: ImageStorage, location: SaveLocation): Option[Boolean]
}
