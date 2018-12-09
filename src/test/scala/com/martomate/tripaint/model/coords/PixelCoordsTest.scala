package com.martomate.tripaint.model.coords

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class PixelCoordsTest extends FlatSpec with Matchers with MockFactory {
  private def tc(x: Int, y: Int): TriangleCoords = TriangleCoords(x, y)
  private def ic(x: Int, y: Int): TriImageCoords = TriImageCoords(x, y)

  private def testNeighbours(sz: Int, pc: PixelCoords)(result: PixelCoords*): Unit =
    pc.neighbours(sz) should contain theSameElementsAs result

  "neighbours" should "work for upside up" in {
    val pc = PixelCoords(tc(2, 3), ic(0, 0))
    testNeighbours(8, pc)(
      PixelCoords(tc(3, 4), ic(0, 0)),
      PixelCoords(tc(1, 3), ic(0, 0)),
      PixelCoords(tc(3, 3), ic(0, 0))
    )
  }

  it should "work for upside up, upside down pixel" in {
    val pc = PixelCoords(tc(3, 3), ic(0, 0))
    testNeighbours(8, pc)(
      PixelCoords(tc(2, 2), ic(0, 0)),
      PixelCoords(tc(2, 3), ic(0, 0)),
      PixelCoords(tc(4, 3), ic(0, 0))
    )
  }
  it should "work for upside up, left edge" in {
    val sz = 8
    val pc = PixelCoords(tc(0, 3), ic(0, 0))
    testNeighbours(sz, pc)(
      PixelCoords(tc(1, 4), ic(0, 0)),
      PixelCoords(tc(0, sz-1 - 3), ic(-1, 0)),
      PixelCoords(tc(1, 3), ic(0, 0))
    )
  }

  it should "work for upside up, right edge" in {
    val sz = 8
    val yy = 3
    val xx = 2 * yy
    val pc = PixelCoords(tc(xx, yy), ic(0, 0))
    testNeighbours(sz, pc)(
      PixelCoords(tc(xx+1, yy+1), ic(0, 0)),
      PixelCoords(tc(xx-1, yy), ic(0, 0)),
      PixelCoords(tc(2 * (sz-1 - yy), sz-1 - yy), ic(1, 0))
    )
  }
  it should "work for upside up, down edge" in {
    val sz = 8
    val yy = sz-1
    val xx = 2
    val pc = PixelCoords(tc(xx, yy), ic(0, 0))
    testNeighbours(sz, pc)(
      PixelCoords(tc(2 * (sz - 1) - xx, yy), ic(1, -1)),
      PixelCoords(tc(xx-1, yy), ic(0, 0)),
      PixelCoords(tc(xx+1, yy), ic(0, 0))
    )
  }

  it should "work for upside down" in {
    val pc = PixelCoords(tc(2, 3), ic(1, 0))
    testNeighbours(8, pc)(
      PixelCoords(tc(3, 4), ic(1, 0)),
      PixelCoords(tc(1, 3), ic(1, 0)),
      PixelCoords(tc(3, 3), ic(1, 0))
    )
  }
  it should "work for upside down, upside down pixel" in {
    val pc = PixelCoords(tc(3, 3), ic(1, 0))
    testNeighbours(8, pc)(
      PixelCoords(tc(2, 2), ic(1, 0)),
      PixelCoords(tc(2, 3), ic(1, 0)),
      PixelCoords(tc(4, 3), ic(1, 0))
    )
  }
  it should "work for upside down, right edge" in {
    val sz = 8
    val pc = PixelCoords(tc(0, 3), ic(1, 0))
    testNeighbours(sz, pc)(
      PixelCoords(tc(1, 4), ic(1, 0)),
      PixelCoords(tc(0, sz-1 - 3), ic(2, 0)),
      PixelCoords(tc(1, 3), ic(1, 0))
    )
  }
  it should "work for upside down, left edge" in {
    val sz = 8
    val yy = 3
    val xx = 2 * yy
    val pc = PixelCoords(tc(xx, yy), ic(1, 0))
    testNeighbours(sz, pc)(
      PixelCoords(tc(xx+1, yy+1), ic(1, 0)),
      PixelCoords(tc(xx-1, yy), ic(1, 0)),
      PixelCoords(tc(2 * (sz-1 - yy), sz-1 - yy), ic(0, 0))
    )
  }
  it should "work for upside down, up edge" in {
    val sz = 8
    val yy = sz-1
    val xx = 2
    val pc = PixelCoords(tc(xx, yy), ic(1, 0))
    testNeighbours(sz, pc)(
      PixelCoords(tc(2 * (sz - 1) - xx, yy), ic(0, 1)),
      PixelCoords(tc(xx-1, yy), ic(1, 0)),
      PixelCoords(tc(xx+1, yy), ic(1, 0))
    )
  }
}
