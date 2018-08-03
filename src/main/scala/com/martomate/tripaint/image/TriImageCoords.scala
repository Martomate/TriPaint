package com.martomate.tripaint.image

case class TriImageCoords(x: Int, y: Int) {
  val vertices: Seq[(Double, Double)] = {
    val pts = if (x % 2 == 0) Seq(
      (x / 2    , y),
      (x / 2 + 1, y),
      (x / 2    , y + 1)
    ) else Seq(
      (x / 2 + 1, y + 1),
      (x / 2    , y + 1),
      (x / 2 + 1, y)
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
