package tripaint.grid

import tripaint.color.Color
import tripaint.coords.GridCoords
import tripaint.coords.PixelCoords
import tripaint.image.ImagePool
import tripaint.image.ImageStorage
import tripaint.util.EventDispatcher
import tripaint.util.RevokeFn
import tripaint.util.Tracker

class ImageGrid(init_imageSize: Int) {
    private var _imageSize: Int = init_imageSize
    val imageSize: Int
        get() = _imageSize

    private val _images: MutableList<GridCell> = mutableListOf()
    fun images(): List<GridCell> = _images.toList()

    private val imageTrackerRevokeFns: MutableMap<GridCoords, RevokeFn> = mutableMapOf()

    private val dispatcher = EventDispatcher<Event>()
    fun trackChanges(tracker: Tracker<Event>) {
        dispatcher.track(tracker)
    }

    // Note: this function is optimized for speed
    fun apply(coords: GridCoords): GridCell? {
        var res: GridCell? = null
        var idx = 0
        val len = _images.size
        while (idx < len) {
            val img = _images[idx]
            if (img.coords == coords) {
                res = img
                idx = len
            }
            idx += 1
        }
        return res
    }

    fun findByStorage(storage: ImageStorage): GridCell? = _images.find { it.storage == storage }

    fun set(image: GridCell) {
        val idx = _images.indexOfFirst { it.coords == image.coords }
        if (idx != -1) {
            val prev = _images[idx]
            if (prev == image) {
                return
            }
            imageTrackerRevokeFns.remove(image.coords)?.invoke()
            dispatcher.notify(Event.ImageRemoved(prev))
            _images[idx] = image
        } else {
            _images += image
        }
        imageTrackerRevokeFns[image.coords] = image.trackChanges { e ->
            this.onGridCellEvent(image.coords, e)
        }
        dispatcher.notify(Event.ImageAdded(image))
    }

    fun remove(coords: GridCoords): GridCell? {
        val idx = _images.indexOfFirst { it.coords == coords }
        return if (idx != -1) {
            val ret = _images.removeAt(idx)
            imageTrackerRevokeFns.remove(ret.coords)?.invoke()
            dispatcher.notify(Event.ImageRemoved(ret))
            ret
        } else {
            null
        }
    }

    private val undoManager = UndoManager()

    fun performChange(change: ImageGridChange) {
        change.redo()
        undoManager.append(change)
        for (im in _images) {
            im.onImageChangedALot()
        }
    }

    fun undo() {
        undoManager.undo()
        for (im in _images) {
            im.onImageChangedALot()
        }
    }

    fun redo() {
        undoManager.redo()
        for (im in _images) {
            im.onImageChangedALot()
        }
    }

    fun setImageSizeIfEmpty(size: Int): Boolean {
        return if (_images.isEmpty()) {
            _imageSize = size
            true
        } else {
            false
        }
    }

    fun setImageSource(image: ImageStorage, location: ImagePool.SaveLocation, info: ImagePool.SaveInfo,
        imagePool: ImagePool,
        imageSaveCollisionHandler: ImageSaveCollisionHandler
    ): Boolean {
        val currentImage = imagePool.imageAt(location)
        if (currentImage == null) {
            imagePool.set(image, location, info)
            return true
        }
        if (currentImage == image) {
            imagePool.set(image, location, info)
            return true
        }
        val replace = imageSaveCollisionHandler.shouldReplaceImage(currentImage, image, location) ?: return false
        if (replace) { // replace old image with new image
            imagePool.set(image, location, info)
            this._images.find { it.storage == currentImage }?.replaceImage(image)
        } else { // keep current image
            imagePool.remove(image)
            imagePool.set(currentImage, location, info)
            this._images.find { it.storage == image }?.replaceImage(currentImage)
        }
        return true
    }

    fun replaceImage(coords: GridCoords, newImage: ImageStorage) {
        apply(coords)?.replaceImage(newImage)
    }

    fun selectedImages(): List<GridCell> = _images.filter { it.editable }

    fun changedImages(): List<GridCell> = _images.filter { it.changed }

    private fun onGridCellEvent(cell: GridCoords, event: GridCell.Event) {
        when (event) {
            is GridCell.Event.PixelChanged -> {
                val (pix, from, to) = event
                dispatcher.notify(Event.PixelChanged(PixelCoords.from(pix, cell), from, to))
            }
            is GridCell.Event.ImageChangedALot -> {
                dispatcher.notify(Event.ImageChangedALot(cell))
            }
            else -> {}
        }
    }

    sealed interface Event {
        data class ImageAdded(val image: GridCell) : Event
        data class ImageRemoved(val image: GridCell) : Event
        data class PixelChanged(val coords: PixelCoords, val from: Color, val to: Color) : Event
        data class ImageChangedALot(val coords: GridCoords) : Event
    }

    companion object {
        fun fromCells(imageSize: Int, cells: List<GridCell>): ImageGrid {
            val grid = ImageGrid(imageSize)
            for (c in cells) {
                grid.set(c)
            }
            return grid
        }
    }
}
