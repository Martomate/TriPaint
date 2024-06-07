package tripaint.coords

import munit.FunSuite

class TriangleCoordsTest extends FunSuite {
  test("constructor should require x >= 0") {
    testApplyFail(-1, 0)
    testApplyFail(-1, -1)
  }

  test("constructor should require x <= 2 * y") {
    testApplyFail(0, -1)
    testApplyFail(1, 0)
    testApplyFail(3, 1)
    testApplyFail(-1, 4)
  }

  test("constructor should require y < 0x1000") {
    testApplyFail(0, 0x1000)
    testApplyFail(0x1000, 0x1000)
    testApplyFail(0x1000, 0x10000)
    TriangleCoords(0x1000, 0xfff)
    TriangleCoords(0x1ffe, 0xfff)
  }

  test("toInt should return xxxxyyy") {
    testToIntSuccess(0, 0)(0x000000)
    testToIntSuccess(0, 1)(0x000001)
    testToIntSuccess(1, 1)(0x001001)
    testToIntSuccess(0x011, 0x110)(0x011110)
    testToIntSuccess(0xfff, 0xfff)(0xffffff)
    testToIntSuccess(0xfff * 2, 0xfff)(0x1ffefff)
  }
  test("fromInt should return null for -1") {
    assertEquals(TriangleCoords.fromInt(-1), null)
  }
  test("fromInt should throw an exception for invalid input") {
    testFromIntFail(0x001000)
    testFromIntFail(0x110011)
    testFromIntFail(0x1ffffff)
  }
  test("fromInt should be the inverse of 'toInt'") {
    testFromIntSuccess(0x000000)(0, 0)
    testFromIntSuccess(0x000001)(0, 1)
    testFromIntSuccess(0x001001)(1, 1)
    testFromIntSuccess(0x111111)(0x111, 0x111)
    testFromIntSuccess(0x1ffefff)(0xfff * 2, 0xfff)
    testFromIntSuccess(0x011110)(0x011, 0x110)
  }

  private def testApplyFail(x: Int, y: Int): Unit =
    intercept[IllegalArgumentException](TriangleCoords(x, y))

  private def testToIntSuccess(x: Int, y: Int)(repr: Int): Unit =
    assertEquals(TriangleCoords(x, y).toInt, repr)

  private def testFromIntFail(repr: Int): Unit =
    intercept[IllegalArgumentException](TriangleCoords.fromInt(repr))

  private def testFromIntSuccess(repr: Int)(x: Int, y: Int): Unit =
    assertEquals(TriangleCoords.fromInt(repr), TriangleCoords(x, y))
}
