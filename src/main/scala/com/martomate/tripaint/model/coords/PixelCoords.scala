package com.martomate.tripaint.model.coords

case class PixelCoords(image: TriImageCoords, pix: TriangleCoords) {
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
      GlobalPixCoords(image.x * sz + pix.x, image.y * sz + sz-1 - pix.y)
    } else {
      GlobalPixCoords(image.x * sz + sz-1 - pix.x, image.y * sz + pix.y)
    }
  }
}

object PixelCoords {
  def apply(coords: TriangleCoords, coords1: TriImageCoords): PixelCoords =
    PixelCoords(coords1, coords)

  def apply(coords: GlobalPixCoords, imageSize: Int): PixelCoords = {
    val iy = Math.floorDiv(coords.y, imageSize)
    val ix = Math.floorDiv(coords.x, 2 * imageSize) * 2
    val py1 = imageSize - 1 - (coords.y - iy * imageSize)
    val px1 = coords.x - ix * imageSize
    val upsideDown = px1 > 2 * py1
    val py = if (upsideDown) imageSize - 1 - py1 else py1
    val px = if (upsideDown) 2 * imageSize - 1 - px1 else px1
    val image = TriImageCoords(if (upsideDown) ix + 1 else ix, iy)
    val pix = TriangleCoords(px, py)
    PixelCoords(pix, image)
  }
}
