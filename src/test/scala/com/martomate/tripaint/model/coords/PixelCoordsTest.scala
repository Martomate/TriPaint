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

  def makeG(sz: Int)(i: (Int, Int))(t: (Int, Int)): GlobalPixCoords =
    makeP(i)(t).toGlobal(sz)

  def makeP(i: (Int, Int))(t: (Int, Int)): PixelCoords =
    PixelCoords(TriangleCoords(t._1, t._2), TriImageCoords(i._1, i._2))

  "toGlobal" should "scale even triangles simply" in {
    val sz = 8
    makeG(sz)(i = (0, 0))(t = (0, 0)) shouldBe GlobalPixCoords(0, sz-1)
    makeG(sz)(i = (0, 5))(t = (0, 0)) shouldBe GlobalPixCoords(0, 5 * sz + sz-1)
    makeG(sz)(i = (-2, 5))(t = (0, 0)) shouldBe GlobalPixCoords(-2 * sz, 5 * sz + sz-1)
    makeG(sz)(i = (42, -51))(t = (0, 0)) shouldBe GlobalPixCoords(42 * sz, -51 * sz + sz-1)
  }

  it should "handle odd triangles" in {
    val sz = 8
    makeG(sz)(i = (1, 0))(t = (0, 0)) shouldBe GlobalPixCoords(sz + sz-1, 0)
    makeG(sz)(i = (-1, 0))(t = (0, 0)) shouldBe GlobalPixCoords(-sz + sz-1, 0)
    makeG(sz)(i = (3, 1))(t = (0, 0)) shouldBe GlobalPixCoords(3 * sz + sz-1, sz)
    makeG(sz)(i = (-5, -2))(t = (0, 0)) shouldBe GlobalPixCoords(-5 * sz + sz-1, -2 * sz)
  }

  it should "handle triangle offset for even triangles" in {
    val sz = 8
    makeG(sz)(i = (0, 0))(t = (3, 5)) shouldBe GlobalPixCoords(3, sz-1 - 5)
    makeG(sz)(i = (0, 0))(t = (12, 7)) shouldBe GlobalPixCoords(12, sz-1 - 7)
    makeG(sz)(i = (-2, 5))(t = (1, 4)) shouldBe GlobalPixCoords(-2 * sz + 1, 5 * sz + sz-1 - 4)
  }

  it should "handle triangle offset for odd triangles" in {
    val sz = 8
    makeG(sz)(i = (1, 0))(t = (0, 5)) shouldBe GlobalPixCoords(sz + sz-1, 5)
    makeG(sz)(i = (3, 1))(t = (3, 4)) shouldBe GlobalPixCoords(3 * sz + sz-1 - 3, sz + 4)
    makeG(sz)(i = (-5, -2))(t = (4, 3)) shouldBe GlobalPixCoords(-5 * sz + sz-1 - 4, -2 * sz + 3)
  }

  "apply(GlobalPixCoords)" should "handle the origin" in {
    val sz = 8
    testPixelCoordsApply(sz, makeP(0, 0)(0, 0))
  }

  it should "handle the (0, 0) image" in {
    val sz = 8
    testPixelCoordsApply(sz, makeP(0, 0)(0, 2))
    testPixelCoordsApply(sz, makeP(0, 0)(1, 2))
    testPixelCoordsApply(sz, makeP(0, 0)(4, 2))
    testPixelCoordsApply(sz, makeP(0, 0)(4, 7))
    testPixelCoordsApply(sz, makeP(0, 0)(14, 7))
  }

  it should "handle (0, 0) in an image" in {
    val sz = 8
    testPixelCoordsApply(sz, makeP(0, 1)(0, 0))
    testPixelCoordsApply(sz, makeP(1, 0)(0, 0))
    testPixelCoordsApply(sz, makeP(0, -1)(0, 0))
    testPixelCoordsApply(sz, makeP(-1, 0)(0, 0))
    testPixelCoordsApply(sz, makeP(1, 1)(0, 0))
    testPixelCoordsApply(sz, makeP(1, -1)(0, 0))
    testPixelCoordsApply(sz, makeP(-1, 1)(0, 0))
    testPixelCoordsApply(sz, makeP(-1, -1)(0, 0))
  }

  it should "handle some other points" in {
    val sz = 8
    testPixelCoordsApply(sz, makeP(0, 0)(1, 7))
    testPixelCoordsApply(sz, makeP(2, 0)(3, 2))
    testPixelCoordsApply(sz, makeP(2, 5)(4, 2))
    testPixelCoordsApply(sz, makeP(1, 5)(4, 2))
    testPixelCoordsApply(sz, makeP(1, 5)(4, 7))
    testPixelCoordsApply(sz, makeP(-1, 5)(4, 7))
    testPixelCoordsApply(sz, makeP(-11, 5)(4, 7))
    testPixelCoordsApply(sz, makeP(11, 5)(14, 7))
  }

  private def testPixelCoordsApply(sz: Int, src: PixelCoords): Unit = {
    PixelCoords(src.toGlobal(sz), sz) shouldBe src
  }
}
