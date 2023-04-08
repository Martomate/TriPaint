package com.martomate.tripaint.model.image

import com.martomate.tripaint.model.coords.StorageCoords
import com.martomate.tripaint.model.image.format.StorageFormat

object ImageUtils:
  def overwritePartOfImage(
      image: ImageStorage,
      format: StorageFormat,
      offset: StorageCoords,
      oldImage: Option[RegularImage]
  ): RegularImage =
    val neededWidth = offset.x + image.imageSize
    val neededHeight = offset.y + image.imageSize

    val bufImage: RegularImage = oldImage match
      case Some(im) =>
        enlargeImageIfNeeded(im, neededWidth, neededHeight)
      case None =>
        RegularImage.ofSize(neededWidth, neededHeight)

    bufImage.pasteImage(offset, image.toRegularImage(format))
    bufImage

  private def enlargeImageIfNeeded(
      image: RegularImage,
      neededWith: Int,
      neededHeight: Int
  ): RegularImage =
    if image.width < neededWith || image.height < neededHeight then
      val newImage = RegularImage.ofSize(neededWith, neededHeight)
      newImage.pasteImage(StorageCoords(0, 0), image)
      newImage
    else image

  def convertImageToArray(image: ImageStorage, format: StorageFormat): Array[Int] =
    val size = image.imageSize
    val array = new Array[Int](size * size)

    for triCoords <- image.allPixels do
      val stCoords = format.transformToStorage(triCoords)
      array(stCoords.x + stCoords.y * size) = image(triCoords).toInt

    array
