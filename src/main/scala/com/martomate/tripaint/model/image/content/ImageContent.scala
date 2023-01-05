package com.martomate.tripaint.model.image.content

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.{TriImageCoords, TriangleCoords}
import com.martomate.tripaint.model.image.SaveLocation
import com.martomate.tripaint.model.image.pool.ImagePoolListener
import com.martomate.tripaint.model.image.save.ImageSaverToFile
import com.martomate.tripaint.model.image.storage.{ImageStorage, ImageStorageListener}
import com.martomate.tripaint.model.undo.UndoManager
import com.martomate.tripaint.util.Listenable
import scalafx.beans.property.{BooleanProperty, ReadOnlyBooleanProperty, ReadOnlyBooleanWrapper}

class ImageContent(val coords: TriImageCoords, init_image: ImageStorage)
    extends Listenable[ImageChangeListener]
    with ImagePoolListener
    with ImageStorageListener {
  private var _image: ImageStorage = init_image

  _image.addListener(this)

  def storage: ImageStorage = _image

  val editableProperty: BooleanProperty = BooleanProperty(true)
  def editable: Boolean = editableProperty.value

  val undoManager = new UndoManager
  def undo(): Unit = undoManager.undo()
  def redo(): Unit = undoManager.redo()

  private val _changed: ReadOnlyBooleanWrapper = ReadOnlyBooleanWrapper(false)
  def changed: Boolean = _changed.value
  def changedProperty: ReadOnlyBooleanProperty = _changed.readOnlyProperty

  def tellListenersAboutBigChange(): Unit = notifyListeners(_.onImageChangedALot())

  def onImageSaved(image: ImageStorage): Unit = {
    if (image == _image) {
      _changed.value = false
    }
  }

  def onImageReplaced(
      oldImage: ImageStorage,
      newImage: ImageStorage,
      location: SaveLocation
  ): Unit = {
    if (oldImage == _image) {
      _image.removeListener(this)
      _image = newImage
      _image.addListener(this)
      notifyListeners(_.onImageChangedALot())
    }
  }

  def onPixelChanged(coords: TriangleCoords, from: Color, to: Color): Unit = {
    _changed.value = true
    notifyListeners(_.onPixelChanged(coords, from, to))
  }
}
