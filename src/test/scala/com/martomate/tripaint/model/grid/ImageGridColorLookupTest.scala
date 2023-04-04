package com.martomate.tripaint.model.grid

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.{GlobalPixCoords, TriImageCoords, TriangleCoords}
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.model.image.storage.ImageStorage
import munit.FunSuite
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ImageGridColorLookupTest extends FunSuite {
  test("lookup should return None if there is no image") {
    val grid = new ImageGrid(16)
    val lookup = new ImageGridColorLookup(grid)

    assertEquals(lookup.lookup(GlobalPixCoords(0, 0)), None)
    assertEquals(lookup.lookup(GlobalPixCoords(10, 0)), None)
    assertEquals(lookup.lookup(GlobalPixCoords(100, 0)), None)
    assertEquals(lookup.lookup(GlobalPixCoords(0, 100)), None)
    assertEquals(lookup.lookup(GlobalPixCoords(0, -100)), None)
    assertEquals(lookup.lookup(GlobalPixCoords(-40, 10)), None)
  }

  test("lookup should return the correct color for the (0, 0) image") {
    val grid = new ImageGrid(16)
    val lookup = new ImageGridColorLookup(grid)
    val storage = ImageStorage.fromBGColor(Color.Black, 16)
    val content = new ImageContent(TriImageCoords(0, 0), storage)

    storage(TriangleCoords(1, 13)) = Color.White

    grid.set(content)

    assertEquals(lookup.lookup(GlobalPixCoords(1, 2)), Some(Color.White))
    assertEquals(lookup.lookup(GlobalPixCoords(1, 3)), Some(Color.Black))
    assertEquals(lookup.lookup(GlobalPixCoords(-1, 3)), None)
  }

  test("lookup should return the correct color for any image") {
    val grid = new ImageGrid(16)
    val lookup = new ImageGridColorLookup(grid)
    val storage = ImageStorage.fromBGColor(Color.Black, 16)
    val content = new ImageContent(TriImageCoords(-1, 0), storage)

    storage(TriangleCoords(0, 2)) = Color.White

    grid.set(content)

    assertEquals(lookup.lookup(GlobalPixCoords(-1, 2)), Some(Color.White))
    assertEquals(lookup.lookup(GlobalPixCoords(-1, 3)), Some(Color.Black))
    assertEquals(lookup.lookup(GlobalPixCoords(0, 3)), None)
  }
}
