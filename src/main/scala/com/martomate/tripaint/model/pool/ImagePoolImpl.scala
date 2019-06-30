package com.martomate.tripaint.model.pool

import com.martomate.tripaint.model.SaveLocation
import com.martomate.tripaint.model.storage.{ImageStorage, ImageStorageFactory}
import com.martomate.tripaint.util.{InjectiveHashMap, InjectiveMap}

class ImagePoolImpl(factory: ImageStorageFactory) extends ImagePool(factory) {
  override protected val mapping: InjectiveMap[SaveLocation, ImageStorage] = new InjectiveHashMap[SaveLocation, ImageStorage]

  override def move(image: ImageStorage, to: SaveLocation)(implicit handler: ImageSaveCollisionHandler): Boolean = {
    val newLocation = to
    val currentImage = get(newLocation)

    if (currentImage == null) {
      mapping.set(to, image)
      true
    } else if (currentImage != image) {
      handler.shouldReplaceImage(currentImage, image, newLocation) match {
        case Some(replace) =>
          if (replace) {
            mapping.removeRight(currentImage)
            set(newLocation, image)
            notifyListeners(_.onImageReplaced(currentImage, image, newLocation))
          } else {
            mapping.removeRight(image)
            notifyListeners(_.onImageReplaced(image, currentImage, newLocation))
          }
          true
        case None =>
          false
      }
    } else true
  }
}
