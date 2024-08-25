package tripaint.color

import tripaint.color.{Color, HsbColor}

import munit.{Compare, FunSuite}

import scala.util.Try

class ColorTest extends FunSuite {
  def compareRgb(actual: Color, expected: Color)(using munit.Location): Unit = {
    assertEqualsDouble(actual.r, expected.r, 0.001, actual)
    assertEqualsDouble(actual.g, expected.g, 0.001, actual)
    assertEqualsDouble(actual.b, expected.b, 0.001, actual)
    assertEquals(actual.a, 1.0)
  }

  def compareHsb(actual: HsbColor, expected: HsbColor)(using munit.Location): Unit = {
    assertEqualsDouble(actual.s, expected.s, 0.001, actual)
    assertEqualsDouble(actual.b, expected.b, 0.001, actual)
    assertEqualsDouble(actual.h, expected.h, 0.1, actual)
    assertEquals(actual.a, 1.0)
  }

  given hsbColorComparator: Compare[HsbColor, HsbColor] with {
    override def isEqual(obtained: HsbColor, expected: HsbColor) = {
      Try(compareHsb(obtained, expected)).isSuccess
    }
  }

  given rgbColorComparator: Compare[Color, Color] with {
    override def isEqual(obtained: Color, expected: Color) = {
      Try(compareRgb(obtained, expected)).isSuccess
    }
  }

  val cases = Seq(
    (Color(1.000, 1.000, 1.000, 1.0), HsbColor(000.0, 0.000, 1.000, 1.0)),
    (Color(0.500, 0.500, 0.500, 1.0), HsbColor(000.0, 0.000, 0.500, 1.0)),
    (Color(0.000, 0.000, 0.000, 1.0), HsbColor(000.0, 0.000, 0.000, 1.0)),
    (Color(1.000, 0.000, 0.000, 1.0), HsbColor(000.0, 1.000, 1.000, 1.0)),
    (Color(0.750, 0.750, 0.000, 1.0), HsbColor(060.0, 1.000, 0.750, 1.0)),
    (Color(0.000, 0.500, 0.000, 1.0), HsbColor(120.0, 1.000, 0.500, 1.0)),
    (Color(0.500, 1.000, 1.000, 1.0), HsbColor(180.0, 0.500, 1.000, 1.0)),
    (Color(0.500, 0.500, 1.000, 1.0), HsbColor(240.0, 0.500, 1.000, 1.0)),
    (Color(0.750, 0.250, 0.750, 1.0), HsbColor(300.0, 0.667, 0.750, 1.0)),
    (Color(0.628, 0.643, 0.142, 1.0), HsbColor(061.8, 0.779, 0.643, 1.0)),
    (Color(0.255, 0.104, 0.918, 1.0), HsbColor(251.1, 0.887, 0.918, 1.0)),
    (Color(0.116, 0.675, 0.255, 1.0), HsbColor(134.9, 0.828, 0.675, 1.0)),
    (Color(0.941, 0.785, 0.053, 1.0), HsbColor(049.5, 0.944, 0.941, 1.0)),
    (Color(0.704, 0.187, 0.897, 1.0), HsbColor(283.7, 0.792, 0.897, 1.0)),
    (Color(0.931, 0.463, 0.316, 1.0), HsbColor(014.3, 0.661, 0.931, 1.0)),
    (Color(0.998, 0.974, 0.532, 1.0), HsbColor(056.9, 0.467, 0.998, 1.0)),
    (Color(0.099, 0.795, 0.591, 1.0), HsbColor(162.4, 0.875, 0.795, 1.0)),
    (Color(0.211, 0.149, 0.597, 1.0), HsbColor(248.3, 0.750, 0.597, 1.0)),
    (Color(0.495, 0.493, 0.721, 1.0), HsbColor(240.5, 0.316, 0.721, 1.0))
  )

  test("rgb to hsb") {
    for (rgb, hsb) <- cases do {
      assertEquals(rgb.toHsb, hsb)
    }
  }

  test("hsb to rgb") {
    for (rgb, hsb) <- cases do {
      assertEquals(hsb.toRgb, rgb)
    }
  }
}
