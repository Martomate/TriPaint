package com.martomate.tripaint.image.storage

import com.martomate.tripaint.image.SaveLocation
import scalafx.scene.paint.Color

import scala.util.Try

trait ImageStorageFactory {
  def fromBGColor(bgColor: Color, imageSize: Int): ImageStorage
  def fromFile(saveLocation: SaveLocation, imageSize: Int): Try[ImageStorage]
}
