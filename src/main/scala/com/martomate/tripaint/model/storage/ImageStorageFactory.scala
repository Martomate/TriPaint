package com.martomate.tripaint.model.storage

import com.martomate.tripaint.model.SaveLocation
import scalafx.scene.paint.Color

import scala.util.Try

trait ImageStorageFactory {
  def fromBGColor(bgColor: Color, imageSize: Int): ImageStorage
  def fromFile(saveLocation: SaveLocation, imageSize: Int): Try[ImageStorage]
}
