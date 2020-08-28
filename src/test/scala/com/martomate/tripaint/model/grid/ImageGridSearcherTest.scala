package com.martomate.tripaint.model.grid

import com.martomate.tripaint.model.coords.GlobalPixCoords
import scalafx.scene.paint.Color
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ImageGridSearcherTest extends AnyFlatSpec with Matchers {
  private def makeWhite = new ImageGridSearcher(_ => Some(Color.White))

  "search" should "return nothing for the 'false' predicate" in {
    val startPos = GlobalPixCoords(4, 7)
    val s = makeWhite
    s.search(startPos, (_, _) => false) shouldBe Seq()
  }

  it should "return the startPos if the predicate is false for the neighbors" in {
    val startPos = GlobalPixCoords(4, 7)
    val s = makeWhite
    s.search(startPos, (c, _) => c == startPos || c.distanceSq(startPos) > 0.34) shouldBe Seq(startPos)
  }
}
