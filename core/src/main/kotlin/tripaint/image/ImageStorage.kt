package tripaint.image

import tripaint.color.Color
import tripaint.coords.StorageCoords
import tripaint.coords.TriangleCoords
import tripaint.image.format.SimpleStorageFormat
import tripaint.image.format.StorageFormat
import tripaint.util.EventDispatcher
import tripaint.util.Tracker

class ImageStorage private constructor (val imageSize: Int, val image: RegularImage) {
    private val dispatcher = EventDispatcher<Event>()
    fun trackChanges(tracker: Tracker<Event>) {
        dispatcher.track(tracker)
    }

    fun contains(coords: TriangleCoords): Boolean = coords.y < imageSize

    fun getColor(coords: TriangleCoords): Color {
        val sc = SimpleStorageFormat.transform(coords)
        return image.getColor(sc.x, sc.y)
    }

    fun getColorArgb(coords: TriangleCoords): Int {
        val sc = SimpleStorageFormat.transform(coords)
        return image.getColorArgb(sc.x, sc.y)
    }

    fun setColor(coords: TriangleCoords, col: Color) {
        val before = this.getColor(coords)
        if (before != col) {
            val sc = SimpleStorageFormat.transform(coords)
            image.setColor(sc.x, sc.y, col)
            dispatcher.notify(Event.PixelChanged(coords, before, col))
        }
    }

    fun allPixels(): List<TriangleCoords> {
        val res = mutableListOf<TriangleCoords>()
        for (y in 0 until imageSize) {
            for (x in 0 until 2 * y + 1) {
                res += TriangleCoords.from(x, y)
            }
        }
        return res
    }

    fun toRegularImage(format: StorageFormat): RegularImage {
        require(format.supportsImageSize(imageSize))

        val image = RegularImage.ofSize(imageSize, imageSize)
        for (y in 0 until imageSize) {
            for (x in 0 until 2 * y + 1) {
                val tCoords = TriangleCoords.from(x, y)
                val sCoords = format.transform(tCoords)
                image.setColor(sCoords.x, sCoords.y, this.getColor(tCoords))
            }
        }
        return image
    }

    sealed interface Event {
        data class PixelChanged(val coords: TriangleCoords, val from: Color, val to: Color) : Event
    }

    companion object {
        fun fill(imageSize: Int, color: Color) = ImageStorage(imageSize, RegularImage.fill(imageSize, imageSize, color))

        fun fromRegularImage(
            image: RegularImage,
            offset: StorageCoords,
            format: StorageFormat,
            imageSize: Int
        ): Result<ImageStorage> {
            require(format.supportsImageSize(imageSize))

            return runCatching {
                val regularImage = RegularImage.tabulate(imageSize, imageSize) {
                    x, y ->
                    val stCoords = format.transform(SimpleStorageFormat.reverse(StorageCoords.from(x, y)))
                    image.getColor(offset.x + stCoords.x, offset.y + stCoords.y)
                }
                ImageStorage(imageSize, regularImage)
            }
        }

    }
}