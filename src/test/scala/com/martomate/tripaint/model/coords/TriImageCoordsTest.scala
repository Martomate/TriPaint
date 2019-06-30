package com.martomate.tripaint.model.coords

import org.scalatest.{FlatSpec, Matchers}

class TriImageCoordsTest extends FlatSpec with Matchers {
  private val epsilon: Double = 1e-6
  private val unitHeight: Double = math.sqrt(3) / 2
  private val aThirdHeight: Double = unitHeight / 3

  "(xOff, yOff)" should "be the mass center of the triangle" in {
    TriImageCoords( 0, 0).xOff shouldBe            0.5 +- epsilon
    TriImageCoords( 0, 3).xOff shouldBe  3 * 0.5 + 0.5 +- epsilon
    TriImageCoords( 0,-3).xOff shouldBe -3 * 0.5 + 0.5 +- epsilon

    TriImageCoords( 1, 0).xOff shouldBe            1.0 +- epsilon
    TriImageCoords( 7, 3).xOff shouldBe  3 * 0.5 +   4 +- epsilon
    TriImageCoords( 7,-3).xOff shouldBe -3 * 0.5 +   4 +- epsilon
    TriImageCoords(-7, 3).xOff shouldBe  3 * 0.5 +  -3 +- epsilon
    TriImageCoords(-7,-3).xOff shouldBe -3 * 0.5 +  -3 +- epsilon

    -TriImageCoords( 0, 0).yOff shouldBe                       aThirdHeight +- epsilon
    -TriImageCoords( 0, 3).yOff shouldBe  3 * unitHeight +     aThirdHeight +- epsilon
    -TriImageCoords( 0,-3).yOff shouldBe -3 * unitHeight +     aThirdHeight +- epsilon

    -TriImageCoords( 1, 0).yOff shouldBe                   2 * aThirdHeight +- epsilon
    -TriImageCoords( 7, 3).yOff shouldBe  3 * unitHeight + 2 * aThirdHeight +- epsilon
    -TriImageCoords( 7,-3).yOff shouldBe -3 * unitHeight + 2 * aThirdHeight +- epsilon
    -TriImageCoords(-7, 3).yOff shouldBe  3 * unitHeight + 2 * aThirdHeight +- epsilon
    -TriImageCoords(-7,-3).yOff shouldBe -3 * unitHeight + 2 * aThirdHeight +- epsilon
  }
  "centroid seems to be something else"
}
