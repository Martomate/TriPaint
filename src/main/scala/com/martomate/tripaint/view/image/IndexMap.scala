package com.martomate.tripaint.view.image

import com.martomate.tripaint.model.coords.TriangleCoords

class IndexMap(imageSize: Int) extends IndexMapper {
  def coordsAt(x: Double, y: Double): TriangleCoords = {
    val yy = y
    val xx = x - (1 - yy) / 2

    if (yy >= 0 && yy < 1 && xx >= 0 && xx < 1) {
      val yInt = (yy * imageSize).toInt
      val xInt = (xx * imageSize).toInt

      val xT =
        if (yy - yInt.toDouble / imageSize < xx - xInt.toDouble / imageSize)
          xInt * 2 + 1
        else
          xInt * 2

      if (yInt < 0 || yInt >= imageSize || xT < 0 || xT > yInt * 2) null
      else TriangleCoords(xT, yInt)
    } else null
  }

}
