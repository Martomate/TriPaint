package com.martomate.tripaint.model.effects

import com.martomate.tripaint.model.{Color, ImageGrid}
import com.martomate.tripaint.model.coords.{GridCoords, TriangleCoords}
import com.martomate.tripaint.model.image.{GridCell, ImageStorage}

import munit.FunSuite

class CellEffectTest extends FunSuite {
  test("Cell effect works for bottom triangles") {
    val image = ImageStorage.fill(4, Color.Black)
    image.setColor(TriangleCoords(0, 0), Color.Cyan)
    image.setColor(TriangleCoords(0, 1), Color.Red)
    image.setColor(TriangleCoords(1, 1), Color.Green)
    image.setColor(TriangleCoords(2, 1), Color.Blue)

    val grid = ImageGrid(4)
    grid.set(GridCell(GridCoords(2, 0), image))

    CellEffect().action(Seq(GridCoords(2, 0)), grid)

    val c = Color.fromInt(((Color.Cyan + Color.Red + Color.Green + Color.Blue) / 4).toInt)
    assertEquals(image.getColor(TriangleCoords(0, 0)), c)
    assertEquals(image.getColor(TriangleCoords(0, 1)), c)
    assertEquals(image.getColor(TriangleCoords(1, 1)), c)
    assertEquals(image.getColor(TriangleCoords(2, 1)), c)
  }

  test("Cell effect works for top triangles") {
    val image = ImageStorage.fill(4, Color.Black)
    image.setColor(TriangleCoords(0, 0), Color.Cyan)
    image.setColor(TriangleCoords(0, 1), Color.Red)
    image.setColor(TriangleCoords(1, 1), Color.Green)
    image.setColor(TriangleCoords(2, 1), Color.Blue)

    val grid = ImageGrid(4)
    grid.set(GridCell(GridCoords(1, 0), image))

    CellEffect().action(Seq(GridCoords(1, 0)), grid)

    val c = Color.fromInt(((Color.Cyan + Color.Red + Color.Green + Color.Blue) / 4).toInt)
    assertEquals(image.getColor(TriangleCoords(0, 0)), c)
    assertEquals(image.getColor(TriangleCoords(0, 1)), c)
    assertEquals(image.getColor(TriangleCoords(1, 1)), c)
    assertEquals(image.getColor(TriangleCoords(2, 1)), c)
  }
}
