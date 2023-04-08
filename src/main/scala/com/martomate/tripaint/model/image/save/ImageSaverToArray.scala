package com.martomate.tripaint.model.image.save

import com.martomate.tripaint.model.image.ImageStorage
import com.martomate.tripaint.model.image.format.StorageFormat

class ImageSaverToArray(val array: Array[Int]) {
  def save(image: ImageStorage, format: StorageFormat): Unit = {
    val size = image.imageSize

    for (triCoords <- image.allPixels) {
      val stCoords = format.transformToStorage(triCoords)
      array(stCoords.x + stCoords.y * size) = image(triCoords).toInt
    }
  }

}

object ImageSaverToArray {
  def fromSize(imageSize: Int): ImageSaverToArray =
    new ImageSaverToArray(new Array[Int](imageSize * imageSize))
}
