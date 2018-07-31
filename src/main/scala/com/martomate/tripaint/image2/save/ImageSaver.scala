package com.martomate.tripaint.image2.save

import com.martomate.tripaint.image2.storage.ImageStorage

trait ImageSaver {
  def save(image: ImageStorage, saveInfo: ImageSaveInfo): Boolean
}
