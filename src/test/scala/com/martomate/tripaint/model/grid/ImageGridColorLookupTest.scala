package com.martomate.tripaint.model.grid

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.{GlobalPixCoords, TriImageCoords, TriangleCoords}
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.model.image.storage.ImageStorage
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ImageGridColorLookupTest extends AnyFlatSpec with Matchers {
  "lookup" should "return None if there is no image" in {
    val grid = new ImageGrid(16)
    val lookup = new ImageGridColorLookup(grid)

    lookup.lookup(GlobalPixCoords(0, 0)) shouldBe None
    lookup.lookup(GlobalPixCoords(10, 0)) shouldBe None
    lookup.lookup(GlobalPixCoords(100, 0)) shouldBe None
    lookup.lookup(GlobalPixCoords(0, 100)) shouldBe None
    lookup.lookup(GlobalPixCoords(0, -100)) shouldBe None
    lookup.lookup(GlobalPixCoords(-40, 10)) shouldBe None
  }

  it should "return the correct color for the (0, 0) image" in {
    val grid = new ImageGrid(16)
    val lookup = new ImageGridColorLookup(grid)
    val storage = ImageStorage.fromBGColor(Color.Black, 16)
    val content = new ImageContent(TriImageCoords(0, 0), storage)

    storage(TriangleCoords(1, 13)) = Color.White

    grid.set(content)

    lookup.lookup(GlobalPixCoords(1, 2)) shouldBe Some(Color.White)
    lookup.lookup(GlobalPixCoords(1, 3)) shouldBe Some(Color.Black)
    lookup.lookup(GlobalPixCoords(-1, 3)) shouldBe None
  }

  it should "return the correct color for any image" in {
    val grid = new ImageGrid(16)
    val lookup = new ImageGridColorLookup(grid)
    val storage = ImageStorage.fromBGColor(Color.Black, 16)
    val content = new ImageContent(TriImageCoords(-1, 0), storage)

    storage(TriangleCoords(0, 2)) = Color.White

    grid.set(content)

    lookup.lookup(GlobalPixCoords(-1, 2)) shouldBe Some(Color.White)
    lookup.lookup(GlobalPixCoords(-1, 3)) shouldBe Some(Color.Black)
    lookup.lookup(GlobalPixCoords(0, 3)) shouldBe None
  }
}
