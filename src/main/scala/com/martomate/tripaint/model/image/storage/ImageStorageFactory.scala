package com.martomate.tripaint.model.image.storage

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.image.SaveLocation
import com.martomate.tripaint.model.image.format.StorageFormat

import scala.util.Try

trait ImageStorageFactory {
  def fromBGColor(bgColor: Color, imageSize: Int): ImageStorage
  def fromFile(saveLocation: SaveLocation, format: StorageFormat, imageSize: Int, fileSystem: FileSystem): Try[ImageStorage]
}
