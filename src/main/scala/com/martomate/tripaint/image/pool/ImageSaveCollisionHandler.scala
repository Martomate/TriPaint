package com.martomate.tripaint.image.pool

import com.martomate.tripaint.image.SaveLocation
import com.martomate.tripaint.image.storage.ImageStorage

trait ImageSaveCollisionHandler {
  def shouldReplaceImage(currentImage: ImageStorage, newImage: ImageStorage, location: SaveLocation): Option[Boolean]
}
