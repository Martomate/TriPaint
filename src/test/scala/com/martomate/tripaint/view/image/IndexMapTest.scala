package com.martomate.tripaint.view.image

import com.martomate.tripaint.model.coords.TriangleCoords
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class IndexMapTest extends AnyFlatSpec with Matchers {

  def make(imageSize: Int): IndexMap = {
    new IndexMap(imageSize)
  }

  "coordsAt" should "return null outside of the image" in {
    val indexMap = make(12)

    indexMap.coordsAt(10, 0) shouldBe null
    indexMap.coordsAt(-10, 0) shouldBe null
    indexMap.coordsAt(0, 10) shouldBe null
    indexMap.coordsAt(0, -10) shouldBe null

    indexMap.coordsAt(0.49, 0) shouldBe null
    indexMap.coordsAt(0.51, 0) shouldBe null

    indexMap.coordsAt(0, 1 / 80.0) shouldBe null
    indexMap.coordsAt(1, 1 / 80.0) shouldBe null
  }

  it should "be correct for size 2" in {
    val indexMap = make(2)

    indexMap.coordsAt(0.50, 79 / 80.0) shouldBe TriangleCoords(1, 1)
    indexMap.coordsAt(0.49, 79 / 80.0) shouldBe TriangleCoords(0, 1)
    indexMap.coordsAt(0.51, 79 / 80.0) shouldBe TriangleCoords(2, 1)

    indexMap.coordsAt(0.2475, 39 / 80.0) shouldBe null
    indexMap.coordsAt(0.26, 39 / 80.0) shouldBe TriangleCoords(0, 0)
    indexMap.coordsAt(0.50, 39 / 80.0) shouldBe TriangleCoords(0, 0)
    indexMap.coordsAt(0.74, 39 / 80.0) shouldBe TriangleCoords(0, 0)
    indexMap.coordsAt(0.7525, 39 / 80.0) shouldBe null

    indexMap.coordsAt(0.2475, 41 / 80.0) shouldBe TriangleCoords(0, 1)
    indexMap.coordsAt(0.26, 41 / 80.0) shouldBe TriangleCoords(1, 1)
    indexMap.coordsAt(0.50, 41 / 80.0) shouldBe TriangleCoords(1, 1)
    indexMap.coordsAt(0.74, 41 / 80.0) shouldBe TriangleCoords(1, 1)
    indexMap.coordsAt(0.7525, 41 / 80.0) shouldBe TriangleCoords(2, 1)
  }

  it should "return correct y levels" in {
    val indexMap = make(20)

    for (y <- 0 until 20) {
      val steps = 23
      val stepSize = 0.05 / steps
      for (d <- 1 until steps) {
        val yReal = y * 0.05 + d * stepSize
        (y, d, indexMap.coordsAt(0.500, yReal)) shouldBe (y, d, TriangleCoords(y, y))
        if (y > 10) {
          (y, d, indexMap.coordsAt(0.250, yReal)) shouldBe (y, d, TriangleCoords(y-10, y))
          (y, d, indexMap.coordsAt(0.750, yReal)) shouldBe (y, d, TriangleCoords(y+10, y))
        }
      }
    }
  }

  it should "return correct x values" in {
    val indexMap = make(20)

    for (x <- 0 until 20) {
      val steps = 23
      val stepSize = 0.05 / steps
      for (d <- 1 until steps) {
        val xReal = x * 0.05 + d * stepSize
        (x, d, indexMap.coordsAt(xReal, 0.999999)) shouldBe (x, d, TriangleCoords(2 * x, 19))
      }
    }
  }
}
