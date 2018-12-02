package com.martomate.tripaint.model.save

import com.martomate.tripaint.model.SaveLocation
import com.martomate.tripaint.model.storage.ImageStorage

trait ImageSaver {
  def save(image: ImageStorage, saveInfo: SaveLocation): Boolean
}
