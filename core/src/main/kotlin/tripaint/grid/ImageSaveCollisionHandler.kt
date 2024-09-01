package tripaint.grid

import tripaint.image.ImagePool
import tripaint.image.ImageStorage

interface ImageSaveCollisionHandler {
    fun shouldReplaceImage(
        currentImage: ImageStorage,
        newImage: ImageStorage,
        location: ImagePool.SaveLocation
    ): Boolean?
}
