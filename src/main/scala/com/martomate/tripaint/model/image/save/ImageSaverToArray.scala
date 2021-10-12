package com.martomate.tripaint.model.image.save

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model.image.SaveLocation
import com.martomate.tripaint.model.image.format.StorageFormat
import com.martomate.tripaint.model.image.storage.ImageStorage
import scalafx.scene.paint.Color

class ImageSaverToArray(val array: Array[Int]) {
  def save(image: ImageStorage, format: StorageFormat, saveInfo: SaveLocation, fileSystem: FileSystem): Boolean = {
    val size = image.imageSize

    for (triCoords <- image.allPixels) {
      val stCoords = format.transformToStorage(triCoords)
      array(stCoords.x + stCoords.y * size) = colorToInt(image(triCoords))
    }

    true
  }

  protected def colorToInt(col: Color): Int = {
    (col.opacity * 255).toInt << 24 |
      (col.red     * 255).toInt << 16 |
      (col.green   * 255).toInt <<  8 |
      (col.blue    * 255).toInt
  }

}

object ImageSaverToArray {
  def fromSize(imageSize: Int): ImageSaverToArray =
    new ImageSaverToArray(new Array[Int](imageSize * imageSize))
}
