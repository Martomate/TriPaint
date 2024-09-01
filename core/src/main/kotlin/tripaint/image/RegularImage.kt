package tripaint.image

import tripaint.color.Color
import tripaint.coords.StorageCoords
import java.awt.image.BufferedImage

class RegularImage private constructor (val width: Int, val height: Int, private val pixels: IntArray) {
    fun getColor(x: Int, y: Int): Color = Color.fromInt(getColorArgb(x, y))

    fun getColorArgb(x: Int, y: Int): Int {
        require(x >= 0)
        require(y >= 0)
        require(x < width)
        require(y < height)

        return pixels[x + y * width]
    }

    fun setColor(x: Int, y: Int, color: Color) = setColorArgb(x, y, color.toInt())

    fun setColorArgb(x: Int, y: Int, color: Int) {
        require(x >= 0)
        require(y >= 0)
        require(x < width)
        require(y < height)

        pixels[x + y * width] = color
    }

    fun pasteImage(offset: StorageCoords, image: RegularImage) {
        require(offset.x + image.width <= width)
        require(offset.y + image.height <= height)

        for (dy in 0 until image.height) {
            for (dx in 0 until image.width) {
                pixels[offset.x + dx + (offset.y + dy) * width] = image.pixels[dx + dy * image.width]
            }
        }
    }

    fun toBufferedImage(): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        image.setRGB(0, 0, width, height, pixels, 0, width)
        return image
    }

    fun toIntArray(): IntArray = pixels.clone()

    override fun equals(other: Any?): Boolean {
        return if (other is RegularImage) {
            if (width == other.width && height == other.height) {
                pixels.contentEquals(other.pixels)
            } else false
        } else false
    }

    override fun toString(): String {
        return pixels
            .asIterable()
            .chunked(width)
            .map { row -> row.map {pix -> Integer.toHexString(pix) }.joinToString(", ") }
            .joinToString(prefix = "RegularImage(\n", separator = "\n", postfix = "\n)")
    }

    companion object {
        fun ofSize(width: Int, height: Int): RegularImage = RegularImage(width, height, IntArray(width * height))

        fun fromBufferedImage(image: BufferedImage): RegularImage {
            val w = image.width
            val h = image.height
            return RegularImage (w, h, image.getRGB(0, 0, w, h, null, 0, w))
        }

        fun fill(width: Int, height: Int, color: Color): RegularImage {
            val image = ofSize(width, height)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    image.setColor(x, y, color)
                }
            }
            return image
        }

        fun tabulate(width: Int, height: Int, fn: (x: Int, y: Int) -> Color): RegularImage {
            val image = ofSize(width, height)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    image.setColor(x, y, fn(x, y))
                }
            }
            return image
        }

        fun fromBaseAndOverlay(
            baseImage: RegularImage?,
            overlayImage: RegularImage,
            overlayOffset: StorageCoords
        ): RegularImage {
            val minimumWidth = overlayOffset.x + overlayImage.width
            val minimumHeight = overlayOffset.y + overlayImage.height

            val finalWidth = baseImage?.width?.coerceAtLeast(minimumWidth) ?: minimumWidth
            val finalHeight = baseImage?.height?.coerceAtLeast(minimumHeight) ?: minimumHeight

            val finalImage = ofSize(finalWidth, finalHeight)
            if (baseImage != null) {
                finalImage.pasteImage(StorageCoords.from(0, 0), baseImage)
            }
            finalImage.pasteImage(overlayOffset, overlayImage)
            return finalImage
        }
    }

}