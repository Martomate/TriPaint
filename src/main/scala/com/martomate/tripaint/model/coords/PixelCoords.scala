package com.martomate.tripaint.model.coords

case class PixelCoords(pix: TriangleCoords, image: TriImageCoords) {
  def neighbours(imageSize: Int): Seq[PixelCoords] = {
    val dyForTop = if (image.x % 2 == 0) 1 else -1

    val (x, y) = (pix.x, pix.y)
    val localCoords = Seq(
      TriangleCoords(x - 1, y),
      if (x % 2 == 0) TriangleCoords(x + 1, y + 1) else TriangleCoords(x - 1, y - 1),
      TriangleCoords(x + 1, y)
    )
    localCoords.map { c =>
      if (c.x == -1) {
        PixelCoords(
          TriangleCoords(0, imageSize - 1 - c.y),
          TriImageCoords(image.x - dyForTop, image.y)
        )
      } else if (c.x == c.y * 2 + 1) {
        val yy = imageSize - 1 - c.y
        PixelCoords(
          TriangleCoords(2 * yy, yy),
          TriImageCoords(image.x + dyForTop, image.y)
        )
      } else if (c.y == imageSize) {
        val xx = 2 * (imageSize - 1) - (c.x - 1)
        PixelCoords(
          TriangleCoords(xx, imageSize - 1),
          TriImageCoords(image.x + dyForTop, image.y - dyForTop)
        )
      } else {
        PixelCoords(c, image)
      }
    }
  }
}
