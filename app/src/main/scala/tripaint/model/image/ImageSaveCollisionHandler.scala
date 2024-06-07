package tripaint.model.image

import tripaint.image.ImageStorage

trait ImageSaveCollisionHandler {
  def shouldReplaceImage(
      currentImage: ImageStorage,
      newImage: ImageStorage,
      location: ImagePool.SaveLocation
  ): Option[Boolean]
}
