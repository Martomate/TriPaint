package com.martomate.tripaint.model.coords

/** Like TriImageCoords but for pixels (on the entire area) */
case class GlobalPixCoords(x: Int, y: Int) {
  def distanceSq(other: GlobalPixCoords): Double = {
    val dx = other.x - x
    val dy = other.y - y
    val otherY = other.y + (if (other.x % 2 == 0) 1.0 / 3 else 2.0 / 3)
    val thisY = y + (if (x % 2 == 0) 1.0 / 3 else 2.0 / 3)
    val actualDy = otherY - thisY
    val xx = dx * 0.5 + dy * 0.5
    // It's like magic!
    val yy = actualDy * GlobalPixCoords.sqrt3 / 2
    xx * xx + yy * yy
  }

  def neighbours: Seq[GlobalPixCoords] = {
    Seq(
      (x - 1, y),
      if (x % 2 == 0) (x + 1, y - 1) else (x - 1, y + 1),
      (x + 1, y)
    ) map (c => GlobalPixCoords(c._1, c._2))
  }
}

object GlobalPixCoords {
  private val sqrt3: Double = math.sqrt(3)
}
