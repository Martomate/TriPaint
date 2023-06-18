package com.martomate.tripaint.model.image

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.{GridCoords, TriangleCoords}
import com.martomate.tripaint.model.image.ImageStorage
import com.martomate.tripaint.util.{EventDispatcher, Tracker}
import scalafx.beans.property.{BooleanProperty, ReadOnlyBooleanProperty, ReadOnlyBooleanWrapper}

object GridCell:
  enum Event:
    case PixelChanged(coords: TriangleCoords, from: Color, to: Color)
    case ImageChangedALot

class GridCell(val coords: GridCoords, init_image: ImageStorage):
  private var _image: ImageStorage = init_image

  private val dispatcher = new EventDispatcher[GridCell.Event]
  def trackChanges(tracker: Tracker[GridCell.Event]): Unit = dispatcher.track(tracker)

  _image.trackChanges(this.onStorageChanged _)

  def storage: ImageStorage = _image

  val editableProperty: BooleanProperty = BooleanProperty(true)
  def editable: Boolean = editableProperty.value

  def onImageChangedALot(): Unit =
    dispatcher.notify(GridCell.Event.ImageChangedALot)

  private val _changed: ReadOnlyBooleanWrapper = ReadOnlyBooleanWrapper(false)
  def changed: Boolean = _changed.value
  def changedProperty: ReadOnlyBooleanProperty = _changed.readOnlyProperty

  def setImageSaved(): Unit = _changed.value = false

  def replaceImage(newImage: ImageStorage): Unit =
    _image = newImage
    _image.trackChanges(this.onStorageChanged _)
    dispatcher.notify(GridCell.Event.ImageChangedALot)

  private def onStorageChanged(event: ImageStorage.Event): Unit =
    event match
      case ImageStorage.Event.PixelChanged(coords, from, to) =>
        _changed.value = true
        dispatcher.notify(GridCell.Event.PixelChanged(coords, from, to))
