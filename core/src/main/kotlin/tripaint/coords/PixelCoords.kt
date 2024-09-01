package tripaint.coords

data class PixelCoords(val value: Long) {
    val image: GridCoords
        get() = GridCoords((value ushr 32).toInt())
    val pix: TriangleCoords
        get() = TriangleCoords(value.toInt())

    fun neighbours(imageSize: Int): List<PixelCoords> {
        return toGlobal(imageSize).neighbours().map { c -> PixelCoords.from(c, imageSize) }
        /*    val dyForTop = if (image.x % 2 == 0) 1 else -1

        val (x, y) = (pix.x, pix.y)
        val localCoords = Seq(
          (x - 1, y),
          if (x % 2 == 0) (x + 1, y + 1) else (x - 1, y - 1),
          (x + 1, y)
        )
        localCoords.map { case c@(cx, cy) =>
          if (cx == -1) {
            PixelCoords(
              TriangleCoords(0, imageSize - 1 - cy),
              TriImageCoords(image.x - dyForTop, image.y)
            )
          } else if (cx == cy * 2 + 1) {
            val yy = imageSize - 1 - cy
            PixelCoords(
              TriangleCoords(2 * yy, yy),
              TriImageCoords(image.x + dyForTop, image.y)
            )
          } else if (cy == imageSize) {
            val xx = 2 * (imageSize - 1) - (cx - 1)
            PixelCoords(
              TriangleCoords(xx, imageSize - 1),
              TriImageCoords(image.x + dyForTop, image.y - dyForTop)
            )
          } else {
            PixelCoords(TriangleCoords(cx, cy), image)
          }
        }*/
    }

    fun toGlobal(imageSize: Int): GlobalPixCoords {
        val sz = imageSize
        return if (image.x % 2 == 0) {
            GlobalPixCoords.from(image.x * sz + pix.x, image.y * sz + sz - 1 - pix.y)
        } else {
            GlobalPixCoords.from(image.x * sz + sz - 1 - pix.x, image.y * sz + pix.y)
        }
    }

    companion object {
        fun from(coords: TriangleCoords, coords1: GridCoords): PixelCoords =
            PixelCoords((coords1.value.toLong() shl 32) or coords.value.toLong())

        fun from(coords: GlobalPixCoords, imageSize: Int): PixelCoords {
            require(imageSize > 0)

            return fromGlobalCoords(coords.x, coords.y, imageSize)
        }

        fun fromGlobalCoords(x: Int, y: Int, imageSize: Int): PixelCoords {
            val iy = Math.floorDiv(y, imageSize)
            val ix = Math.floorDiv(x, 2 * imageSize) * 2
            val py1 = imageSize - 1 - (y - iy * imageSize)
            val px1 = x - ix * imageSize
            val upsideDown = px1 > 2 * py1

            return if (upsideDown) {
                val image = GridCoords.from(ix + 1, iy)
                val pix = TriangleCoords.from(2 * imageSize - 1 - px1, imageSize - 1 - py1)
                PixelCoords((image.value.toLong() shl 32) or (pix.value.toLong() and 0xffffffffL))
            } else {
                val image = GridCoords.from(ix, iy)
                val pix = TriangleCoords.from(px1, py1)
                PixelCoords((image.value.toLong() shl 32) or (pix.value.toLong() and 0xffffffffL))
            }
        }
    }
}