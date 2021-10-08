package com.martomate.tripaint.model.coords

import org.scalactic.{Equality, TolerantNumerics}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GlobalPixCoordsTest extends AnyFlatSpec with Matchers {
  private def make(x: Int, y: Int): GlobalPixCoords = GlobalPixCoords(x, y)

  private val neighborDistSq = 1.0 / 3 // dist = sqrt(3) / 2 * (2.0 / 3)
  private val eps = 0.0001

  private implicit val doubleEq: Equality[Double] = TolerantNumerics.tolerantDoubleEquality(eps)

  "distanceSq" should "be 0 for the same coordinate" in {
    make(4, 5) distanceSq make(4, 5) shouldBe 0.0 +- eps
    GlobalPixCoords(11, -5) distanceSq make(11, -5) shouldBe 0.0 +- eps
  }

  it should "be 1 / 3 for a direct neighbor" in {
    make(4, 5) distanceSq make(5, 5) shouldBe neighborDistSq +- eps
    make(4, 5) distanceSq make(3, 5) shouldBe neighborDistSq +- eps
    make(4, 5) distanceSq make(5, 4) shouldBe neighborDistSq +- eps

    make(-3, 15) distanceSq make(-2, 15) shouldBe neighborDistSq +- eps
    make(-3, 15) distanceSq make(-4, 15) shouldBe neighborDistSq +- eps
    make(-3, 15) distanceSq make(-4, 16) shouldBe neighborDistSq +- eps
  }

  it should "be 1 for offset (+-2, 0)" in {
    make(4, 5) distanceSq make(6, 5) shouldBe 1.0 +- eps
    make(4, 5) distanceSq make(2, 5) shouldBe 1.0 +- eps

    make(-3, 15) distanceSq make(-1, 15) shouldBe 1.0 +- eps
    make(-3, 15) distanceSq make(-5, 15) shouldBe 1.0 +- eps
  }

  it should "be symmetric" in {
    for (i <- 1 to 10) {
      for (j <- 1 to 10) {
        val c1 = make(i * 13 % 29 - 17, j * 17 % 29 - 13)
        val c2 = make(j * 17 % 29 - 13, i * 13 % 29 - 17)
        (c1 distanceSq c2) shouldBe (c2 distanceSq c1)
      }
    }
  }
}
