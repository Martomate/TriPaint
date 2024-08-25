package tripaint.grid

import tripaint.color.Color
import tripaint.coords.{GridCoords, TriangleCoords}
import tripaint.image.ImageStorage
import tripaint.util.{EventDispatcher, Tracker}

object GridCell {
  enum Event {
    case PixelChanged(coords: TriangleCoords, from: Color, to: Color)
    case ImageChangedALot
    case StateUpdated(editable: Boolean, changed: Boolean)
  }
}

class GridCell(val coords: GridCoords, init_image: ImageStorage):
  private var _image: ImageStorage = init_image

  private val dispatcher = new EventDispatcher[GridCell.Event]
  def trackChanges(tracker: Tracker[GridCell.Event]): Tracker.RevokeFn = dispatcher.track(tracker)

  _image.trackChanges(this.onStorageChanged(_))

  def storage: ImageStorage = _image

  private var _editable = true
  def editable: Boolean = _editable
  def setEditable(editable: Boolean): Unit = {
    _editable = editable
    dispatcher.notify(GridCell.Event.StateUpdated(editable, changed))
  }

  def onImageChangedALot(): Unit =
    dispatcher.notify(GridCell.Event.ImageChangedALot)

  private var _changed = false
  def changed: Boolean = _changed

  def setImageSaved(): Unit = {
    if _changed then {
      _changed = false
      dispatcher.notify(GridCell.Event.StateUpdated(editable, changed))
    }
  }

  def replaceImage(newImage: ImageStorage): Unit =
    _image = newImage
    _image.trackChanges(this.onStorageChanged(_))
    dispatcher.notify(GridCell.Event.ImageChangedALot)

  private def onStorageChanged(event: ImageStorage.Event): Unit =
    event match
      case ImageStorage.Event.PixelChanged(coords, from, to) =>
        if !_changed then {
          _changed = true
          dispatcher.notify(GridCell.Event.StateUpdated(editable, changed))
        }
        dispatcher.notify(GridCell.Event.PixelChanged(coords, from, to))
