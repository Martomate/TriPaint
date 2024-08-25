package tripaint.coords

case class GridCoords(value: Int) extends AnyVal {
  inline def x: Int = value >> 16
  inline def y: Int = value << 16 >> 16

  def center: (Double, Double) = {
    val vertices: Seq[(Double, Double)] =
      val xDiv2 = (x.toDouble / 2).floor
      val pts =
        if x % 2 == 0 then
          Seq(
            (xDiv2, y),
            (xDiv2 + 1, y),
            (xDiv2, y + 1)
          )
        else
          Seq(
            (xDiv2 + 1, y + 1),
            (xDiv2, y + 1),
            (xDiv2 + 1, y)
          )
      for (xx, yy) <- pts yield (xx + yy * 0.5, -yy * Math.sqrt(3) / 2)

    val centerX: Double = vertices.map(_._1).sum / 3
    val centerY: Double = vertices.map(_._2).sum / 3

    (centerX, centerY)
  }
}

object GridCoords {
  inline def apply(x: Int, y: Int): GridCoords = {
    new GridCoords(x << 16 | (y & 0xffff))
  }
}
