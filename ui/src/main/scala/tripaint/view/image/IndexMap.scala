package tripaint.view.image

import tripaint.coords.TriangleCoords

class IndexMap(imageSize: Int) {
  def coordsAt(x: Double, y: Double): Option[TriangleCoords] = {
    val yy = y
    val xx = x - (1 - y) / 2

    if yy >= 0 && yy < 1 && xx >= 0 && xx < 1 then {
      val yInt = (yy * imageSize).toInt
      val xInt = (xx * imageSize).toInt

      val xT =
        if yy - yInt.toDouble / imageSize < xx - xInt.toDouble / imageSize
        then xInt * 2 + 1
        else xInt * 2

      if (yInt < 0 || yInt >= imageSize || xT < 0 || xT > yInt * 2) None
      else Some(TriangleCoords(xT, yInt))
    } else None
  }
}
