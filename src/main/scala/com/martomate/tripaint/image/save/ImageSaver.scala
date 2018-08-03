package com.martomate.tripaint.image.save

import com.martomate.tripaint.image.SaveLocation
import com.martomate.tripaint.image.storage.ImageStorage

trait ImageSaver {
  def save(image: ImageStorage, saveInfo: SaveLocation): Boolean
}
