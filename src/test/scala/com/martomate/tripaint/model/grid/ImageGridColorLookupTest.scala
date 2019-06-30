package com.martomate.tripaint.model.grid

import com.martomate.tripaint.model.content.ImageContent
import com.martomate.tripaint.model.coords.{GlobalPixCoords, TriImageCoords, TriangleCoords}
import com.martomate.tripaint.model.storage.{ImageStorage, ImageStorageImpl}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import scalafx.scene.paint.Color

class ImageGridColorLookupTest extends FlatSpec with Matchers with MockFactory {
  "lookup" should "return None if there is no image" in {
    val grid = new ImageGridImplOld(16)
    val lookup = new ImageGridColorLookup(grid)

    lookup.lookup(GlobalPixCoords(0, 0)) shouldBe None
    lookup.lookup(GlobalPixCoords(10, 0)) shouldBe None
    lookup.lookup(GlobalPixCoords(100, 0)) shouldBe None
    lookup.lookup(GlobalPixCoords(0, 100)) shouldBe None
    lookup.lookup(GlobalPixCoords(0, -100)) shouldBe None
    lookup.lookup(GlobalPixCoords(-40, 10)) shouldBe None
  }

  it should "return the correct color for the (0, 0) image" in {
    val grid = new ImageGridImplOld(16)
    val lookup = new ImageGridColorLookup(grid)
    val storage = ImageStorageImpl.fromBGColor(Color.Black, 16)
    val stLink = storage
    val content = new ImageContent(TriImageCoords(0, 0), null) {
      override def storage: ImageStorage = stLink
    }

    storage(TriangleCoords(1, 13)) = Color.White

    grid(TriImageCoords(0, 0)) = content

    lookup.lookup(GlobalPixCoords(1, 2)) shouldBe Some(Color.White)
    lookup.lookup(GlobalPixCoords(1, 3)) shouldBe Some(Color.Black)
    lookup.lookup(GlobalPixCoords(-1, 3)) shouldBe None
  }

  it should "return the correct color for any image" in {
    val grid = new ImageGridImplOld(16)
    val lookup = new ImageGridColorLookup(grid)
    val storage = ImageStorageImpl.fromBGColor(Color.Black, 16)
    val stLink = storage
    val content = new ImageContent(TriImageCoords(-1, 0), null) {
      override def storage: ImageStorage = stLink
    }

    storage(TriangleCoords(0, 2)) = Color.White

    grid(TriImageCoords(-1, 0)) = content

    lookup.lookup(GlobalPixCoords(-1, 2)) shouldBe Some(Color.White)
    lookup.lookup(GlobalPixCoords(-1, 3)) shouldBe Some(Color.Black)
    lookup.lookup(GlobalPixCoords(0, 3)) shouldBe None
  }
}
