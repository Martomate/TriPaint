package tripaint.grid

import tripaint.image.{ImagePool, ImageStorage}

trait ImageSaveCollisionHandler {
  def shouldReplaceImage(
      currentImage: ImageStorage,
      newImage: ImageStorage,
      location: ImagePool.SaveLocation
  ): Option[Boolean]
}
