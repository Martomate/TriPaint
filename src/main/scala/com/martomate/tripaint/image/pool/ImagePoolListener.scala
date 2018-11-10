package com.martomate.tripaint.image.pool

import com.martomate.tripaint.image.SaveLocation
import com.martomate.tripaint.image.save.ImageSaver
import com.martomate.tripaint.image.storage.ImageStorage

trait ImagePoolListener {
  def onImageSaved(image: ImageStorage, saver: ImageSaver): Unit
  def onImageReplaced(oldImage: ImageStorage, newImage: ImageStorage, location: SaveLocation): Unit
}
