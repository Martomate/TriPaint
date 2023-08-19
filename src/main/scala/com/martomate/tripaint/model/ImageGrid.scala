package com.martomate.tripaint.model

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model.coords.GridCoords
import com.martomate.tripaint.model.image.{GridCell, ImagePool, ImageStorage, RegularImage}
import com.martomate.tripaint.model.image.ImagePool.{SaveInfo, SaveLocation}
import com.martomate.tripaint.util.{EventDispatcher, Tracker}

import scala.collection.mutable.ArrayBuffer

object ImageGrid {
  enum Event:
    case ImageAdded(image: GridCell)
    case ImageRemoved(image: GridCell)

  def fromCells(imageSize: Int, cells: Seq[GridCell]): ImageGrid =
    val grid = new ImageGrid(imageSize)
    for c <- cells do grid.set(c)
    grid
}

class ImageGrid(init_imageSize: Int) {
  private var _imageSize: Int = init_imageSize
  def imageSize: Int = _imageSize

  private val _images: ArrayBuffer[GridCell] = ArrayBuffer.empty
  def images: Seq[GridCell] = _images.toSeq

  private val dispatcher = new EventDispatcher[ImageGrid.Event]
  def trackChanges(tracker: Tracker[ImageGrid.Event]): Unit = dispatcher.track(tracker)

  def apply(coords: GridCoords): Option[GridCell] = _images.find(_.coords == coords)

  def findByStorage(storage: ImageStorage): Option[GridCell] = images.find(_.storage == storage)

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

  private val undoManager = new UndoManager

  def performChange(change: ImageGridChange): Unit =
    change.redo()
    undoManager.append(change)
    for im <- images do im.onImageChangedALot()

  def undo(): Unit =
    undoManager.undo()
    for im <- images do im.onImageChangedALot()

  def redo(): Unit =
    undoManager.redo()
    for im <- images do im.onImageChangedALot()

  def setImageSizeIfEmpty(size: Int): Boolean = {
    if (_images.isEmpty) {
      _imageSize = size
      true
    } else false
  }

  def save(image: ImageStorage, fileSystem: FileSystem, loc: SaveLocation, info: SaveInfo): Boolean =
      val didWrite = doSave(image, fileSystem, loc, info)

      if didWrite then
        for
          im <- this._images
          if im.storage == image
        do im.setImageSaved()
      didWrite

  private def doSave(image: ImageStorage, fileSystem: FileSystem, loc: SaveLocation, info: SaveInfo): Boolean =
    val oldImage = fileSystem.readImage(loc.file)

    val imageToSave = image.toRegularImage(info.format)
    val newImage = RegularImage.fromBaseAndOverlay(oldImage, imageToSave, loc.offset)

    fileSystem.writeImage(newImage, loc.file)

  def listenToImagePool(pool: ImagePool): Unit =
    pool.trackChanges:
      case ImagePool.Event.ImageReplaced(oldImage, newImage, _) =>
        for
          im <- this._images
          if im.storage == oldImage
        do im.replaceImage(newImage)

  def replaceImage(coords: GridCoords, newImage: ImageStorage): Unit =
    apply(coords).foreach(_.replaceImage(newImage))

  final def selectedImages: Seq[GridCell] = images.filter(_.editable)

  final def changedImages: Seq[GridCell] = images.filter(_.changed)
}
