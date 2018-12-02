package com.martomate.tripaint.model.coords

case class TriImageCoords(x: Int, y: Int) {
  val vertices: Seq[(Double, Double)] = {
    val xDiv2 = (x.toDouble / 2).floor
    val pts = if (x % 2 == 0) Seq(
      (xDiv2    , y),
      (xDiv2 + 1, y),
      (xDiv2    , y + 1)
    ) else Seq(
      (xDiv2 + 1, y + 1),
      (xDiv2    , y + 1),
      (xDiv2 + 1, y)
    )
    pts map {
      case (xx, yy) => (xx + yy * 0.5, -yy * Math.sqrt(3) / 2)
    }
  }

  val centroid: (Double, Double) = {
    val sum: (Double, Double) = vertices.fold((0d, 0d))((t1, t2) => (t1._1 + t2._1, t1._2 + t2._2))
    (sum._1 / 3, sum._2 / 3)
  }

  def xOff: Double = centroid._1
  def yOff: Double = centroid._2
}
