package com.martomate.tripaint.model.image.storage

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.StorageCoords
import com.martomate.tripaint.model.image.RegularImage
import com.martomate.tripaint.model.image.format.StorageFormat

import scala.util.Try

trait ImageStorageFactory {
  def fromBGColor(bgColor: Color, imageSize: Int): ImageStorage
  def fromRegularImage(image: RegularImage, offset: StorageCoords, format: StorageFormat, imageSize: Int): Try[ImageStorage]
}
