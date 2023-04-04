package com.martomate.tripaint.model.coords

import munit.FunSuite

class GlobalPixCoordsTest extends FunSuite {
  private def make(x: Int, y: Int): GlobalPixCoords = GlobalPixCoords(x, y)

  private val neighborDistSq = 1.0 / 3 // dist = sqrt(3) / 2 * (2.0 / 3)
  private val eps = 0.0001

  test("distanceSq should be 0 for the same coordinate") {
    assertEqualsDouble(make(4, 5) distanceSq make(4, 5), 0.0, eps)
    assertEqualsDouble(GlobalPixCoords(11, -5) distanceSq make(11, -5), 0.0, eps)
  }

  test("distanceSq should be 1 / 3 for a direct neighbor") {
    assertEqualsDouble(make(4, 5) distanceSq make(5, 5), neighborDistSq, eps)
    assertEqualsDouble(make(4, 5) distanceSq make(3, 5), neighborDistSq, eps)
    assertEqualsDouble(make(4, 5) distanceSq make(5, 4), neighborDistSq, eps)

    assertEqualsDouble(make(-3, 15) distanceSq make(-2, 15), neighborDistSq, eps)
    assertEqualsDouble(make(-3, 15) distanceSq make(-4, 15), neighborDistSq, eps)
    assertEqualsDouble(make(-3, 15) distanceSq make(-4, 16), neighborDistSq, eps)
  }

  test("distanceSq should be 1 for offset (+-2, 0)") {
    assertEqualsDouble(make(4, 5) distanceSq make(6, 5), 1.0, eps)
    assertEqualsDouble(make(4, 5) distanceSq make(2, 5), 1.0, eps)

    assertEqualsDouble(make(-3, 15) distanceSq make(-1, 15), 1.0, eps)
    assertEqualsDouble(make(-3, 15) distanceSq make(-5, 15), 1.0, eps)
  }

  test("distanceSq should be symmetric") {
    for (i <- 1 to 10) {
      for (j <- 1 to 10) {
        val c1 = make(i * 13 % 29 - 17, j * 17 % 29 - 13)
        val c2 = make(j * 17 % 29 - 13, i * 13 % 29 - 17)
        assertEquals(c1 distanceSq c2, c2 distanceSq c1)
      }
    }
  }
}
