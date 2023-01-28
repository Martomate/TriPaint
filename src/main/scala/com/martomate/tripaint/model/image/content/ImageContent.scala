package com.martomate.tripaint.model.image.content

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.{TriImageCoords, TriangleCoords}
import com.martomate.tripaint.model.image.SaveLocation
import com.martomate.tripaint.model.image.save.ImageSaverToFile
import com.martomate.tripaint.model.image.storage.{ImageStorage, ImageStorageListener}
import com.martomate.tripaint.model.undo.UndoManager
import com.martomate.tripaint.util.Listenable
import scalafx.beans.property.{BooleanProperty, ReadOnlyBooleanProperty, ReadOnlyBooleanWrapper}

object ImageContent:
  enum Event:
    case PixelChanged(coords: TriangleCoords, from: Color, to: Color)
    case ImageChangedALot

class ImageContent(val coords: TriImageCoords, init_image: ImageStorage)
    extends Listenable[ImageContent.Event => Unit]
    with ImageStorageListener:
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

  def tellListenersAboutBigChange(): Unit =
    import ImageContent.Event.*
    notifyListeners(_.apply(ImageChangedALot))

  def setImageSaved(): Unit = _changed.value = false

  def replaceImage(newImage: ImageStorage): Unit =
    import ImageContent.Event.*

    _image.removeListener(this)
    _image = newImage
    _image.addListener(this)
    notifyListeners(_.apply(ImageChangedALot))

  def onPixelChanged(coords: TriangleCoords, from: Color, to: Color): Unit =
    import ImageContent.Event.*

    _changed.value = true
    notifyListeners(_.apply(PixelChanged(coords, from, to)))
