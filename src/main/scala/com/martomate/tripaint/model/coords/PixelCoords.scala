package com.martomate.tripaint.model.coords

case class PixelCoords(pix: TriangleCoords, image: TriImageCoords) {
  def neighbours(imageSize: Int): Seq[PixelCoords] = {// TODO: reach into neighboring images
    val (x, y) = (pix.x, pix.y)
    val localCoords = Seq(
      TriangleCoords(x - 1, y),
      if (x % 2 == 0) TriangleCoords(x + 1, y + 1) else TriangleCoords(x - 1, y - 1),
      TriangleCoords(x + 1, y)
    ).filter(t => t.x >= 0 && t.y >= 0 && t.x < 2 * t.y + 1 && t.y < imageSize)
    localCoords.map(c => PixelCoords(c, image))
  }
}
