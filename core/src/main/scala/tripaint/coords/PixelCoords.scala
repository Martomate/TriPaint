package tripaint.coords

case class PixelCoords(value: Long) extends AnyVal {
  inline def image: GridCoords = new GridCoords((value >> 32).toInt)
  inline def pix: TriangleCoords = new TriangleCoords(value.toInt)

  def neighbours(imageSize: Int): Seq[PixelCoords] = {
    toGlobal(imageSize).neighbours map (c => PixelCoords(c, imageSize))
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

  def toGlobal(imageSize: Int): GlobalPixCoords = {
    val sz = imageSize
    if (image.x % 2 == 0) {
      GlobalPixCoords(image.x * sz + pix.x, image.y * sz + sz - 1 - pix.y)
    } else {
      GlobalPixCoords(image.x * sz + sz - 1 - pix.x, image.y * sz + pix.y)
    }
  }
}

object PixelCoords {
  def apply(coords: TriangleCoords, coords1: GridCoords): PixelCoords =
    new PixelCoords(coords1.value.toLong << 32 | coords.value.toLong)

  def apply(coords: GlobalPixCoords, imageSize: Int): PixelCoords = {
    require(imageSize > 0)

    fromGlobalCoords(coords.x, coords.y, imageSize)
  }

  inline def fromGlobalCoords(x: Int, y: Int, imageSize: Int): PixelCoords = {
    val iy = Math.floorDiv(y, imageSize)
    val ix = Math.floorDiv(x, 2 * imageSize) * 2
    val py1 = imageSize - 1 - (y - iy * imageSize)
    val px1 = x - ix * imageSize
    val upsideDown = px1 > 2 * py1

    if upsideDown then {
      val image = GridCoords(ix + 1, iy)
      val pix = TriangleCoords(2 * imageSize - 1 - px1, imageSize - 1 - py1)
      new PixelCoords(image.value.toLong << 32 | (pix.value.toLong & 0xffffffffL))
    } else {
      val image = GridCoords(ix, iy)
      val pix = TriangleCoords(px1, py1)
      new PixelCoords(image.value.toLong << 32 | (pix.value.toLong & 0xffffffffL))
    }
  }
}
