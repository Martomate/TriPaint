package com.martomate.tripaint.model.save

import com.martomate.tripaint.model.SaveLocation
import com.martomate.tripaint.model.format.StorageFormat
import com.martomate.tripaint.model.storage.ImageStorage
import scalafx.scene.paint.Color

class ImageSaverToArray(val array: Array[Int]) extends ImageSaver {
  override def save(image: ImageStorage, format: StorageFormat, saveInfo: SaveLocation): Boolean = {
    val size = image.imageSize

    for (triCoords <- image.allPixels) {
      val stCoords = format.transformToStorage(triCoords)
      array(stCoords.x + stCoords.y * size) = colorToInt(image(triCoords))
    }

    true
  }

}
