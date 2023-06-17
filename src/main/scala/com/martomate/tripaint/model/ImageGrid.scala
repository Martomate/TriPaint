package com.martomate.tripaint.model

import com.martomate.tripaint.model.coords.GridCoords
import com.martomate.tripaint.model.image.ImagePool
import com.martomate.tripaint.model.image.content.GridCell
import com.martomate.tripaint.util.{EventDispatcher, Tracker}

import scala.collection.mutable.ArrayBuffer

object ImageGrid {
  enum Event:
    case ImageAdded(image: GridCell)
    case ImageRemoved(image: GridCell)
}

class ImageGrid(init_imageSize: Int) {
  private var _imageSize: Int = init_imageSize
  def imageSize: Int = _imageSize

  private val _images: ArrayBuffer[GridCell] = ArrayBuffer.empty
  def images: Seq[GridCell] = _images.toSeq

  private val dispatcher = new EventDispatcher[ImageGrid.Event]
  def trackChanges(tracker: Tracker[ImageGrid.Event]): Unit = dispatcher.track(tracker)

  def apply(coords: GridCoords): Option[GridCell] = _images.find(_.coords == coords)

  def set(image: GridCell): Unit = {
    val idx = _images.indexWhere(_.coords == image.coords)
    if (idx != -1) {
      val prev = _images(idx)
      if (prev != image) dispatcher.notify(ImageGrid.Event.ImageRemoved(prev))
      _images(idx) = image
    } else _images += image
    dispatcher.notify(ImageGrid.Event.ImageAdded(image))
  }

  def -=(coords: GridCoords): GridCell = {
    val idx = _images.indexWhere(_.coords == coords)
    if (idx != -1) {
      val ret = _images.remove(idx)
      dispatcher.notify(ImageGrid.Event.ImageRemoved(ret))
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

  final def selectedImages: Seq[GridCell] = images.filter(_.editable)
}
