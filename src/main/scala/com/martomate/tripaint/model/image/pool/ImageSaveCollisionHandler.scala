package com.martomate.tripaint.model.image.pool

import com.martomate.tripaint.model.image.SaveLocation
import com.martomate.tripaint.model.image.storage.ImageStorage

trait ImageSaveCollisionHandler {
  def shouldReplaceImage(currentImage: ImageStorage, newImage: ImageStorage, location: SaveLocation): Option[Boolean]
}
