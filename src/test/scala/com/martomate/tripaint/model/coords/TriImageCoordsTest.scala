package com.martomate.tripaint.model.coords

import munit.FunSuite

class TriImageCoordsTest extends FunSuite {
  private val epsilon: Double = 1e-6
  private val unitHeight: Double = math.sqrt(3) / 2
  private val aThirdHeight: Double = unitHeight / 3

  test("(xOff, yOff) should be the mass center of the triangle") {
    assertEqualsDouble(TriImageCoords(0, 0).centerX, 0.5, epsilon)
    assertEqualsDouble(TriImageCoords(0, 3).centerX, 3 * 0.5 + 0.5, epsilon)
    assertEqualsDouble(TriImageCoords(0, -3).centerX, -3 * 0.5 + 0.5, epsilon)

    assertEqualsDouble(TriImageCoords(1, 0).centerX, 1.0, epsilon)
    assertEqualsDouble(TriImageCoords(7, 3).centerX, 3 * 0.5 + 4, epsilon)
    assertEqualsDouble(TriImageCoords(7, -3).centerX, -3 * 0.5 + 4, epsilon)
    assertEqualsDouble(TriImageCoords(-7, 3).centerX, 3 * 0.5 + -3, epsilon)
    assertEqualsDouble(TriImageCoords(-7, -3).centerX, -3 * 0.5 + -3, epsilon)

    assertEqualsDouble(-TriImageCoords(0, 0).centerY, aThirdHeight, epsilon)
    assertEqualsDouble(-TriImageCoords(0, 3).centerY, 3 * unitHeight + aThirdHeight, epsilon)
    assertEqualsDouble(-TriImageCoords(0, -3).centerY, -3 * unitHeight + aThirdHeight, epsilon)

    assertEqualsDouble(-TriImageCoords(1, 0).centerY, 2 * aThirdHeight, epsilon)
    assertEqualsDouble(-TriImageCoords(7, 3).centerY, 3 * unitHeight + 2 * aThirdHeight, epsilon)
    assertEqualsDouble(-TriImageCoords(7, -3).centerY, -3 * unitHeight + 2 * aThirdHeight, epsilon)
    assertEqualsDouble(-TriImageCoords(-7, 3).centerY, 3 * unitHeight + 2 * aThirdHeight, epsilon)
    assertEqualsDouble(-TriImageCoords(-7, -3).centerY, -3 * unitHeight + 2 * aThirdHeight, epsilon)
  }
}
