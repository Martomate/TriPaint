package com.martomate.tripaint.model.storage

import com.martomate.tripaint.model.SaveLocation
import com.martomate.tripaint.model.format.StorageFormat
import scalafx.scene.paint.Color

import scala.util.Try

trait ImageStorageFactory {
  def fromBGColor(bgColor: Color, imageSize: Int): ImageStorage
  def fromFile(saveLocation: SaveLocation, format: StorageFormat, imageSize: Int): Try[ImageStorage]
}
