package tripaint.coords

import munit.FunSuite

class GridCoordsTest extends FunSuite {
  private val epsilon: Double = 1e-6
  private val unitHeight: Double = math.sqrt(3) / 2
  private val aThirdHeight: Double = unitHeight / 3

  test("(xOff, yOff) should be the mass center of the triangle") {
    assertEqualsDouble(GridCoords(0, 0).center._1, 0.5, epsilon)
    assertEqualsDouble(GridCoords(0, 3).center._1, 3 * 0.5 + 0.5, epsilon)
    assertEqualsDouble(GridCoords(0, -3).center._1, -3 * 0.5 + 0.5, epsilon)

    assertEqualsDouble(GridCoords(1, 0).center._1, 1.0, epsilon)
    assertEqualsDouble(GridCoords(7, 3).center._1, 3 * 0.5 + 4, epsilon)
    assertEqualsDouble(GridCoords(7, -3).center._1, -3 * 0.5 + 4, epsilon)
    assertEqualsDouble(GridCoords(-7, 3).center._1, 3 * 0.5 + -3, epsilon)
    assertEqualsDouble(GridCoords(-7, -3).center._1, -3 * 0.5 + -3, epsilon)

    assertEqualsDouble(-GridCoords(0, 0).center._2, aThirdHeight, epsilon)
    assertEqualsDouble(-GridCoords(0, 3).center._2, 3 * unitHeight + aThirdHeight, epsilon)
    assertEqualsDouble(-GridCoords(0, -3).center._2, -3 * unitHeight + aThirdHeight, epsilon)

    assertEqualsDouble(-GridCoords(1, 0).center._2, 2 * aThirdHeight, epsilon)
    assertEqualsDouble(-GridCoords(7, 3).center._2, 3 * unitHeight + 2 * aThirdHeight, epsilon)
    assertEqualsDouble(-GridCoords(7, -3).center._2, -3 * unitHeight + 2 * aThirdHeight, epsilon)
    assertEqualsDouble(-GridCoords(-7, 3).center._2, 3 * unitHeight + 2 * aThirdHeight, epsilon)
    assertEqualsDouble(-GridCoords(-7, -3).center._2, -3 * unitHeight + 2 * aThirdHeight, epsilon)
  }
}
