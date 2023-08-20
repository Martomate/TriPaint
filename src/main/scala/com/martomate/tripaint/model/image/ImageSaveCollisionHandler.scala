package com.martomate.tripaint.model.image

trait ImageSaveCollisionHandler {
  def shouldReplaceImage(
      currentImage: ImageStorage,
      newImage: ImageStorage,
      location: ImagePool.SaveLocation
  ): Option[Boolean]
}
