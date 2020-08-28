package com.martomate.tripaint.model.save

import com.martomate.tripaint.model.SaveLocation
import com.martomate.tripaint.model.format.StorageFormat
import com.martomate.tripaint.model.storage.ImageStorage
import scalafx.scene.paint.Color

trait ImageSaver {
  def save(image: ImageStorage, format: StorageFormat, saveInfo: SaveLocation): Boolean

  protected def colorToInt(col: Color): Int = {
    (col.opacity * 255).toInt << 24 |
      (col.red     * 255).toInt << 16 |
      (col.green   * 255).toInt <<  8 |
      (col.blue    * 255).toInt
  }

}
