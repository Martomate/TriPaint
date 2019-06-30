package com.martomate.tripaint.model.pool

import com.martomate.tripaint.model.SaveLocation
import com.martomate.tripaint.model.save.ImageSaver
import com.martomate.tripaint.model.storage.ImageStorage

trait ImagePoolListener {
  def onImageSaved(image: ImageStorage, saver: ImageSaver): Unit
  def onImageReplaced(oldImage: ImageStorage, newImage: ImageStorage, location: SaveLocation): Unit
}
