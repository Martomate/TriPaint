package tripaint.model

import tripaint.Color
import tripaint.infrastructure.FileSystem
import tripaint.model.coords.{GridCoords, PixelCoords}
import tripaint.model.image.{
  GridCell,
  ImagePool,
  ImageSaveCollisionHandler,
  ImageStorage,
  RegularImage
}
import tripaint.model.image.ImagePool.{SaveInfo, SaveLocation}
import tripaint.util.{EventDispatcher, Tracker}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object ImageGrid {
  enum Event:
    case ImageAdded(image: GridCell)
    case ImageRemoved(image: GridCell)
    case PixelChanged(coords: PixelCoords, from: Color, to: Color)
    case ImageChangedALot(coords: GridCoords)

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

  private val imageTrackerRevokeFns = mutable.Map.empty[GridCoords, Tracker.RevokeFn]

  private val dispatcher = new EventDispatcher[ImageGrid.Event]
  def trackChanges(tracker: Tracker[ImageGrid.Event]): Unit = dispatcher.track(tracker)

  def apply(coords: GridCoords): Option[GridCell] = _images.find(_.coords == coords)

  def findByStorage(storage: ImageStorage): Option[GridCell] = images.find(_.storage == storage)

  def set(image: GridCell): Unit = {
    val idx = _images.indexWhere(_.coords == image.coords)
    if (idx != -1) {
      val prev = _images(idx)
      if (prev == image) {
        return
      }
      imageTrackerRevokeFns.remove(image.coords).foreach(_.apply())
      dispatcher.notify(ImageGrid.Event.ImageRemoved(prev))
      _images(idx) = image
    } else _images += image
    imageTrackerRevokeFns += image.coords -> image.trackChanges(e =>
      this.onGridCellEvent(image.coords, e)
    )
    dispatcher.notify(ImageGrid.Event.ImageAdded(image))
  }

  def -=(coords: GridCoords): GridCell = {
    val idx = _images.indexWhere(_.coords == coords)
    if (idx != -1) {
      val ret = _images.remove(idx)
      imageTrackerRevokeFns.remove(ret.coords).foreach(_.apply())
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

  def setImageSource(image: ImageStorage, location: SaveLocation, info: SaveInfo)(
      imagePool: ImagePool,
      imageSaveCollisionHandler: ImageSaveCollisionHandler
  ): Boolean =
    imagePool.imageAt(location) match
      case Some(currentImage) =>
        if currentImage == image then
          imagePool.set(image, location, info)
          true
        else
          imageSaveCollisionHandler.shouldReplaceImage(currentImage, image, location) match
            case Some(true) =>
              imagePool.set(image, location, info)
              this._images.find(_.storage == currentImage).foreach(_.replaceImage(image))
              true
            case Some(false) =>
              imagePool.remove(image)
              imagePool.set(currentImage, location, info)
              this._images.find(_.storage == image).foreach(_.replaceImage(currentImage))
              true
            case None =>
              false
      case None =>
        imagePool.set(image, location, info)
        true

  def save(
      image: ImageStorage,
      fileSystem: FileSystem,
      loc: SaveLocation,
      info: SaveInfo
  ): Boolean =
    val didWrite = doSave(image, fileSystem, loc, info)

    if didWrite then
      for
        im <- this._images
        if im.storage == image
      do im.setImageSaved()
    didWrite

  private def doSave(
      image: ImageStorage,
      fileSystem: FileSystem,
      loc: SaveLocation,
      info: SaveInfo
  ): Boolean =
    val oldImage = fileSystem.readImage(loc.file)

    val imageToSave = image.toRegularImage(info.format)
    val newImage = RegularImage.fromBaseAndOverlay(oldImage, imageToSave, loc.offset)

    fileSystem.writeImage(newImage, loc.file)

  def replaceImage(coords: GridCoords, newImage: ImageStorage): Unit =
    apply(coords).foreach(_.replaceImage(newImage))

  final def selectedImages: Seq[GridCell] = images.filter(_.editable)

  final def changedImages: Seq[GridCell] = images.filter(_.changed)

  private def onGridCellEvent(cell: GridCoords, event: GridCell.Event): Unit = {
    event match {
      case GridCell.Event.PixelChanged(pix, from, to) =>
        dispatcher.notify(ImageGrid.Event.PixelChanged(PixelCoords(cell, pix), from, to))
      case GridCell.Event.ImageChangedALot =>
        dispatcher.notify(ImageGrid.Event.ImageChangedALot(cell))
    }
  }
}
