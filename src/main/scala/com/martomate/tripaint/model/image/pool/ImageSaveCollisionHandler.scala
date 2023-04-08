package com.martomate.tripaint.model.image.pool

import com.martomate.tripaint.model.image.{ImageStorage, SaveLocation}

trait ImageSaveCollisionHandler {
  def shouldReplaceImage(
      currentImage: ImageStorage,
      newImage: ImageStorage,
      location: SaveLocation
  ): Option[Boolean]
}
