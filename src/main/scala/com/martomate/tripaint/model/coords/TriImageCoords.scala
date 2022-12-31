package com.martomate.tripaint.model.coords

case class TriImageCoords(x: Int, y: Int):
  private val vertices: Seq[(Double, Double)] =
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
