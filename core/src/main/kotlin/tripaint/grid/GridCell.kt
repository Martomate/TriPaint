package tripaint.grid

import tripaint.color.Color
import tripaint.coords.GridCoords
import tripaint.coords.TriangleCoords
import tripaint.image.ImageStorage
import tripaint.util.EventDispatcher
import tripaint.util.RevokeFn
import tripaint.util.Tracker

class GridCell(val coords: GridCoords, init_image: ImageStorage) {
    private var _image: ImageStorage = init_image

    init {
        _image.trackChanges { this.onStorageChanged(it) }
    }

    private val dispatcher = EventDispatcher<Event>()
    fun trackChanges(tracker: Tracker<Event>): RevokeFn = dispatcher.track(tracker)

    val storage: ImageStorage
        get() = _image

    private var _editable = true
    var editable: Boolean
        get() = _editable
        set(value) {
            _editable = value
            dispatcher.notify(Event.StateUpdated(value, changed))
        }

    fun onImageChangedALot(): Unit = dispatcher.notify(Event.ImageChangedALot)

    private var _changed = false
    val changed: Boolean
        get() = _changed

    fun setImageSaved() {
        if (_changed) {
            _changed = false
            dispatcher.notify(Event.StateUpdated(editable, changed))
        }
    }

    fun replaceImage(newImage: ImageStorage) {
        _image = newImage
        _image.trackChanges { this.onStorageChanged(it) }
        dispatcher.notify(Event.ImageChangedALot)
    }

    private fun onStorageChanged(event: ImageStorage.Event) {
        when (event) {
            is ImageStorage.Event.PixelChanged -> {
                val (coords, from, to) = event
                if (!_changed) {
                    _changed = true
                    dispatcher.notify(Event.StateUpdated(editable, changed))
                }
                dispatcher.notify(Event.PixelChanged(coords, from, to))
            }
        }
    }

    sealed interface Event {
        data class PixelChanged(val coords: TriangleCoords, val from: Color, val to: Color) : Event
        data object ImageChangedALot : Event
        data class StateUpdated(val editable: Boolean, val changed: Boolean) : Event
    }
}
