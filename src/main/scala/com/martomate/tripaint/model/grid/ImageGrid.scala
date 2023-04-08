package com.martomate.tripaint.model.grid

import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.model.image.SaveLocation
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.model.image.pool.ImagePool
import com.martomate.tripaint.model.image.storage.ImageStorage
import com.martomate.tripaint.util.Listenable

import scala.collection.mutable.ArrayBuffer

class ImageGrid(init_imageSize: Int) extends Listenable[ImageGridListener] {
  private var _imageSize: Int = init_imageSize
  def imageSize: Int = _imageSize

  private val _images: ArrayBuffer[ImageContent] = ArrayBuffer.empty
  def images: Seq[ImageContent] = _images.toSeq

  def apply(coords: TriImageCoords): Option[ImageContent] = _images.find(_.coords == coords)

  def set(image: ImageContent): Unit = {
    val idx = _images.indexWhere(_.coords == image.coords)
    if (idx != -1) {
      val prev = _images(idx)
      if (prev != image) onRemoveImage(prev)
      _images(idx) = image
    } else _images += image
    onAddImage(image)
  }

  def -=(coords: TriImageCoords): ImageContent = {
    val idx = _images.indexWhere(_.coords == coords)
    if (idx != -1) {
      val ret = _images.remove(idx)
      onRemoveImage(ret)
      ret
    } else null
  }

  def setImageSizeIfEmpty(size: Int): Boolean = {
    if (_images.isEmpty) {
      _imageSize = size
      true
    } else false
  }

  def listenToImagePool(pool: ImagePool): Unit =
    pool.trackChanges {
      case ImagePool.Event.ImageSaved(image) =>
        for
          im <- this._images
          if im.storage == image
        do im.setImageSaved()
      case ImagePool.Event.ImageReplaced(oldImage, newImage, _) =>
        for
          im <- this._images
          if im.storage == oldImage
        do im.replaceImage(newImage)
    }

  private def onAddImage(image: ImageContent): Unit = notifyListeners(_.onAddImage(image))

  private def onRemoveImage(image: ImageContent): Unit = notifyListeners(_.onRemoveImage(image))

  final def selectedImages: Seq[ImageContent] = images.filter(_.editable)
}
