package tripaint.view.image

import tripaint.coords.TriangleCoords

import munit.FunSuite

class IndexMapTest extends FunSuite {

  def make(imageSize: Int): IndexMap = {
    new IndexMap(imageSize)
  }

  test("coordsAt should return null outside of the image") {
    val indexMap = make(12)

    assertEquals(indexMap.coordsAt(10, 0), null)
    assertEquals(indexMap.coordsAt(-10, 0), null)
    assertEquals(indexMap.coordsAt(0, 10), null)
    assertEquals(indexMap.coordsAt(0, -10), null)

    assertEquals(indexMap.coordsAt(0.49, 0), null)
    assertEquals(indexMap.coordsAt(0.51, 0), null)

    assertEquals(indexMap.coordsAt(0, 1 / 80.0), null)
    assertEquals(indexMap.coordsAt(1, 1 / 80.0), null)
  }

  test("coordsAt should be correct for size 2") {
    val indexMap = make(2)

    assertEquals(indexMap.coordsAt(0.50, 79 / 80.0), TriangleCoords(1, 1))
    assertEquals(indexMap.coordsAt(0.49, 79 / 80.0), TriangleCoords(0, 1))
    assertEquals(indexMap.coordsAt(0.51, 79 / 80.0), TriangleCoords(2, 1))

    assertEquals(indexMap.coordsAt(0.2475, 39 / 80.0), null)
    assertEquals(indexMap.coordsAt(0.26, 39 / 80.0), TriangleCoords(0, 0))
    assertEquals(indexMap.coordsAt(0.50, 39 / 80.0), TriangleCoords(0, 0))
    assertEquals(indexMap.coordsAt(0.74, 39 / 80.0), TriangleCoords(0, 0))
    assertEquals(indexMap.coordsAt(0.7525, 39 / 80.0), null)

    assertEquals(indexMap.coordsAt(0.2475, 41 / 80.0), TriangleCoords(0, 1))
    assertEquals(indexMap.coordsAt(0.26, 41 / 80.0), TriangleCoords(1, 1))
    assertEquals(indexMap.coordsAt(0.50, 41 / 80.0), TriangleCoords(1, 1))
    assertEquals(indexMap.coordsAt(0.74, 41 / 80.0), TriangleCoords(1, 1))
    assertEquals(indexMap.coordsAt(0.7525, 41 / 80.0), TriangleCoords(2, 1))
  }

  test("coordsAt should return correct y levels") {
    val indexMap = make(20)

    for (y <- 0 until 20) {
      val steps = 23
      val stepSize = 0.05 / steps
      for (d <- 1 until steps) {
        val yReal = y * 0.05 + d * stepSize
        assertEquals((y, d, indexMap.coordsAt(0.500, yReal)), (y, d, TriangleCoords(y, y)))
        if (y > 10) {
          assertEquals((y, d, indexMap.coordsAt(0.250, yReal)), (y, d, TriangleCoords(y - 10, y)))
          assertEquals((y, d, indexMap.coordsAt(0.750, yReal)), (y, d, TriangleCoords(y + 10, y)))
        }
      }
    }
  }

  test("coordsAt should return correct x values") {
    val indexMap = make(20)

    for (x <- 0 until 20) {
      val steps = 23
      val stepSize = 0.05 / steps
      for (d <- 1 until steps) {
        val xReal = x * 0.05 + d * stepSize
        assertEquals((x, d, indexMap.coordsAt(xReal, 0.999999)), (x, d, TriangleCoords(2 * x, 19)))
      }
    }
  }
}
