package tripaint.model

import tripaint.model.coords.GlobalPixCoords

import munit.FunSuite
import scalafx.scene.paint.Color

class FloodFillSearcherTest extends FunSuite {
  private def makeWhite = new FloodFillSearcher(_ => Some(Color.White))

  test("search should return nothing for the 'false' predicate") {
    val startPos = GlobalPixCoords(4, 7)
    val s = makeWhite
    assertEquals(s.search(startPos, (_, _) => false), Seq())
  }

  test("search should return the startPos if the predicate is false for the neighbors") {
    val startPos = GlobalPixCoords(4, 7)
    val s = makeWhite
    assertEquals(
      s.search(startPos, (c, _) => c == startPos || c.distanceSq(startPos) > 0.34),
      Seq(startPos)
    )
  }
}
