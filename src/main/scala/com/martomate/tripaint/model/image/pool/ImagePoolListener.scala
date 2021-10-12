package com.martomate.tripaint.model.image.pool

import com.martomate.tripaint.model.image.SaveLocation
import com.martomate.tripaint.model.image.save.ImageSaverToFile
import com.martomate.tripaint.model.image.storage.ImageStorage

trait ImagePoolListener {
  def onImageSaved(image: ImageStorage, saver: ImageSaverToFile): Unit
  def onImageReplaced(oldImage: ImageStorage, newImage: ImageStorage, location: SaveLocation): Unit
}
